import codec.COF.COF
import codec.DCC.DCC
import codec.DCC.Palette
import com.soywiz.korge.Korge
import com.soywiz.korge.view.SpriteAnimation
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
    val image = resourcesVfs["miss/curseCast.dcc"].readBitmapImageData(DCC).frames

    val animations = resourcesVfs["miss"].listNames().map {
        resourcesVfs["miss/$it"].readBitmapImageData(DCC).frames
    }

    // char/CharacterClass/COF/<TOKEN><MODE><WEAPON>.cof
    val am = resourcesVfs["char/AM/COF/ama11hs.cof"]
    val cof = COF.loadFromFile(am)

    // load sprites in the correctOrder
    var attack: SpriteAnimation? = null

    

    val x = ""

    // character class will load all COF files and keep in memory
    // then load and cache 1 version of each mode (attack, walk, etc)
    // when weapon changes re load and cache modes


    /*addChild(
            sprite(SpriteAnimation(image.map { it.bitmap.slice() }))
                    .apply {
                        playAnimationLooped(spriteDisplayTime = 60.milliseconds)
                    }
    )*/

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

enum class Composite(val code: String) {
    Head("HD"),
    Torso("TR"),
    Legs("LG"),
    RightArm("RA"),
    LeftArm("LA"),
    Shield("SH"),
    Special1("S1"),
    Special2("S2"),
    Special3("S3"),
    Special4("S4"),
    Special5("S5"),
    Special6("S6"),
    Special7("S7"),
    Special8("S8")
}

enum class WeaponMode(val code: String) {
    HandToHand("HTH"),
    Bow("BOW"),
    OneHandSwing("1HS"),
    OneHandThrust("1HT"),
    Staff("STF"),
    TwoHandSwing("2HS"),
    TwoHandThrust("2HT"),
    CrossBow("XBW"),
    LeftJabRightSwing("1JS"),
    LeftJabRightThrust("1JT"),
    LeftSwingRightSwing("1SS"),
    LeftSwingRightThrust("1ST"),
    SingleClaw("HT1"),
    DualClaw("HT2")
}

enum class CharacterClass(val code: String) {
    // "AM", "SO", "NE", "PA", "BA", "DZ", "AI"
    Amazon("AM"),
    Sorceress("SO"),
    Paladin("PA"),
    Barbarian("BA"),
    Druid("DZ"),
    Assassin("AI")
}

enum class CharacterMode(val code: String) {
    Death("DT"),
    Dead("DD"),
    TownNeutral("TN"),
    Neutral("NU"),
    Cast("SC"),
    Kick("KK"),
    Attack1("A1"),
    Attack2("A2"),
    Throw("TH"),
    Skill1("S1"),
    TownWalk("TW"),
    Walk("WL"),
    Run("RN"),
    Block("BL"),
    GetHit("GH")
}

enum class OneHandSwingCode(val code: String) {
    Axe("AXE"),
    BroadSword("BSD"),
    BoneWand("BWN"),
    Club("CLB"),
    CrystalSword("CRS"),
    Flail("FLA"),
    Falchion("FLC"),
    Hatchet("HAX"),
    LongSword("LSD"),
    Mace("MAC"),
    Scimitar("SCM"),
    ShortSword("SSD"),
    WarHammer("WHM"),
    Wand("WND"),
    YewWand("YWN"),
}

enum class TwoHandSwingCode(val code: String) {
    Claymore("CLM"),
    GreatSword("GSD")
}

enum class TwoHandThrustCode(val code: String) {
    Spetum("BRN"),
    Pike("PIK"),
    Spear("SPR"),
    Trident("TRI")
}

enum class OneHandThrustCode(val code: String) {
    Dagger("DGR"),
    Kriss("DIR"),
    Glaive("GLV"),
    PoisonLarge("GPL"),
    PoisonSmall("GPS"),
    Javelion("JAV"),
    FireLarge("OPL"),
    FireSmall("OPS"),
    Pilum("PIL")
}

enum class BowCode(val code: String) {
    LongWarBow("LBB"),
    LongBow("LBW"),
    ShortWarBow("SBB"),
    ShortBow("LBW")
}

enum class ShieldCode(val code: String) {
    BoneShield("BSH"),
    Buckler("BUC"),
    KiteShield("KIT"),
    LargeShield("LRG"),
    SpikeShield("SPK"),
    TowerShield("TOW"),
}

enum class StaffCode(val code: String) {
    Staff("BST"),
    Bardiche("BTX"),
    LongStaff("CST"),
    GreatAxe("GIX"),
    Halberd("HAL"),
    BattleAxe("LAX"),
    SpecialStaff("LST"),
    Maul("MAU"),
    Polearm("PAX"),
    Scythe("SCY"),
    WarStaff("SST")
}

enum class CrossbowCode(val code: String) {
    LightCrossbow("LXB"),
    HeavyCrossbow("HXB")
}

enum class HelmetCode(val code: String) {
    BoneHelm("BHM"),
    Cap("CAP"),
    Crown("CRN"),
    FullHelm("FHL"),
    GreatHelm("GHM"),
    Helm("HLM"),
    Circlet("LIT"),
    Mask("MSK"),
    SkullCap("SKP"),
}

enum class ArmorCode(val code: String) {
    Light("LIT"),
    Medium("MED"),
    Heavy("HVY")
}