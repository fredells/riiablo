import codec.DCC
import codec.Palette
import com.soywiz.klock.milliseconds
import com.soywiz.korge.Korge
import com.soywiz.korge.view.SpriteAnimation
import com.soywiz.korge.view.sprite
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.PaletteColorFormat
import com.soywiz.korim.color.RgbaArray
import com.soywiz.korim.format.readBitmapImageData
import com.soywiz.korio.file.std.resourcesVfs

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
    val palette = resourcesVfs["palettes/act1.dat"]
    DCC.palette = Palette.rgbaArray(palette)
    val image = resourcesVfs["AndarielFlameDeath.dcc"].readBitmapImageData(DCC).frames

    addChild(
            sprite(
                    SpriteAnimation(image.map { it.bitmap.slice() })
            ).apply {
                playAnimationLooped(spriteDisplayTime = 500.milliseconds)
            }
    )
}