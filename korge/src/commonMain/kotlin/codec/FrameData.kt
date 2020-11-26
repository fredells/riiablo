package codec

import com.soywiz.korio.stream.EMPTY_BYTE_ARRAY

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