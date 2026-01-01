package com.mizi.miztinker.modifier.register;

import com.csdy.tcondiadema.modifier.CommonDiademaModifier;
import com.csdy.tcondiadema.modifier.DiademaModifier;
import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.diadema.DiademaRegister;
import com.mizi.miztinker.modifier.modifiers.*;
import com.mizi.miztinker.modifier.modifiers.base.*;
import com.mizi.miztinker.sounds.MiztinkerSounds;
import net.minecraftforge.fml.ModList;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;


public class MiztinkerModifiers {
    public static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(miztinker.MODID);
    public static final StaticModifier<Ironman> IRONMAN = MODIFIERS.register("ironman", Ironman::new);

    //血液蝙蝠
    public static StaticModifier<?> BLOOD_BAT  = null;

    //永恒防晒霜
    public static StaticModifier<?> ETERNALSUNSCREEN  = null;

    //德古拉之血
    public static StaticModifier<?> DRACULASBLOOD  = null;

    //永恒之血

    public static StaticModifier<?> ETERNALBOOLD  = null;

    //极巨化
    public static StaticModifier<Dynamax> DYNAMAX = MODIFIERS.register("dynamax", Dynamax::new);

    //极巨化
    public static final StaticModifier<Dynamax_armor> DYNAMAX_ARMOR = MODIFIERS.register("dynamax_armor", Dynamax_armor::new);

    public static StaticModifier<?> BLOOD_WING = null;

    //蝎毒
    public static final StaticModifier<Venom> VENOM = MODIFIERS.register("venom", Venom::new);

    //毒素激活
    public static final StaticModifier<Venom_damage> VENOM_DAMAGE = MODIFIERS.register("venom_damage", Venom_damage::new);

    //氯
    public static final StaticModifier<Chlorine> CHLORINE = MODIFIERS.register("chlorine", Chlorine::new);

    //钠
    public static final StaticModifier<Sodium> SODIUM = MODIFIERS.register("sodium", Sodium::new);


    public static StaticModifier<XXkiller> XXKILLER = MODIFIERS.register("xxkiller", XXkiller::new);

    //飞行
    public static final StaticModifier<Fly> FLY = MODIFIERS.register("fly", Fly::new);



    public static final StaticModifier<Chloroplast> CHLOROPLAST_STATIC_MODIFIER = MODIFIERS.register("chloroplast", Chloroplast::new);

    //血液护盾
    public static StaticModifier<?> BLOODSHIDID = null;

    //水龙之力
    public static final StaticModifier<Water_Power> WATER_POWER = MODIFIERS.register("water_power", Water_Power::new);

    //熔岩大爆弹!
    public static final StaticModifier<Lava_explosion> LAVA_EXPLOSION = MODIFIERS.register("lava_explosion", Lava_explosion::new);

    //mega极巨化磁性
    public static final StaticModifier<Mega_big_Magnetic> MEGA_BIG_MAGNETIC = MODIFIERS.register("mega_big_magnetic", Mega_big_Magnetic::new);

    //强化呼吸系统
    public static final StaticModifier<Strengthen_breathing> STRENGTHEN_BREATHING = MODIFIERS.register("strengthen_breathing", Strengthen_breathing::new);

    //饥渴进化
    public static final StaticModifier<Ravenous> RAVENOUS = MODIFIERS.register("ravenous", Ravenous::new);

    //狂暴
    public static final StaticModifier<Berserk> BERSERK = MODIFIERS.register("berserk", Berserk::new);

    //心脏掌握
    public static final StaticModifier<Grasp_Heart> GRASP_HEART = MODIFIERS.register("grasp_heart", Grasp_Heart::new);

    //骸骨障壁
    public static StaticModifier<?> WALL_OF_SKELETON  = null;
    //噬魂
    public static final StaticModifier<SoulEat> SOUL_EAT = MODIFIERS.register("souleat", SoulEat::new);

    //取消后腰
    public static final StaticModifier<SB_cancel> SB_CANCEL = MODIFIERS.register("sb_cancel", SB_cancel::new);


    public static StaticModifier<?> STREDGEUNIVERSE = null;

