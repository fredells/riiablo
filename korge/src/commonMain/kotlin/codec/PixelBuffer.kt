package codec

import com.soywiz.korio.lang.assert

class PixelBuffer {
    var value = ByteArray(4)
    var frame = -1
    var frameCellIndex = -1

    companion object {
        const val MAX_VALUE = 65586
        val PIXEL_TABLE = intArrayOf(0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4)
    }
}

@ExperimentalStdlibApi
fun fillPixelBuffer(cache: Cache, dir: DirectionData, frames: Array<FrameData>) {
    cache.pixelBuffer = arrayOfNulls(PixelBuffer.MAX_VALUE)
    cache.frameBuffer = Bitmap.create(dir.width, dir.height)

    prepareBufferCells(cache, dir.width, dir.height)
    val frameBufferCellsW: Int = cache.frameBufferCellsW
    val frameBufferCellsH: Int = cache.frameBufferCellsH
    val numCells = frameBufferCellsW * frameBufferCellsH
    val cellBuffer: Array<PixelBuffer?> = arrayOfNulls(numCells)
    var cellsW: Int
    var cellsH: Int
    var cellX: Int
    var cellY: Int
    var tmp: Int
    var pixelMask = 0
    var lastPixel: Int
    var pixels: Int
    var decodedPixels: Int
    val readPixel = IntArray(4)
    var encodingType: Int
    var pixelDisplacement: Int
    var oldEntry: PixelBuffer?
    var newEntry: PixelBuffer
    var curId: Int
    var pixelBufferId = 0
    var curCX: Int
    var curCY: Int
    var curCell: Int
    for (f in frames.indices) {
        val frame: FrameData = frames[f]
        cache.frame[f] = Cache.FrameCache()

        val frameCache: Cache.FrameCache = cache.frame[f]!!
        prepareFrameCells(cache, frameCache, dir.xMin, dir.yMin, frame)

        cellsW = frameCache.cellsW
        cellsH = frameCache.cellsH

        cellX = (frame.xMin - dir.xMin) / 4
        cellY = (frame.yMin - dir.yMin) / 4

        for (cy in 0 until cellsH) {
            curCY = cellY + cy
            for (cx in 0 until cellsW) {
                curCX = cellX + cx
                curCell = curCY * frameBufferCellsW + curCX
                assert(curCell < numCells)
                var nextCell = false
                try {
                    val x = cellBuffer[curCell]
                } catch (e: Exception) {
                    val y = ""
                }
                if (cellBuffer[curCell] != null) {
                    tmp = if (dir.equalCellBitStreamSize > 0) {
                        dir.equalCellBitStream.readRaw(1)
                    } else {
                        0
                    }
                    if (tmp == 0) {
                        pixelMask = dir.pixelMaskBitStream.readRaw(4)
                        assert(pixelMask >= 0)
                    } else {
                        nextCell = true
                    }
                } else {
                    pixelMask = 0xF
                }
                if (!nextCell) {
                    readPixel.fill(0)
                    pixels = PixelBuffer.PIXEL_TABLE[pixelMask]
                    if (pixels > 0 && dir.encodingTypeBitStreamSize > 0) {
                        encodingType = dir.encodingTypeBitStream.readRaw(1)
                        assert(encodingType >= 0)
                    } else {
                        encodingType = 0
                    }
                    lastPixel = 0
                    decodedPixels = 0
                    for (i in 0 until pixels) {
                        if (encodingType > 0) {
                            readPixel[i] = try {
                                dir.rawPixelCodesBitStream.readRaw(8).toShort().toInt()
                            } catch (e: Exception) {
                                0
                            }
                        } else {
                            readPixel[i] = lastPixel
                            do {
                                pixelDisplacement = try {
                                    dir.pixelCodeAndDisplacementBitStream.readRaw(4)
                                } catch (e: Exception) {
                                    0
                                }
                                readPixel[i] += pixelDisplacement
                            } while (pixelDisplacement == 0xF)
                        }
                        if (readPixel[i] == lastPixel) {
                            readPixel[i] = 0
                            break
                        } else {
                            lastPixel = readPixel[i]
                            decodedPixels++
                        }
                    }
                    oldEntry = cellBuffer[curCell]
                    if (pixelBufferId >= PixelBuffer.MAX_VALUE) {
                        throw Exception("Pixel buffer full, cannot add more entries")
                    }

                    newEntry = PixelBuffer()
                    cache.pixelBuffer[pixelBufferId++] = newEntry
                    curId = decodedPixels - 1
                    for (i in 0..3) {
                        if (pixelMask and (1 shl i) != 0) {
                            if (curId >= 0) {
                                newEntry.value[i] = readPixel[curId--].toByte()
                            } else {
                                newEntry.value[i] = 0
                            }
                        } else {
                            newEntry.value[i] = oldEntry!!.value[i]
                        }
                    }
                    cellBuffer[curCell] = newEntry
                    newEntry.frame = f // TODO: I'm not sure how this will behave with f as df
                    newEntry.frameCellIndex = cy * cellsW + cx
                }
            }
        }
    }
    var pbe: PixelBuffer
    for (i in 0 until pixelBufferId) {
        for (x in 0..3) {
            pbe = cache.pixelBuffer[i]!!
            val y: Int = pbe.value[x].toInt() and 0xFF // added toInt
            pbe.value[x] = dir.pixelValues[y]
        }
    }
    cache.numEntries = pixelBufferId
}

