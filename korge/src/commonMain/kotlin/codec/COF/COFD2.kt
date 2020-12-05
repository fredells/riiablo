package codec.COF

import codec.DCC.ByteCache
import com.soywiz.korio.lang.assert
import com.soywiz.korio.stream.SyncInputStream

/*
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class COFD2(private var entries: Array<Entry>) {
    var trie: Trie<String, COF>
    val numEntries: Int
        get() = entries.size

    fun lookup(cof: String): COF {
        return trie.get(cof.toLowerCase())
    }

    class Entry(input: SyncInputStream) {
        var header1: Int
        var header2: Int
        var cofSize: Int
        var cofName: String
        var header3: Int
        var cof: COF

        companion object {
            const val HEADER_SIZE = 24
        }

        init {
            val header: ByteCache()
            header1 = header.getInt()
            header2 = header.getInt()
            cofSize = header.getInt()
            cofName = com.riiablo.util.BufferUtils.readString2(header, 8)
            header3 = header.getInt()
            assert(!header.hasRemaining())

            // TODO: BoundedInputStream available proxies in.available and not the length of cofSize, but
            //       a marker is needed anyways to tell COF to load special.
            cof = COF.loadFromStream(org.apache.commons.io.input.BoundedInputStream(input, cofSize), cofSize)
        }
    }

    companion object {
        private const val TAG = "COFD2"
        private const val DEBUG = true
        private val DEBUG_ENTRIES = COFD2.Companion.DEBUG && false
        fun loadFromFile(handle: FileHandle): COFD2 {
            return COFD2.Companion.loadFromStream(handle.read())
        }

        fun loadFromStream(`in`: java.io.InputStream): COFD2 {
            return try {
                var i = 0
                val entries: utils.Array<COFD2.Entry> = utils.Array(1024)
                while (`in`.available() > 0) {
                    val entry: COFD2.Entry = COFD2.Entry(`in`)
                    entries.add(entry)
                    if (entry.header1 != -1 || entry.header2 != 0 || entry.header3 != -1) {
                        throw Exception("Invalid entry headers: $entry")
                    }
                }
                COFD2(entries)
            } catch (t: Throwable) {
                throw Exception("Couldn't load D2 from stream.", t)
            }
        }
    }

    init {
        trie = PatriciaTrie()
        for (entry in entries) {
            trie.put(entry.cofName.toLowerCase(), entry.cof)
        }
    }
}*/
