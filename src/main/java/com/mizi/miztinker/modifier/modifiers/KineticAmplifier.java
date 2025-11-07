package com.mizi.miztinker.modifier.modifiers;


import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import net.minecraft.world.phys.Vec3;

public class KineticAmplifier extends NoLevelsModifier implements MeleeDamageModifierHook {

    /**
     * 疾跑基准速度（玩家平地疾跑约 0.12）
     */
    private static final double BASE_SPEED = 0.12;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }

    @Override
    public float getMeleeDamage(@NotNull IToolStackView tool,
                                @NotNull ModifierEntry modifier,
                                @NotNull ToolAttackContext context,
                                float baseDamage,
                                float damage) {

        Player player = context.getPlayerAttacker();
        if (player == null) {
            return damage;
        }

        // 实际移动速度（包含XYZ）
        Vec3 motion = player.getDeltaMovement();
        double currentSpeed = motion.horizontalDistance(); // 只计算水平速度

        double multiplier;

        if (player.onGround()) {
            // ✅ 地面上：速度超过 0.1 即计为“全速”
            multiplier = currentSpeed / 0.1;
            if (multiplier < 0.2) multiplier = 0.2; // 站着也有点基础伤害
            if (multiplier > 1.0) multiplier = 1.0; // 平地最高 1.0 倍速
            multiplier *= 2.0; // 放大地面系数（相当于轻微移动就2倍伤害）
        } else {
            // 🪂 空中时按实际速度比例计算（下落可大幅增伤）
            multiplier = currentSpeed / BASE_SPEED;
            if (multiplier < 0.05) multiplier = 0.05;
        }

        float newDamage = (float) (damage * multiplier);

        // 调试显示
        if (player.level().isClientSide() && player.tickCount % 20 == 0) {
            player.displayClientMessage(
                    Component.literal(String.format("速度: %.3f 倍率: %.2f", currentSpeed, multiplier)),
                    true
            );
        }

        return newDamage;
    }

}