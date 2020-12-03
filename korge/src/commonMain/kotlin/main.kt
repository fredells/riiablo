import codec.DCC
import codec.Palette
import com.soywiz.klock.milliseconds
import com.soywiz.korge.Korge
import com.soywiz.korge.view.SpriteAnimation
import com.soywiz.korge.view.sprite
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.readBitmapImageData
import com.soywiz.korio.file.std.resourcesVfs

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
    val palette = resourcesVfs["palettes/act1.dat"]
    DCC.palette = Palette.rgbaArray(palette).apply {
        // can i transform the colors here?
    }
    val image = resourcesVfs["miss/DiabloLightning.dcc"].readBitmapImageData(DCC).frames

    /*val animations = resourcesVfs["miss"].listNames().map {
        resourcesVfs["miss/$it"].readBitmapImageData(DCC).frames
    }*/

    addChild(
            sprite(SpriteAnimation(image.map { it.bitmap.slice() }))
                    .apply {
                        playAnimationLooped(spriteDisplayTime = 60.milliseconds)
                    }
    )

    /*var index = 0
    keys {
        down(Key.DOWN) {
            removeChildren()
            sprite(SpriteAnimation(animations[++index].map { it.bitmap.slice() }))
                    .apply {
                        playAnimationLooped(spriteDisplayTime = 60.milliseconds)
                    }
        }
        down(Key.UP) {
            removeChildren()
            sprite(SpriteAnimation(animations[--index].map { it.bitmap.slice() }))
                    .apply {
                        playAnimationLooped(spriteDisplayTime = 60.milliseconds)
                    }
        }
    }*/
}