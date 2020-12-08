package codec.COF

import codec.BBox
import codec.DCC.DirectionData
import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.lang.assert
import com.soywiz.korio.stream.*

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class COF(
        var header: Header,
        var layers: Array<Layer?>,
        var keyframe: Array<Keyframe?>,
        var layerOrder: ByteArray
) {
    object Component {
        const val HD: Byte = 0x0 // head
        const val TR: Byte = 0x1 // torso
        const val LG: Byte = 0x2 // legs
        const val RA: Byte = 0x3 // right arm
        const val LA: Byte = 0x4 // left arm
        const val RH: Byte = 0x5 // right hand
        const val LH: Byte = 0x6 // left hand
        const val SH: Byte = 0x7 // shield
        const val S1: Byte = 0x8 // special 1
        const val S2: Byte = 0x9 // special 2
        const val S3: Byte = 0xA // special 3
        const val S4: Byte = 0xB // special 4
        const val S5: Byte = 0xC // special 5
        const val S6: Byte = 0xD // special 6
        const val S7: Byte = 0xE // special 7
        const val S8: Byte = 0xF // special 8
        const val NUM_COMPONENTS = 16
    }

    val box: BBox = BBox()
    var components: Array<Layer?>

    val numLayers: Int
        get() = header.layers
    val numDirections: Int
        get() = header.directions
    val numFramesPerDir: Int
        get() = header.framesPerDir
    val animRate: Int
        get() = header.animRate

    fun getLayer(layer: Int): Layer? {
        return layers[layer]
    }

    fun getComponent(component: Int): Layer? {
        return components[component]
    }

    fun getLayerOrder(d: Int, f: Int, l: Int): Byte {
        val dfl = d * header.framesPerDir * header.layers
        val df = f * header.layers
        return layerOrder[dfl + df + l]
    }

    fun getKeyframe(f: Int): Keyframe? {
        return keyframe[f]
    }

    fun getKeyframeFrame(keyframe: Keyframe?): Int {
        return this.keyframe.indexOf(keyframe)
    }

    @ExperimentalUnsignedTypes
    @ExperimentalStdlibApi
    class Header(input: SyncStream) {
        var layers: Int
        var framesPerDir: Int
        var directions: Int
        var version: Int
        var unknown1: ByteArray
        var minX: Int
        var maxX: Int
        var minY: Int
        var maxY: Int
        var animRate: Int
        var zeros: Int

        companion object {
            const val SIZE = 28
        }

        init {
            layers = input.read() and 0xFF // BufferUtils.readUnsignedByte(buffer)
            framesPerDir = input.read() and 0xFF
            directions = input.read() and 0xFF
            version = input.read() and 0xFF
            unknown1 = input.readBytes(4)
            minX = input.readS32LE()
            maxX = input.readS32LE()
            minY = input.readS32LE()
            maxY = input.readS32LE()
            animRate = input.readU16BE()
            zeros = input.readU16BE()
            // assert(!byteCache.syncStream.hasAvailable)
        }
    }

    class Layer(input: SyncStream) {
        var component: Byte
        var shadow: Byte
        var selectable: Byte
        var overrideTransLvl: Byte
        var newTransLvl: Byte
        var weaponClass: String

        companion object {
            const val SIZE = 9
            const val HD = 0
            const val TR = 1
            const val LG = 2
            const val RA = 3
            const val LA = 4
            const val RH = 5
            const val LH = 6
            const val SH = 7
            const val S1 = 8
            const val S2 = 9
            const val S3 = 10
            const val S4 = 11
            const val S5 = 12
            const val S6 = 13
            const val S7 = 14
            const val S8 = 15
        }

        init {
            component = input.read().toByte()
            shadow = input.read().toByte()
            selectable = input.read().toByte()
            overrideTransLvl = input.read().toByte()
            newTransLvl = input.read().toByte()
            weaponClass = input.readString(4) // BufferUtils.readString2(buffer, 4)
            val x = ""
            // assert(!buffer.syncStream.hasAvailable)
        }
    }

    enum class Keyframe {
        NONE, ATTACK, MISSILE, SOUND, SKILL;

        companion object {
            fun fromInteger(i: Int): Keyframe {
                return when (i) {
                    0 -> NONE
                    1 -> ATTACK
                    2 -> MISSILE
                    3 -> SOUND
                    4 -> SKILL
                    else -> throw Exception("$i does not map to any keyframe constant!")
                }
            }
        }
    }

    companion object {
        private const val TAG = "COF"
        private const val DEBUG = !true
        private const val DEBUG_LAYERS = DEBUG && true
        fun parse(cof: String): Array<String> {
            return arrayOf(
                    cof.substring(0, 2),
                    cof.substring(2, 4),
                    cof.substring(4)
            )
        }

        suspend fun loadFromFile(handle: VfsFile): COF {
            return loadFromStream(handle.readAsSyncStream())
        }

        fun loadFromStream(input: SyncStream, size: Int = -1): COF {
            return try {
                val header = Header(input)
                val layers = arrayOfNulls<Layer>(header.layers)
                for (l in 0 until header.layers) {
                    layers[l] = Layer(input)
                }
                val keyframesSize = if (size == 42 && header.layers == 1 && header.directions == 1 && header.framesPerDir == 1) {
                    4
                } else {
                    header.framesPerDir
                }
                val keyframes = arrayOfNulls<Keyframe>(header.framesPerDir)
                for (f in 0 until header.framesPerDir) {
                    keyframes[f] = Keyframe.fromInteger(input.read())
                }
                val numLayers = header.directions * header.framesPerDir * header.layers
                val layerOrder: ByteArray = input.readAvailable()
                assert(input.available == 0L)
                COF(header, layers, keyframes, layerOrder)
            } catch (e: Exception) {
                throw Exception("Couldn't load COF: $e")
            }
        }
    }

    init {
        box.xMin = header.minX
        box.yMin = header.minY
        box.xMax = header.maxX
        box.yMax = header.maxY
        box.width = box.xMax - box.xMin + 1
        box.height = box.yMax - box.yMin + 1
        components = arrayOfNulls(16)
        for (layer in layers) {
            components[layer!!.component.toInt()] = layer
        }
    }
}

