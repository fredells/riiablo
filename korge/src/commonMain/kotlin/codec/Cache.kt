package codec

class Cache(framesPerDir: Int) {
    var frameBufferCellsW = 0
    var frameBufferCellsH = 0
    var frameBufferCells = arrayOf<Cell>()
    var pixelBuffer = arrayOf<PixelBuffer>()
    var numEntries = 0
    var frameBuffer: Bitmap? = null
    var frame: Array<FrameCache?> = arrayOfNulls(framesPerDir)

    class FrameCache {
        var cellsW = 0
        var cellsH = 0
        var cells = arrayOf<Cell>()
    }
}