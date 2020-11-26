package codec

import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.stream.SyncStream
import com.soywiz.korio.stream.readExact

class Palette private constructor(val colors: IntArray) {
    fun get(): IntArray {
        return colors
    }

    companion object {
        private const val TAG = "Palette"
        private const val DEBUG = false
        const val COLORS = 256
        fun a(color: Int): Int {
            return color and 0xFF
        }

        fun r(color: Int): Int {
            return color ushr 24
        }

        fun g(color: Int): Int {
            return color ushr 16 and 0xFF
        }

        fun b(color: Int): Int {
            return color ushr 8 and 0xFF
        }

        fun abgr8888(a: Int, b: Int, g: Int, r: Int): Int {
            return r shl 24 or (g shl 16) or (b shl 8) or a
        }

        fun r8(r: Int): Int {
            return r shl 24
        }

        fun a8(a: Int): Int {
            return a and 0xFF
        }

        fun a8(a: Byte): Int {
            return a.toInt() and 0xFF
        }

        fun loadFromStream(`in`: SyncStream): Palette {
            return try {
                val data = ByteArray(COLORS * 3)
                `in`.readExact(data, len = data.size, offset = 0)
                loadFromArray(data)
            } catch (t: Throwable) {
                throw Exception("Couldn't load palette from stream.", t)
            }
        }

        suspend fun loadFromFile(file: VfsFile): Palette {
            val data: ByteArray = file.readAll()
            return loadFromArray(data)
        }

        private fun loadFromArray(data: ByteArray): Palette {
            var r: Int
            var g: Int
            var b: Int
            val colors = IntArray(COLORS)
            var i = 0
            var j = 0
            while (i < colors.size) {
                b = data[j++].toInt() and 0xFF
                g = data[j++].toInt() and 0xFF
                r = data[j++].toInt() and 0xFF
                colors[i] = abgr8888(0xFF, b, g, r)
                i++
            }
            colors[0] = colors[0] and -0x100
            return Palette(colors)
        }
    }
}