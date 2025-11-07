package com.mizi.miztinker.modifier.diadema.onimiko;

import com.csdy.tcondiadema.diadema.api.ranges.SelfDiademaRange;
import com.csdy.tcondiadema.frames.diadema.Diadema;
import com.csdy.tcondiadema.frames.diadema.DiademaType;
import com.csdy.tcondiadema.frames.diadema.movement.DiademaMovement;
import com.csdy.tcondiadema.frames.diadema.range.DiademaRange;
import com.mizi.miztinker.miztinker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import static net.minecraftforge.eventbus.api.EventPriority.HIGHEST;


@Mod.EventBusSubscriber(modid = miztinker.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OniMikoDiadema extends Diadema {

    private static final float DAMAGE_CAP_HIGH = 20.0F;
    private static final float DAMAGE_CAP_LOW = 10.0F;
    private static final float REFLECT_RATIO = 0.5F;

    private final SelfDiademaRange range;

    private static boolean LOADED = false;

    static {
        try {
            // 检测两个类是否都存在
            Class.forName("com.csdy.tcondiadema.diadema.api.ranges.SelfDiademaRange");
            LOADED = true;
        } catch (Throwable ignored) {
            LOADED = false;
        }
    }

    public OniMikoDiadema(DiademaType type, DiademaMovement movement) {
        super(type, movement);

        Entity core = getCoreEntity();
        LivingEntity livingCore = core instanceof LivingEntity ? (LivingEntity) core : null;
        this.range = new SelfDiademaRange(this, livingCore);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void removed() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public @NotNull DiademaRange getRange() {
        return range;
    }

    /** 限制自身受到的伤害上限 */
    @SubscribeEvent(priority = HIGHEST)
    public void onEntityHurt(LivingHurtEvent event) {
        Entity core = getCoreEntity();
        if (!(core instanceof LivingEntity player)) return;
        if (event.getEntity() != player) return;

        float maxHealth = player.getMaxHealth();
        float currentHealth = player.getHealth();
        float damageCap = currentHealth < maxHealth * 0.5f ? DAMAGE_CAP_LOW : DAMAGE_CAP_HIGH;

        event.setAmount(Math.min(event.getAmount(), damageCap));
    }

    /** 反弹部分伤害给攻击者 */
    @SubscribeEvent
    public void onEntityDamaged(LivingDamageEvent event) {
        Entity core = getCoreEntity();
        if (!(core instanceof LivingEntity player)) return;
        if (event.getEntity() != player) return;

        DamageSource src = event.getSource();
        if (src.getEntity() instanceof LivingEntity attacker && attacker.isAlive()) {
            attacker.hurt(player.damageSources().thorns(player), event.getAmount() * REFLECT_RATIO);
        }
    }
}