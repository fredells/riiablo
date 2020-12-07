import codec.COF.COF
import codec.DCC.DirectionData
import codec.DCC.Palette
import codec.DCC.getDirectionData
import codec.DCC.loadDirection
import com.soywiz.korev.Key
import com.soywiz.korev.keys
import com.soywiz.korge.Korge
import com.soywiz.korge.view.SpriteAnimation
import com.soywiz.korge.view.sprite
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.color.Colors
import com.soywiz.korio.file.std.resourcesVfs
import d2.CharacterMode
import d2.Composite
import d2.PlayerState

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors.BLACK) {
    val act1Palette = Palette.rgbaArray(resourcesVfs["palettes/act1.dat"])
    scaleX = 3.0
    scaleY = 3.0

    val missiles = resourcesVfs["miss"].listNames().map {
        resourcesVfs["miss/$it"].readAsSyncStream().getDirectionData()
    }

    // A1 to {
    //  dirIndex to listOf<Layers>(
    //      listOf<DirectionData>() -> getDirectionData(index, palette)
    //  )
    // }
    val amazonAnimations = mutableMapOf<CharacterMode, MutableMap<Int, List<List<DirectionData>>>>()
    val attackCOF = COF.loadFromFile(resourcesVfs["char/AM/COF/ama11hs.cof"])

    val player = PlayerState()

    // need to draw sprites in the correctOrder with proper x y offset
    repeat(attackCOF.header.directions) { dirIndex ->
        // for each direction > for each frame > combine layers according to COF > save t
        val componentAnimations = mutableListOf<List<DirectionData>>()

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

        amazonAnimations.getOrPut(CharacterMode.Attack1, { mutableMapOf() })[dirIndex] = componentAnimations
    }

//    var index = 0
//    fun render() {
//        removeChildren()
//        amazonAnimations[CharacterMode.Attack1]!![index]!!.forEach {
//            addChild(
//                    sprite(SpriteAnimation(it.map { it.bitmap.slice() }))
//                            .apply {
//                                playAnimationLooped(spriteDisplayTime = 60.milliseconds)
//                            }
//            )
//        }
//    }
//     render()

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

    fun render() {
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

    render()

    keys {
        down(Key.DOWN) {
            directionIndex = 0
            spriteIndex++
            render()
        }

        down(Key.UP) {
            directionIndex = 0
            spriteIndex--
            render()
        }

        down(Key.RIGHT) {
            directionIndex++
            render()
        }

        down(Key.LEFT) {
            directionIndex--
            render()
        }
    }
}
