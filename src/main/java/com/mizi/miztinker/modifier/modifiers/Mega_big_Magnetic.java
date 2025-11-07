package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Mega_big_Magnetic extends NoLevelsModifier implements InventoryTickModifierHook {

    /** 每 tick 检查并执行 **/
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {
        // 仅在服务器端执行
        if (world.isClientSide()) return;
        if (!(holder instanceof Player player)) return;
        if (!isCorrectSlot) return;

        // 每秒执行一次
        if (player.tickCount % 20 == 0) {
            HaoranArua(player, 7.0);
        }
    }

    /**
     * 半径7格内所有生物流失最大生命值的20%，
     * 玩家吸收并永久提升最大生命值。
     */
    private static void HaoranArua(Player player, double range) {
        Level level = player.level();
        AABB area = new AABB(
                player.getX() - range, player.getY() - range, player.getZ() - range,
                player.getX() + range, player.getY() + range, player.getZ() + range
        );

        double totalDrained = 0.0;

        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (living == player || !living.isAlive()) continue;

            // 按最大生命值计算目标流失量
            float maxHp = living.getMaxHealth();
            float drainAmount = maxHp * 0.2F;

            // 实际可造成的伤害不能超过当前血量
            float actualDrain = Math.min(drainAmount, living.getHealth());

            // 扣血
            living.setHealth(living.getHealth() - actualDrain);

            // 如果生命值太低则死亡
            if (living.getHealth() <= 0.5F) {
                living.die(new DamageSource(
                        living.level().registryAccess()
                                .registryOrThrow(Registries.DAMAGE_TYPE)
                                .getHolderOrThrow(DamageTypes.PLAYER_ATTACK),
                        player
                ));
                living.setHealth(0);
            }

            // 累计真实吸收的生命值
            totalDrained += actualDrain;
        }

        // 将吸取的生命值转化为最大生命值提升
        if (totalDrained > 0) {
            AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr != null) {
                double originalMaxHealth = maxHealthAttr.getBaseValue();
                double newMaxHealth = originalMaxHealth + totalDrained;
                maxHealthAttr.setBaseValue(newMaxHealth);
            }
        }
    }
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
}
