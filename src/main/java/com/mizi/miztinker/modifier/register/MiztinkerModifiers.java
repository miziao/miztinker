package com.mizi.miztinker.modifier.register;

import com.csdy.tcondiadema.modifier.CommonDiademaModifier;
import com.csdy.tcondiadema.modifier.DiademaModifier;
import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.diadema.DiademaRegister;
import com.mizi.miztinker.modifier.modifiers.*;
import com.mizi.miztinker.modifier.modifiers.base.BaseToolMusic;
import com.mizi.miztinker.sounds.MiztinkerSounds;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
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
    public static StaticModifier<?> DYNAMAX = null;

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


    public static StaticModifier<?> XXKILLER = null;
    //深渊保护
    public static final StaticModifier<Abyssal_Protection> ABYSSAL_PROTECTION = MODIFIERS.register("abyssal_protection", Abyssal_Protection::new);

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

    public static StaticModifier<?> awakenDoomGuyStaticModifier = null;

    public static final StaticModifier<AwakenUltraman> AWAKEN_ULTRAMAN_STATIC_MODIFIER = MODIFIERS.register("awaken_ultraman", AwakenUltraman::new);

    public static StaticModifier<?> GREY_MATTER_STATIC_MODIFIER = null;

    public static final StaticModifier<WoundEffectAttack > WOUND_EFFECT_ATTACK_STATIC_MODIFIER = MODIFIERS.register("woundeffectattack", WoundEffectAttack ::new);

    public static final StaticModifier<Tiga > TIGA_STATIC_MODIFIER = MODIFIERS.register("tiga", Tiga ::new);

    public static final StaticModifier<AntonBloodline > ANTON_BLOODLINE_STATIC_MODIFIER = MODIFIERS.register("antonbloodline", AntonBloodline ::new);

    public static final StaticModifier<EnchantedGold > ENCHANTED_GOLD_STATIC_MODIFIER = MODIFIERS.register("enchantedgold", EnchantedGold ::new);

    public static final StaticModifier<Plumber > PLUMBER_STATIC_MODIFIER = MODIFIERS.register("plumber", Plumber ::new);

    public static final StaticModifier<Alloying> ALLOYING = MODIFIERS.register("alloying", Alloying::new);

    public static final StaticModifier<Infinitum> INFINITUM_STATIC_MODIFIER = MODIFIERS.register("infinitum", Infinitum::new);

    public static final StaticModifier<BlockingDamage> BLOCKING_DAMAGE_STATIC_MODIFIER = MODIFIERS.register("blockingdamage", BlockingDamage::new);

    public static final StaticModifier<BornOfStorm> BORN_OF_STORM_STATIC_MODIFIER = MODIFIERS.register("bronofstorm", BornOfStorm::new);

    public static final StaticModifier<SteelHowForged> STEEL_HOW_FORGED_STATIC_MODIFIER = MODIFIERS.register("steelhowforged", SteelHowForged::new);

    public static final StaticModifier<Astral> ASTRAL_STATIC_MODIFIER = MODIFIERS.register("astral", Astral::new);


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

