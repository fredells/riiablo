package codec

import com.soywiz.klock.milliseconds
import com.soywiz.korim.bitmap.Bitmap8
import com.soywiz.korim.color.RgbaArray
import com.soywiz.korim.format.*
import com.soywiz.korio.lang.assert
import com.soywiz.korio.lang.printStackTrace
import com.soywiz.korio.stream.*
import kotlin.math.max
import kotlin.math.min

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
object DCC : ImageFormat("dcc") {
    private const val HasRawPixelEncoding = 0x1
    private const val CompressEqualCells = 0x2
    var palette: RgbaArray? = null

    override fun decodeHeader(s: SyncStream, props: ImageDecodingProps): ImageInfo? {
        try {
            val stream = s.clone()
            val signature = stream.readU8()
            val version = stream.readU8()
            val directions = stream.readU8()
            val framesPerDir = stream.readU32BE()
            val tag = stream.readU32BE()
            val finalDC6Size = stream.readU32BE()
            return ImageInfo()
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
        }
    }

    override fun readImage(s: SyncStream, props: ImageDecodingProps): ImageData {
        val stream = s.clone()

        // read header
        val signature = stream.readU8()
        val version = stream.readU8()
        val directions = stream.readU8()
        val framesPerDir = stream.readU32LE()
        val tag = stream.readU32LE()
        val finalDCCSize = stream.readU32LE()

        // read offset
        val dirOffset = LongArray(directions + 1)
        repeat(directions) {
            dirOffset[it] = stream.readU32LE()
        }
        dirOffset[directions] = finalDCCSize

        val directionData = mutableListOf<DirectionData>()
        val frameData = mutableListOf<FrameData>()

        val BITS_WIDTH_TABLE = intArrayOf(
                0, 1, 2, 4, 6, 8, 10, 12, 14, 16, 20, 24, 26, 28, 30, 32
        )

        repeat(directions) { directionIndex ->
            // read direction
            val size = dirOffset[directionIndex + 1] - dirOffset[directionIndex]
            val slice = stream.readSlice(size)
            val outsizeCoded = slice.readU32LE()

            // convert to string so we can read bit by bit
            val byteCache = ByteCache(slice)

            val compressionFlags = byteCache.readRaw(2)
            val variable0Bits = byteCache.readRaw(4)
            val widthBits = byteCache.readRaw(4)
            val heightBits = byteCache.readRaw(4)
            val xOffsetBits = byteCache.readRaw(4)
            val yOffsetBits = byteCache.readRaw(4)
            val optionalBytesBits = byteCache.readRaw(4)
            val codedBytesBits = byteCache.readRaw(4)

            var dMinX = Int.MAX_VALUE
            var dMaxX = Int.MIN_VALUE
            var dMinY = Int.MAX_VALUE
            var dMaxY = Int.MIN_VALUE

            var dOptionalBytes = 0

            repeat(framesPerDir.toInt()) { frameIndex ->
                // read frame
                val variable0 = byteCache.readRaw(BITS_WIDTH_TABLE[variable0Bits])
                val width = byteCache.readRaw(BITS_WIDTH_TABLE[widthBits])
                val height = byteCache.readRaw(BITS_WIDTH_TABLE[heightBits])
                val xOffset = byteCache.readRawSigned(BITS_WIDTH_TABLE[xOffsetBits]).toInt()
                val yOffset = byteCache.readRawSigned(BITS_WIDTH_TABLE[yOffsetBits]).toInt()
                val optionalBytes = byteCache.readRaw(BITS_WIDTH_TABLE[optionalBytesBits])
                val codedBytes = byteCache.readRaw(BITS_WIDTH_TABLE[codedBytesBits])
                val flip = byteCache.readRaw(1)

                dOptionalBytes += optionalBytes

                val xMin = xOffset
                val xMax = xMin + width - 1
                val yMin: Int
                val yMax: Int

                if (flip != 0) {
                    yMin = yOffset
                    yMax = yMin + height - 1
                } else {
                    yMax = yOffset
                    yMin = yMax - height + 1
                }

                dMinX = min(dMinX, xMin)
                dMaxX = max(dMaxX, xMax)
                dMinY = min(dMinY, yMin)
                dMaxY = max(dMaxY, yMax)

                val finalWidth = xMax - xMin + 1
                val finalHeight = yMax - yMin + 1

                frameData.add(FrameData(variable0, xMin, xMax, yMin, yMax, finalWidth, finalHeight, codedBytes, optionalBytes))
            }

            // read optional bytes
            if (dOptionalBytes > 0) {
                byteCache.clearBuffer()
                for (frame in frameData) {
                    byteCache.syncStream.readExact(frame.optionalBytesData, len = frame.optionalBytes, offset = 0)
                }
            }

            // read bitstream sizes
            var equalCellBitStreamSize = 0L
            var pixelMaskBitStreamSize = 0L
            var encodingTypeBitStreamSize = 0L
            var rawPixelCodesBitStreamSize = 0L

            if (compressionFlags and CompressEqualCells == CompressEqualCells) {
                equalCellBitStreamSize = byteCache.readRawLong(20)
            }

            pixelMaskBitStreamSize = byteCache.readRawLong(20)

            if (compressionFlags and HasRawPixelEncoding == HasRawPixelEncoding) {
                encodingTypeBitStreamSize = byteCache.readRawLong(20)
                rawPixelCodesBitStreamSize = byteCache.readRawLong(20)
            }

            // read pixel values
            var index = 0
            val pixelValues = ByteArray(Palette.COLORS)
            for (i in 0 until Palette.COLORS) {
                if (byteCache.readRawBoolean()) pixelValues[index++] = i.toByte()
            }

            // init bit streams
            assert(compressionFlags and CompressEqualCells != CompressEqualCells || equalCellBitStreamSize > 0)
            val equalCellBitStream = byteCache.readSlice(equalCellBitStreamSize)
            val pixelMaskBitStream = byteCache.readSlice(pixelMaskBitStreamSize)
            assert(compressionFlags and HasRawPixelEncoding != HasRawPixelEncoding
                    || encodingTypeBitStreamSize > 0 && rawPixelCodesBitStreamSize > 0)
            val encodingTypeBitStream = byteCache.readSlice(encodingTypeBitStreamSize)
            val rawPixelCodesBitStream = byteCache.readSlice(rawPixelCodesBitStreamSize)
            val cacheBufferLen = byteCache.buffer.length
            val bytesTimes8 = byteCache.syncStream.available * 8L
            var bitsRemaining = byteCache.syncStream.available * Byte.SIZE_BITS + byteCache.buffer.length
            val pixelCodeAndDisplacementBitStream = byteCache.readAvailable()

            // TODO need to verify above sizes

            val dWidth = dMaxX - dMinX + 1
            val dHeight = dMaxY - dMinY + 1

            val dir = DirectionData(
                    compressionFlags,
                    dMinX,
                    dMaxX,
                    dMinY,
                    dMaxY,
                    dWidth,
                    dHeight,
                    equalCellBitStreamSize,
                    encodingTypeBitStreamSize,
                    equalCellBitStream,
                    pixelMaskBitStream,
                    encodingTypeBitStream,
                    rawPixelCodesBitStream,
                    pixelCodeAndDisplacementBitStream
            ).apply {
                this.pixelValues = pixelValues
            }

            val cache = Cache(framesPerDir.toInt())
            fillPixelBuffer(cache, dir, frameData.toTypedArray())
            makeFrames(cache, dir, frameData.toTypedArray())
            directionData.add(dir)
        }

        val dir = directionData.first()

        return ImageData(
                frames = frameData.map {
                    val bmp = it.bitmaps.first()
                    ImageFrame(
                            bitmap = Bitmap8(
                                    width = bmp.width,
                                    height = bmp.height,
                                    data = bmp.colormap,
                                    palette = palette!!).toBMP32(),
                            time = 40.milliseconds,
                            targetX = 0,
                            targetY = 0
                    )
                },
                loopCount = 0,
                width = dir.width,
                height = dir.height
        )
    }

    override fun writeImage(image: ImageData, s: SyncStream, props: ImageEncodingProps) {
        super.writeImage(image, s, props)
    }

    override fun toString(): String {
        return super.toString()
    }
}