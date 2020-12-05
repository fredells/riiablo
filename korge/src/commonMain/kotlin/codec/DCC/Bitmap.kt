package codec.DCC

import com.soywiz.kmem.arraycopy
import com.soywiz.korio.lang.assert

class Bitmap {
    var colormap: ByteArray
    var x: Int
    var y: Int
    var width: Int
    var height: Int
    var stride: Int

    constructor(colormap: ByteArray, w: Int, h: Int) {
        this.colormap = colormap
        y = 0
        x = y
        stride = w
        width = stride
        height = h
    }

    constructor(bmp: Bitmap, x: Int, y: Int, w: Int, h: Int) {
        colormap = bmp.colormap
        this.x = bmp.x + x
        this.y = bmp.y + y
        width = w
        height = h
        stride = bmp.stride
        val debugX = x + w
        val debugY = y + h
        if (debugX > bmp.width || debugY > bmp.height) {
            val z = ""
        }
        assert(x + w <= bmp.width && y + h <= bmp.height)
    }

    fun getSubimage(x: Int, y: Int, width: Int, height: Int): Bitmap {
        return Bitmap(this, x, y, width, height)
    }

    fun clear() {
        fill(0.toByte())
    }

    fun fill(id: Byte) {
        fillBytes(0, 0, width, height, id)
    }

    fun fillBytes(x: Int, y: Int, w: Int, h: Int, id: Byte) {
        var x = x
        var y = y
        x += this.x
        y += this.y
        var start = y * stride + x
        var end: Int
        for (r in 0 until h) {
            end = start + w
            colormap.fill(id, start, end)
            start += stride
        }
    }

    fun setPixel(x: Int, y: Int, i: Byte) {
        var x = x
        var y = y
        x += this.x
        y += this.y
        colormap[y * stride + x] = i
    }

    fun copy(dst: Bitmap) {
        copy(this, dst, 0, 0, 0, 0, width, height)
    }

    fun copy(): ByteArray {
        val copy = ByteArray(colormap.size)
        colormap.copyInto(copy)
        return copy
    }

    companion object {
        fun create(width: Int, height: Int): Bitmap {
            return Bitmap(ByteArray(width * height), width, height)
        }

        fun copy(src: Bitmap, dst: Bitmap,
                 srcX: Int, srcY: Int,
                 dstX: Int, dstY: Int,
                 width: Int, height: Int) {
            var srcX = srcX
            var srcY = srcY
            var dstX = dstX
            var dstY = dstY
            assert(srcX + width <= src.width && dstX + width <= dst.width)
            srcX += src.x
            srcY += src.y
            var fromIndexSrc = srcY * src.stride + srcX
            dstX += dst.x
            dstY += dst.y
            var fromIndexDst = dstY * dst.stride + dstX
            for (r in 0 until height) {
                arraycopy(
                        src.colormap, fromIndexSrc,
                        dst.colormap, fromIndexDst,
                        width)
                fromIndexSrc += src.stride
                fromIndexDst += dst.stride
            }
        }
    }
}