    //取消后腰
    public static StaticModifier<?> GATLING_SWORD = null;

    //欺骗恶魔
    public static final StaticModifier<DeceiveDevil> DECEIVE_DEVIL = MODIFIERS.register("deceivedevil", DeceiveDevil::new);

    //圆刃
    public static StaticModifier<?> CIRCLE_SLASH = null;

    //捕捉
    public static final StaticModifier<Capturin> CAPTURIN = MODIFIERS.register("capturin", Capturin::new);

    //龙贪
    public static final StaticModifier<Dragon_Greedy> DRAGON_GREEDY = MODIFIERS.register("dragon_greedy", Dragon_Greedy::new);

    //原子斩
    public static StaticModifier<?> ATOMSLASH = null;

    //便携式睡袋
    public static final StaticModifier<SleepInstant> SLEEP_INSTANT = MODIFIERS.register("sleepinstant", SleepInstant::new);

    //死神之眼
    public static final StaticModifier<Death_eye> DEATH_EYE = MODIFIERS.register("death_eye", Death_eye::new);

    //死神之眼
    public static final StaticModifier<Bfg2000 > BFG_2000 = MODIFIERS.register("bfg2000", Bfg2000 ::new);

    //灵魂汲取
    public static final StaticModifier<SoulDrain > SOULDRAIN = MODIFIERS.register("souldrain", SoulDrain ::new);

    //怨念爆破
    public static final StaticModifier<RetributionExplosion > RETRIBUTIONEXPLOSION = MODIFIERS.register("retributionexplosion", RetributionExplosion ::new);

    //动能冲击
    public static final StaticModifier<KineticAmplifier > KINETICAMPLIFIER = MODIFIERS.register("kineticamplifier", KineticAmplifier ::new);

    //动能冲击
    public static StaticModifier<?> VEXSUMMONER = null;

    //动能冲击
    public static final StaticModifier<Entropy_Decay > ENTROPY_DECAY = MODIFIERS.register("entropy_decay", Entropy_Decay ::new);

    //空灵矿工
    public static final StaticModifier<EtherealMiner > ETHEREALMINER = MODIFIERS.register("etherealminer", EtherealMiner ::new);

    //细语
    public static final StaticModifier<Whispering > WHISPERING_STATIC_MODIFIER = MODIFIERS.register("whispering", Whispering ::new);

    //伊格尼斯领域
    public static final StaticModifier<IgnisterField > IGNISTER_FIELD_STATIC_MODIFIER = MODIFIERS.register("ignisterfield", IgnisterField ::new);

    //配重
    public static final StaticModifier<Configuration > configurationStaticModifier = MODIFIERS.register("configuration", Configuration ::new);


    //赤石
    public static final StaticModifier<Eatstone > EATSTONE_STATIC_MODIFIER = MODIFIERS.register("eatstone", Eatstone ::new);

    //赤石
    public static final StaticModifier<Emerald_splash > EMERALD_SPLASH_STATIC_MODIFIER = MODIFIERS.register("emerald_splash", Emerald_splash ::new);

    public static final StaticModifier<BaseToolMusic> DOOM_GUY =
            MODIFIERS.register("doom_guy", () -> new BaseToolMusic(() -> MiztinkerSounds.DOOM_GUY.get()));

    public static final StaticModifier<BaseToolMusic> ULTRAMAN =
            MODIFIERS.register("ultraman", () -> new BaseToolMusic(() -> MiztinkerSounds.ULTRAMAN.get()));

    public static final StaticModifier<BaseToolMusic> GUANYU =
            MODIFIERS.register("guanyu", () -> new BaseToolMusic(() -> MiztinkerSounds.GUANYU.get()));

    public static final StaticModifier<BaseToolMusic> KNIGHT_OF_NIGHT =
            MODIFIERS.register("knight_of_night", () -> new BaseToolMusic(() -> MiztinkerSounds.KNIGHT_OF_NIGHT.get()));

    public static StaticModifier<AwakenDoomGuy> awakenDoomGuyStaticModifier =MODIFIERS.register("awaken_doomguy", AwakenDoomGuy::new);

