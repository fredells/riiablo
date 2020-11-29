package codec

import com.soywiz.korim.color.RGBA
import com.soywiz.korim.color.RgbaArray
import com.soywiz.korio.file.VfsFile

object Palette {
    const val COLORS = 256

    suspend fun rgbaArray(file: VfsFile): RgbaArray {
        val data: ByteArray = file.readAll()
        val rgbaArray = RgbaArray.invoke(COLORS)

        var i = 0
        var j = 0
        while (i < COLORS) {
            val b = data[j++].toInt() and 0xFF
            val g = data[j++].toInt() and 0xFF
            val r = data[j++].toInt() and 0xFF
            rgbaArray[i] = RGBA.Companion.invoke(r, g, b)
            i++
        }
        return rgbaArray
    }
}