package codec.DCC

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
data class DirectionData(
        val frameData: List<FrameData>,
        val compressionFlags: Int,
        val xMin: Int,
        val xMax: Int,
        val yMin: Int,
        val yMax: Int,
        val width: Int,
        val height: Int,
        val equalCellBitStreamSize: Long,
        val encodingTypeBitStreamSize: Long,
        val equalCellBitStream: ByteCache,
        val pixelMaskBitStream: ByteCache,
        val encodingTypeBitStream: ByteCache,
        val rawPixelCodesBitStream: ByteCache,
        val pixelCodeAndDisplacementBitStream: ByteCache
) {
    var pixelValues = byteArrayOf()
}