    public static final StaticModifier<AwakenUltraman> AWAKEN_ULTRAMAN_STATIC_MODIFIER = MODIFIERS.register("awaken_ultraman", AwakenUltraman::new);

    public static StaticModifier<?> GREY_MATTER_STATIC_MODIFIER = MODIFIERS.register("greymatter", GreyMatter::new);

    public static final StaticModifier<WoundEffectAttack > WOUND_EFFECT_ATTACK_STATIC_MODIFIER = MODIFIERS.register("woundeffectattack", WoundEffectAttack ::new);

    public static final StaticModifier<Tiga > TIGA_STATIC_MODIFIER = MODIFIERS.register("tiga", Tiga ::new);

    public static final StaticModifier<AntonBloodline > ANTON_BLOODLINE_STATIC_MODIFIER = MODIFIERS.register("antonbloodline", AntonBloodline ::new);

    public static final StaticModifier<EnchantedGold > ENCHANTED_GOLD_STATIC_MODIFIER = MODIFIERS.register("enchantedgold", EnchantedGold ::new);

    public static final StaticModifier<Plumber > PLUMBER_STATIC_MODIFIER = MODIFIERS.register("plumber", Plumber ::new);

    public static final StaticModifier<DamageReductionModifier> ALLOYING =
            MODIFIERS.register(
                    "alloying",
                    () -> new DamageReductionModifier(0.07f)
            );

    public static final StaticModifier<DamageReductionModifier> HARDENED =
            MODIFIERS.register(
                    "hardened",
                    () -> new DamageReductionModifier(0.15f)
            );

    public static final StaticModifier<Infinitum> INFINITUM_STATIC_MODIFIER = MODIFIERS.register("infinitum", Infinitum::new);

    public static final StaticModifier<BlockingDamage> BLOCKING_DAMAGE_STATIC_MODIFIER = MODIFIERS.register("blockingdamage", BlockingDamage::new);

    public static final StaticModifier<BornOfStorm> BORN_OF_STORM_STATIC_MODIFIER = MODIFIERS.register("bronofstorm", BornOfStorm::new);

    public static final StaticModifier<SteelHowForged> STEEL_HOW_FORGED_STATIC_MODIFIER = MODIFIERS.register("steelhowforged", SteelHowForged::new);

    public static final StaticModifier<Astral> ASTRAL_STATIC_MODIFIER = MODIFIERS.register("astral", Astral::new);

    public static final StaticModifier<Elemental> ELEMENTAL_STATIC_MODIFIER = MODIFIERS.register("elemental", Elemental::new);

    public static final StaticModifier<Terrafirma> TERRAFIRMA_STATIC_MODIFIER = MODIFIERS.register("terrafirma", Terrafirma::new);

    public static StaticModifier<?> BLOODMARY_STATIC_MODIFIER = null;

    public static final StaticModifier<LifeSteal> LIFE_STEAL_STATIC_MODIFIER = MODIFIERS.register("life_steal", LifeSteal::new);

    public static final StaticModifier<LifeStealArmor> LIFE_STEAL_ARMOR_STATIC_MODIFIER = MODIFIERS.register("life_steal_armor", LifeStealArmor::new);

    public static final StaticModifier<TradeOff> TRADE_OFF_STATIC_MODIFIER = MODIFIERS.register("trade_off", TradeOff::new);

    public static final StaticModifier<HaoransCult> HAORANS_CULT_STATIC_MODIFIER = MODIFIERS.register("haorans_cult", HaoransCult::new);

    public static final StaticModifier<HellFire> HELL_FIRE_STATIC_MODIFIER = MODIFIERS.register("hell_fire", HellFire::new);

    public static final StaticModifier<MythicalPush> MYTHICAL_PUSH_STATIC_MODIFIER = MODIFIERS.register("mythical_push", MythicalPush::new);

    public static final StaticModifier<HellFireArmor> HELL_FIRE_ARMOR_STATIC_MODIFIER = MODIFIERS.register("hell_fire_armor", HellFireArmor::new);

