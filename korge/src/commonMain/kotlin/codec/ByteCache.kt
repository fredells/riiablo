package codec

import com.soywiz.korio.stream.SyncStream
import com.soywiz.korio.stream.readU8

@ExperimentalStdlibApi
data class ByteCache(val syncStream: SyncStream) {
    private var buffer = ""

    private fun readStream() {
        var buff = syncStream.readU8().toString(2)
        while (buff.length < 8) buff = "0$buff"
        buffer = buff + buffer
    }

    fun readRawBoolean(): Boolean {
        return readRaw(1) == 1
    }

    fun readRaw(bits: Int): Int {
        if (bits == 0) return 0
        while (buffer.length < bits) readStream()
        return buffer.takeLast(bits).toInt(2).also {
            buffer = buffer.dropLast(bits)
        }
    }

    fun readRawLong(bits: Int): Long {
        if (bits == 0) return 0
        while (buffer.length < bits) readStream()
        return buffer.takeLast(bits).toLong(2).also {
            buffer = buffer.dropLast(bits)
        }
    }

    fun readAll(): Long {
        return readRawLong((syncStream.available * 8).toInt())
    }

    fun clearBuffer() {
        buffer = ""
    }
}