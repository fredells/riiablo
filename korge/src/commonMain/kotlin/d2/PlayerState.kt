package d2

class PlayerState() {
    // class
    var clazz: CharacterClass = CharacterClass.Amazon

    // mode
    var mode: CharacterMode = CharacterMode.Attack1

    // gear
    var helmet: HelmetCode? = HelmetCode.BoneHelm
    var armor: ArmorCode? = ArmorCode.Heavy
    var rightHand: Any? = OneHandSwingCode.Axe
    var leftHand: Any? = null
    var shield: ShieldCode? = ShieldCode.BoneShield

    val weaponMode: WeaponMode
        get() = WeaponMode.OneHandSwing

    fun getCode(component: Composite): String {
        return when (component) {
            Composite.Head -> helmet!!.code
            Composite.Torso -> armor!!.code
            Composite.Legs -> armor!!.code
            Composite.RightArm -> armor!!.code
            Composite.LeftArm -> armor!!.code
            Composite.RightHand -> (rightHand!! as OneHandSwingCode).code
            Composite.LeftHand -> TODO()
            Composite.Shield -> shield!!.code
            Composite.Special1 -> armor!!.code
            Composite.Special2 -> armor!!.code
            Composite.Special3 -> TODO()
            Composite.Special4 -> TODO()
            Composite.Special5 -> TODO()
            Composite.Special6 -> TODO()
            Composite.Special7 -> TODO()
            Composite.Special8 -> TODO()
        }
    }
}

enum class Composite(val code: String) {
    Head("HD"),
    Torso("TR"),
    Legs("LG"),
    RightArm("RA"),
    LeftArm("LA"),
    RightHand("RH"),
    LeftHand("LH"),
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