    public static final StaticModifier<MythicalPushArmor> MYTHICAL_PUSH_ARMOR_STATIC_MODIFIER = MODIFIERS.register("mythical_push_armor", MythicalPushArmor::new);

    public static final StaticModifier<WizardFlight> WIZARD_FLIGHT_STATIC_MODIFIER = MODIFIERS.register("wizardflight", WizardFlight::new);

    public static final StaticModifier<ScorchedBone> SCORCHED_BONE_STATIC_MODIFIER = MODIFIERS.register("scorchedbone", ScorchedBone::new);

    public static final StaticModifier<TimeTyrant> TIME_TYRANT_STATIC_MODIFIER = MODIFIERS.register("timetyrant", TimeTyrant::new);

    public static final StaticModifier<Celestial_Aura> CELESTIAL_AURA_STATIC_MODIFIER = MODIFIERS.register("celestial_aura", Celestial_Aura::new);

    public static final StaticModifier<CelestialStrike> CELESTIAL_STRIKE_STATIC_MODIFIER = MODIFIERS.register("celestial_strike", CelestialStrike::new);

    public static StaticModifier<?> LUNGS_PAST_STATIC_MODIFIER = null;

    public static StaticModifier<?> EMC_TORRENT = null;

    public static StaticModifier<?> TINKERSCROWN = null;

    public static StaticModifier<?> LOVESTONE = null;

    public static final StaticModifier<DamageModifier> SEVER = MODIFIERS.register("sever", () -> new DamageModifier(0.5F, 0));

    public static final StaticModifier<Triple_Seven> TRIPLE_SEVEN_STATIC_MODIFIER = MODIFIERS.register("triple_seven", Triple_Seven::new);

    public static final StaticModifier<EnderManSoul> ENDER_MAN_SOUL_STATIC_MODIFIER = MODIFIERS.register("ender_man_soul", EnderManSoul::new);

    public static final StaticModifier<UnDeadSoul> UN_DEAD_SOUL_STATIC_MODIFIER = MODIFIERS.register("undead_soul", UnDeadSoul::new);

    public static final StaticModifier<Derakkuma> DERAKKUMA_STATIC_MODIFIER = MODIFIERS.register("derakkuma", Derakkuma::new);

    public static final StaticModifier<Refill> REFILL_STATIC_MODIFIER = MODIFIERS.register("refill", Refill::new);

    public static final StaticModifier<AwakenKnight_of_Night> AWAKEN_KNIGHT_OF_NIGHT_STATIC_MODIFIER = MODIFIERS.register("awakenknight_of_night", AwakenKnight_of_Night::new);

    public static final StaticModifier<EZ_Constant> EZ_CONSTANT_STATIC_MODIFIER = MODIFIERS.register("ez_constant", EZ_Constant::new);

    public static final StaticModifier<GhostSoul> GHOST_SOUL_STATIC_MODIFIER = MODIFIERS.register("ghostsoul", GhostSoul::new);

    public static final StaticModifier<Agni> AGNI_STATIC_MODIFIER = MODIFIERS.register("agni", Agni::new);

    public static final StaticModifier<RainbowLife> RAINBOW_LIFE_STATIC_MODIFIER = MODIFIERS.register("rainbowlife", RainbowLife::new);

    public static final StaticModifier<FutureDiary> FUTURE_DIARY_STATIC_MODIFIER = MODIFIERS.register("futurediary", FutureDiary::new);

    public static final StaticModifier<DropFountain> DROP_FOUNTAIN_STATIC_MODIFIER = MODIFIERS.register("dropfountain", DropFountain::new);

    public static final StaticModifier<Flattening> FLATTENING_STATIC_MODIFIER = MODIFIERS.register("flattening", Flattening::new);

    public static final StaticModifier<OverHeal> OVER_HEAL_STATIC_MODIFIER = MODIFIERS.register("overheal", OverHeal::new);

    public static final StaticModifier<EntropyStrike> ENTROPY_STRIKE_STATIC_MODIFIER = MODIFIERS.register("entropystrike", EntropyStrike::new);

    public static final StaticModifier<SuperLollipop> SUPER_LOLLIPOP_STATIC_MODIFIER = MODIFIERS.register("superlollipop", SuperLollipop::new);

