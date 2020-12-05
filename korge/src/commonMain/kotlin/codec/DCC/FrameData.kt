package codec.DCC

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
    val bitmaps = mutableListOf<Bitmap>()
}

@ExperimentalStdlibApi
fun makeFrames(cache: Cache, dir: DirectionData, frames: Array<FrameData>) {
    val size: Int = cache.frameBufferCellsW * cache.frameBufferCellsH
    for (c in 0 until size) {
        cache.frameBufferCells[c]!!.lastW = -1
        cache.frameBufferCells[c]!!.lastH = -1
    }
    var pbId = 0
    var pbe: PixelBuffer
    var numCells: Int
    var cellX: Int
    var cellY: Int
    var cellId: Int
    var frame: FrameData
    var frameCache: Cache.FrameCache
    var f = 0
    while (f < frames.size) {
        val frameBmp: Bitmap = Bitmap.create(dir.width, dir.height)
        frame = frames[f]
        frameCache = cache.frame[f]!!
        numCells = frameCache.cellsW * frameCache.cellsH
        for (c in 0 until numCells) {
            cache.pixelBuffer[pbId]?.let {
                pbe = it
                val cell: Cell = frameCache.cells[c]!!
                cellX = cell.x / 4
                cellY = cell.y / 4
                cellId = cellY * cache.frameBufferCellsW + cellX
                val bufferCell: Cell = cache.frameBufferCells[cellId]!!
                if (pbe.frame != f || pbe.frameCellIndex != c) {
                    if (cell.w != bufferCell.lastW || cell.h != bufferCell.lastH) {
                        cell.bmp?.clear()
                    } else {
                        Bitmap.copy(cache.frameBuffer!!, cache.frameBuffer!!,
                                bufferCell.lastX, bufferCell.lastY,
                                cell.x, cell.y,
                                cell.w, cell.h)
                        Bitmap.copy(cell.bmp!!, frameBmp,
                                0, 0,
                                cell.x, cell.y,
                                cell.w, cell.h)
                    }
                } else {
                    if (pbe.value[0] == pbe.value[1]) {
                        cell.bmp!!.fill(pbe.value[0])
                    } else {
                        val bits: Int = if (pbe.value[1] == pbe.value[2]) {
                            1
                        } else {
                            2
                        }
                        for (y in 0 until cell.h) {
                            for (x in 0 until cell.w) {
                                val pix: Int = dir.pixelCodeAndDisplacementBitStream.readRaw(bits)
                                cell.bmp!!.setPixel(x, y, pbe.value[pix])
                            }
                        }
                    }
                    Bitmap.copy(cell.bmp!!, frameBmp,
                            0, 0,
                            cell.x, cell.y,
                            cell.w, cell.h)
                    pbId++
                }
                bufferCell.lastX = cell.x
                bufferCell.lastY = cell.y
                bufferCell.lastW = cell.w
                bufferCell.lastH = cell.h
            }
        }
        frame.bitmaps.add(frameBmp)
        f++
    }
}