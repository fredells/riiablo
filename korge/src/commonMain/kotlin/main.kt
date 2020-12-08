import codec.COF.COF
import codec.DCC.DirectionData
import codec.DCC.Palette
import codec.DCC.getDirectionData
import codec.DCC.loadDirection
import com.soywiz.korev.Key
import com.soywiz.korev.keys
import com.soywiz.korge.Korge
import com.soywiz.korge.view.BlendMode
import com.soywiz.korge.view.SpriteAnimation
import com.soywiz.korge.view.scale
import com.soywiz.korge.view.sprite
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.color.Colors
import com.soywiz.korio.async.runBlockingNoSuspensions
import com.soywiz.korio.file.std.resourcesVfs
import d2.CharacterMode
import d2.Composite
import d2.PlayerState

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors.BLACK) {
    scale(2, 2)

    val act1Palette = Palette.rgbaArray(resourcesVfs["palettes/act1.dat"])

    val missiles = resourcesVfs["miss"].listNames().map {
        resourcesVfs["miss/$it"].readAsSyncStream().getDirectionData()
    }

    // A1 to {
    //  dirIndex to listOf<Layers>(
    //      listOf<DirectionData>() -> getDirectionData(index, palette)
    //  )
    // }
    val amazonAnimations = mutableMapOf<CharacterMode, MutableMap<Int, List<List<DirectionData>>>>() // List<List<DirectionData>> == all one for each layer
    val attackCOF = COF.loadFromFile(resourcesVfs["char/AM/COF/ama11hs.cof"])

    val player = PlayerState()

    // need to draw sprites in the correctOrder with proper x y offset
    blendMode = BlendMode.ALPHA
    /*repeat(attackCOF.header.directions) { dirIndex ->
        // for each direction > for each frame > combine layers according to COF > save the composite bitmap

        val layerOrder = attackCOF.getLayerOrder(dirIndex, layerIndex, frameIndex)

        amazonAnimations.getOrPut(CharacterMode.Attack1, { mutableMapOf() })[dirIndex] = componentAnimations
    }*/

    val componentAnimations = mutableListOf<List<DirectionData>>()
    suspend fun equip() {
        componentAnimations.clear()
        repeat(attackCOF.layers.size) { layerIndex ->
            val layer = attackCOF.layers[layerIndex]
            val component = Composite.values()[layer!!.component.toInt()]
            val classCode = player.clazz.code
            val file = resourcesVfs["char/" +
                    "$classCode/" +
                    "${component.code}/" +
                    classCode +
                    component.code +
                    player.getCode(component) +
                    player.mode.code +
                    "${player.weaponMode.code}.dcc"
            ]
            val componentFrames = file.readAsSyncStream().getDirectionData()
            componentAnimations.add(componentFrames)
        }
    }


    // [dir][bmp]
    /*val compositeBitmaps = Array(attackCOF.numDirections) {
        Array<Bitmap>(attackCOF.numFramesPerDir) {
            Bitmap8(attackCOF.box.width, attackCOF.box.height)
        }
    }

    componentAnimations.forEachIndexed { layerIndex, directionData ->
        repeat(attackCOF.numDirections) { dirIndex ->
            // val layerOrder = attackCOF.getLayerOrder(dirIndex, frameIndex, layerIndex)
            val frames = directionData.loadDirection(dirIndex, act1Palette)
            repeat(frames.frames.size) { frameIndex ->
                val frame = frames.frames[frameIndex]
                frame.bitmap.copy(
                        srcX = 0,
                        srcY = 0,
                        dst = compositeBitmaps[dirIndex][frameIndex],
                        dstX = 0,
                        dstY = 0,
                        width = frame.width,
                        height = frame.height
                )
            }
        }
    }*/

    var dirIndex = 0
    fun render() {
        removeChildren()
        componentAnimations.forEach {
            // need to calculate draw order and offset
            val component = it.loadDirection(dirIndex, act1Palette)
            addChild(
                    sprite(SpriteAnimation(component.frames.map { it.bitmap.slice() }))
                            // .xy(it[dirIndex].xMin, it[dirIndex].yMin)
                            .apply {
                                x += 100 + it[dirIndex].xMin
                                y += 100 + it[dirIndex].yMin
                                playAnimationLooped()
                            }
            )
        }
    }
    render()

    // character class will load all COF files and keep in memory
    // then load and cache 1 version of each mode (attack, walk, etc) * each direction (cherry pick relevant ones)
    // when weapon changes re load and cache modes


    /*addChild(
            sprite(SpriteAnimation(image.map { it.bitmap.slice() }))
                    .apply {
                        playAnimationLooped(spriteDisplayTime = 60.milliseconds)
                    }
    )*/

    var spriteIndex = 0
    var directionIndex = 0

    /*fun render() {
        removeChildren()
        sprite(
                SpriteAnimation(
                        missiles[spriteIndex].loadDirection(directionIndex, act1Palette)
                                .frames.map { it.bitmap.slice() }
                )
        ).apply {
            playAnimationLooped()
        }
    }

    render()*/

    keys {
        down(Key.DOWN) {
            directionIndex = 0
            spriteIndex++
            player.randomize()
            runBlockingNoSuspensions {
                equip()
            }
            render()
        }

        down(Key.UP) {
            directionIndex = 0
            spriteIndex--
            player.randomize()
            runBlockingNoSuspensions {
                equip()
            }
            render()
        }

        down(Key.RIGHT) {
            directionIndex++
            dirIndex++
            render()
        }

        down(Key.LEFT) {
            directionIndex--
            dirIndex--
            render()
        }
    }
}
