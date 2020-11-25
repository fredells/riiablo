package codec

internal class PixelBuffer {
    var value = ByteArray(4)
    var frame = -1
    var frameCellIndex = -1

    companion object {
        //static final int MAX_VALUE = 5625;
        const val MAX_VALUE = 65586
        val PIXEL_TABLE = intArrayOf(0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4)
    }
}