    public static final StaticModifier<Tasty> TASTY_STATIC_MODIFIER = MODIFIERS.register("tasty", Tasty::new);

    public static final StaticModifier<VoidImmunity> VOID_IMMUNITY_STATIC_MODIFIER = MODIFIERS.register("void_immunity", VoidImmunity::new);

    public static final StaticModifier<Ubbo_Sathla> UBBO_SATHLA_STATIC_MODIFIER = MODIFIERS.register("ubbo_sathla", Ubbo_Sathla::new);

    public static final StaticModifier<Death_Note> DEATH_NOTE_STATIC_MODIFIER = MODIFIERS.register("death_note", Death_Note::new);

    public static final StaticModifier<All_Perfect> ALL_PERFECT_STATIC_MODIFIER = MODIFIERS.register("all_perfect", All_Perfect::new);

    public static final StaticModifier<Mercy> MERCY_STATIC_MODIFIER = MODIFIERS.register("mercy", Mercy::new);

    public static final StaticModifier<AcidicBlood> ACIDIC_BLOOD_STATIC_MODIFIER = MODIFIERS.register("acidic_blood", AcidicBlood::new);

    public static final StaticModifier<Infested> INFESTED_STATIC_MODIFIER = MODIFIERS.register("infested", Infested::new);

    public static final StaticModifier<FireEater> FIRE_EATER_STATIC_MODIFIER = MODIFIERS.register("fire_eater", FireEater::new);

    public static final StaticModifier<FirstBlood> FIRST_BLOOD_STATIC_MODIFIER = MODIFIERS.register("first_blood", FirstBlood::new);

    public static final StaticModifier<Pulverize> PULVERIZE_STATIC_MODIFIER = MODIFIERS.register("pulverize", Pulverize::new);

    public static final StaticModifier<ItsLive> ITS_LIVE_STATIC_MODIFIER = MODIFIERS.register("its_live", ItsLive::new);

    public static final StaticModifier<Adaptation> ADAPTATION_STATIC_MODIFIER = MODIFIERS.register("adaptation", Adaptation::new);

    public static final StaticModifier<Ghostfreak> GHOSTFREAK = MODIFIERS.register("ghostfreak", Ghostfreak::new);

    public static StaticModifier<?> LEARNINGDEVICE = null;

    public static final StaticModifier<BiomeScanner> BIOME_SCANNER_STATIC_MODIFIER = MODIFIERS.register("biomescanner", BiomeScanner::new);

    public static final StaticModifier<Energy_Overdose> ENERGY_OVERDOSE_STATIC_MODIFIER = MODIFIERS.register("energy_overdose", Energy_Overdose::new);

    public static final StaticModifier<Energy_Absorption> ENERGY_ABSORPTION_STATIC_MODIFIER = MODIFIERS.register("energy_absorption", Energy_Absorption::new);

    public static final StaticModifier<Old_Spiky> OLD_SPIKY_STATIC_MODIFIER = MODIFIERS.register("old_spiky", Old_Spiky::new);

    public static final StaticModifier<NBT_Microscope> NBT_MICROSCOPE_STATIC_MODIFIER = MODIFIERS.register("nbt_microscope", NBT_Microscope::new);

    public static final StaticModifier<Undone> UNDONE_STATIC_MODIFIER = MODIFIERS.register("undone", Undone::new);

    public static final StaticModifier<Digitization> DIGITIZATION_STATIC_MODIFIER = MODIFIERS.register("digitization", Digitization::new);

    public static final StaticModifier<Happy_NewYear> HAPPY_NEW_YEAR_STATIC_MODIFIER = MODIFIERS.register("happy_newyear", Happy_NewYear::new);

    public static final StaticModifier<Invert> INVERT_STATIC_MODIFIER = ModList.get().isLoaded("tinkerslevellingaddon") ? MODIFIERS.register("invert",Invert::new) : null;

    public static final StaticModifier<Randomizer_Launcher> RANDOMIZER_LAUNCHER_STATIC_MODIFIER = ModList.get().isLoaded("projecte") ? MODIFIERS.register("randomizer_launcher",Randomizer_Launcher::new) : null;