fun prepareBufferCells(cache: Cache, width: Int, height: Int) {
    val bufferW: Int = width
    val bufferH: Int = height
    cache.frameBufferCellsW = 1 + (bufferW - 1) / 4
    val cellsW: Int = cache.frameBufferCellsW
    val cellW = IntArray(cellsW)
    if (cellsW == 1) {
        cellW[0] = bufferW
    } else {
        val cellMax = cellsW - 1
        cellW.fill(4, 0, cellMax)
        cellW[cellMax] = bufferW - 4 * cellMax
    }
    cache.frameBufferCellsH = 1 + (bufferH - 1) / 4
    val cellsH: Int = cache.frameBufferCellsH
    val cellH = IntArray(cellsH)
    if (cellsH == 1) {
        cellH[0] = bufferH
    } else {
        val cellMax = cellsH - 1
        cellH.fill(4, 0, cellMax)
        cellH[cellMax] = bufferH - 4 * cellMax
    }
    val numCells = cellsW * cellsH
    cache.frameBufferCells = arrayOfNulls<Cell>(numCells)

    //int id = 0;
    var y = 0
    var x = 0
    var cy = 0
    while (cy < cellsH) {
        var cx = 0
        while (cx < cellsW) {
            cache.frameBufferCells[cy * cellsW + cx] = Cell()
            val cell: Cell = cache.frameBufferCells.get(cy * cellsW + cx)!!
            cell.w = cellW[cx]
            cell.h = cellH[cy]
            cell.bmp = cache.frameBuffer?.getSubimage(x, y, cell.w, cell.h)
            cx++
            x += 4
        }
        cy++
        y += 4
        x = 0
    }

    //assert id == numCells;
}

fun prepareFrameCells(cache: Cache, frameCache: Cache.FrameCache, xMinD: Int, yMinD: Int, frame: FrameData) {
    var tmp: Int
    var tmpSize: Int
    val frameW: Int = frame.width
    val frameH: Int = frame.height
    val cellsW: Int
    val w: Int = 4 - (frame.xMin - xMinD) % 4 // TODO: & 0x3
    if (frameW - w <= 1) {
        cellsW = 1
    } else {
        tmp = frameW - w - 1
        tmpSize = 2 + tmp / 4
        if (tmp % 4 == 0) tmpSize--
        cellsW = tmpSize
    }
    val cellsH: Int
    val h: Int = 4 - (frame.yMin - yMinD) % 4 // TODO: & 0x3
    if (frameH - h <= 1) {
        cellsH = 1
    } else {
        tmp = frameH - h - 1
        tmpSize = 2 + tmp / 4
        if (tmp % 4 == 0) tmpSize--
        cellsH = tmpSize
    }
    val cellW = IntArray(cellsW)
    if (cellsW == 1) {
        cellW[0] = frameW
    } else {
        val cellMax = cellsW - 1
        cellW[0] = w
        cellW.fill(4, 1, cellMax)
        cellW[cellMax] = frameW - w - 4 * (cellMax - 1)
    }
    val cellH = IntArray(cellsH)
    if (cellsH == 1) {
        cellH[0] = frameH
    } else {
        val cellMax = cellsH - 1
        cellH[0] = h
        cellH.fill(4, 1, cellMax)
        cellH[cellMax] = frameH - h - 4 * (cellMax - 1)
    }
    frameCache.cellsW = cellsW
    frameCache.cellsH = cellsH
    val numCells = cellsW * cellsH
    frameCache.cells = arrayOfNulls<Cell>(numCells)
    var id = 0
    var cell: Cell? = null
    val xReset: Int = frame.xMin - xMinD
    var y: Int = frame.yMin - yMinD
    var x = xReset
    var cy = 0
    while (cy < cellsH) {
        var cx = 0
        while (cx < cellsW) {
            assert(id == cy * cellsW + cx)
            // TODO: Cell implements Poolable
            cell = Cell()
            cell.x = x
            cell.y = y
            cell.w = cellW[cx]
            cell.h = cellH[cy]
            cell.bmp = cache.frameBuffer?.getSubimage(cell.x, cell.y, cell.w, cell.h)
            frameCache.cells[id++] = cell
            cx++
            x += cell.w
        }
        cy++
        y += cell?.h ?: 0
        x = xReset
    }
    assert(id == numCells)
}