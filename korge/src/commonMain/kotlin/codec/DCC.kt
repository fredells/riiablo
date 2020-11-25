package codec

import com.soywiz.korim.format.*
import com.soywiz.korio.lang.printStackTrace
import com.soywiz.korio.stream.*
import kotlin.math.max
import kotlin.math.min

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
object DCC : ImageFormat("dcc") {
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

        val frames = mutableListOf<ImageFrame>()
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
                val xOffset = byteCache.readRaw(BITS_WIDTH_TABLE[xOffsetBits])
                val yOffset = byteCache.readRaw(BITS_WIDTH_TABLE[yOffsetBits])
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


        }

        return ImageData(listOf())
    }

    override fun writeImage(image: ImageData, s: SyncStream, props: ImageEncodingProps) {
        super.writeImage(image, s, props)
    }

    override fun toString(): String {
        return super.toString()
    }
}

data class FrameData(
        val variable0: Int,
        val xMin: Int,
        val xMax: Int,
        val yMin: Int,
        val yMax: Int,
        val width: Int,
        val height: Int,
        val codedBytes: Int,
        val optionalBytes: Int
) {
    var optionalBytesData: ByteArray = EMPTY_BYTE_ARRAY
}

@ExperimentalStdlibApi
data class ByteCache(val syncStream: SyncStream) {
    private var buffer = ""

    private fun readStream() {
        buffer = syncStream.readU8().toString(2) + buffer
    }

    fun readRaw(bits: Int): Int {
        if (bits == 0) return 0
        while (buffer.length < bits) readStream()
        val buff = buffer.takeLast(bits)
        val ed = buff.toInt(radix = 2)
        val ed2 = buff.toLong(radix = 2)
        return buffer.takeLast(bits).toInt(2).also {
            buffer = buffer
        }
    }

    fun clearBuffer() {
        buffer = ""
    }
}