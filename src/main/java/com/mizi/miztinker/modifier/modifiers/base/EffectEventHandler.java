package com.mizi.miztinker.modifier.modifiers.base;

import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker")
public class EffectEventHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            MobEffectInstance strEffect = attacker.getEffect(MiztinkerEffect.StrengthOldEffect.get());
            if (strEffect != null) {
                float extraMultiplier = (strEffect.getAmplifier() + 1) * 1.30f;
                event.setAmount(event.getAmount() * (1.0f + extraMultiplier));
            }
        }

        LivingEntity victim = event.getEntity();
        MobEffectInstance fractureEffect = victim.getEffect(MiztinkerEffect.BoneFractureEffect.get());

        if (fractureEffect != null) {
            int level = fractureEffect.getAmplifier();

            float vulnerabilityMultiplier = (level + 1) * 0.25f;

            float currentDamage = event.getAmount();
            event.setAmount(currentDamage * (1.0f + vulnerabilityMultiplier));
        }
    }
}
