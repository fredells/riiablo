package codec

import com.soywiz.korio.lang.assert
import com.soywiz.korio.stream.SyncStream
import com.soywiz.korio.stream.readSlice
import com.soywiz.korio.stream.readU8

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
data class ByteCache(val syncStream: SyncStream, val startingBuffer: String = "") {
    var buffer = startingBuffer
        private set

    private fun readStream() {
        var buff = syncStream.readU8().toString(2)
        while (buff.length < 8) buff = "0$buff"
        buffer = buff + buffer
    }

    fun readRawBoolean(): Boolean {
        return readRaw(1) == 1
    }

    fun readRawSigned(bits: Int): Long {
        assert(bits >= 0)
        assert(bits <= Long.SIZE_BITS)
        if (bits <= 0) return 0
        if (bits == Long.SIZE_BITS) return readRawLong(bits)
        val shift: Int = Long.SIZE_BITS - bits
        assert(shift > 0)
        val value: Long = readRawLong(bits)
        return value shl shift shr shift
    }

    fun readRaw(bits: Int): Int {
        if (bits == 0) return 0
        while (buffer.length < bits) readStream()
        return (buffer.takeLast(bits).toIntOrNull(2) ?: 0).also {
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

    fun readSlice(bits: Long): ByteCache {
        val len = (bits - buffer.length + Byte.SIZE_BITS - 1) / Byte.SIZE_BITS
        val bufferLength = buffer.length
        val anotherThing = bits % 8
        val overflow = Byte.SIZE_BITS - bufferLength + anotherThing

        val slice = ByteCache(syncStream.readSlice(len), buffer)
        syncStream.position -= 1
        clearBuffer()
        readRaw(overflow.toInt())

        return slice
    }

    fun clearBuffer() {
        buffer = ""
    }
}