    public static final StaticModifier<Arcana> ARCANA_STATIC_MODIFIER = ModList.get().isLoaded("projecte") ? MODIFIERS.register("arcana",Arcana::new) : null;

    public static final StaticModifier<Double_Evolution_Pill> DOUBLE_EVOLUTION_PILL_STATIC_MODIFIER = ModList.get().isLoaded("tinkerslevellingaddon") ? MODIFIERS.register("double_evolution_pill",Double_Evolution_Pill::new) : null;

    public static final StaticModifier<StructureScanner> STRUCTURE_SCANNER_STATIC_MODIFIER = MODIFIERS.register("structurescanner", StructureScanner::new);

    public static final StaticModifier<NoLevelsModifier> BEDROCK_DESTROYER = MODIFIERS.register("bedrock_destr0yer", NoLevelsModifier::new);

    public static final StaticModifier<Loli_Energy> LOLI_ENERGY_STATIC_MODIFIER = MODIFIERS.register("loli_energy", Loli_Energy::new);

    public static final StaticModifier<Lens_Miner> LENS_MINER_STATIC_MODIFIER= MODIFIERS.register("lens_miner", Lens_Miner::new);

    public static final StaticModifier<Real_souleat_realform> REAL_SOULEAT_REALFORM_STATIC_MODIFIER = MODIFIERS.register("real_souleat_realform", Real_souleat_realform::new);

    public static final StaticModifier<BaseHealModifier> SRP_BASE_HEAL_MODIFIER_STATIC_MODIFIER = MODIFIERS.register("srp_heal",() -> new BaseHealModifier(2));

    public static final StaticModifier<BaseMaxHealthModifier> BASE_MAX_HEALTH_MODIFIER_STATIC_MODIFIER = MODIFIERS.register("srp_max_health",() -> new BaseMaxHealthModifier("base_max_health1",10));

    public static final StaticModifier<BaseMaxHealthModifier> ABYSSAL_PROTECTION = MODIFIERS.register("abyssal_protection",() -> new BaseMaxHealthModifier("base_max_health2",30));

    public static final StaticModifier<BaseMaxHealthModifier> HARCADIUM_ARMOR = MODIFIERS.register("harcadium_armor",() -> new BaseMaxHealthModifier("base_max_health3",60));

    public static final StaticModifier<BaseMaxHealthModifier> BED_ARMOR = MODIFIERS.register("bed_armor",() -> new BaseMaxHealthModifier("base_max_health4",1));

    public static final StaticModifier<MaliciouslySlandering> MALICIOUSLY_SLANDERING_STATIC_MODIFIER = MODIFIERS.register("maliciously_slandering", MaliciouslySlandering::new);

    public static final StaticModifier<TemporalAmplifier> TEMPORAL_AMPLIFIER_STATIC_MODIFIER = MODIFIERS.register("temporal_amplifier", TemporalAmplifier::new);

    public static final StaticModifier<WormAccumulation> WORM_ACCUMULATION_STATIC_MODIFIER = MODIFIERS.register("wormaccumulation", WormAccumulation::new);

    public static final StaticModifier<ScepterOfDivineSociety> SCEPTER_OF_DIVINE_SOCIETY_STATIC_MODIFIER = MODIFIERS.register("scepter_of_divine_society", ScepterOfDivineSociety::new);

    public static final StaticModifier<DiademaModifier> ONIMIKO_STATIC_MODIFIER =
            MODIFIERS.register("onimiko", CommonDiademaModifier.Create(DiademaRegister.ONIMIKO::get));
    ///真实形态
    public static final StaticModifier<Real_souleat> REAL_SOULEAT_STATIC_MODIFIER =
            MODIFIERS.register(
                    "real_souleat",
                    () -> new Real_souleat(
                            "ten_thousand_souls",
                            MaterialVariantId.create(
                                    new MaterialId("miztinker", "nihilite_souls"),
                                    "default"
                            ),
                            "你的武器彻底吃饱了..."
                    )
            );

}

