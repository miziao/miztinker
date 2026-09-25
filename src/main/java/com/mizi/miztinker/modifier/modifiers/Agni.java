package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;

import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurt;

public class Agni extends NoLevelsModifier implements InventoryTickModifierHook {

    private static final int SELF_DAMAGE_COOLDOWN_TICKS = 10;

    private static final double RADIUS = 1.5;

    private static final float SELF_DAMAGE = 10f;

    private static final float INFECT_DAMAGE = 10f;

    public static final ResourceLocation AGNI_ID =
            ResourceLocation.fromNamespaceAndPath("miztinker", "agni");

    public static final String TAG_AGNI_BURN = "agni_eternal_burn";

    public static final String TAG_AGNI_SELF_BURN = "agni_self_burn";

    private static final String TAG_SELF_COOLDOWN = "agni_self_damage_cooldown";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(
            IToolStackView tool,
            ModifierEntry modifier,
            Level level,
            LivingEntity holder,
            int slot,
            boolean isSelected,
            boolean isCorrectSlot,
            ItemStack stack
    ) {
        if (!(holder instanceof ServerPlayer player)) {
            return;
        }

        if (!isCorrectSlot || level.isClientSide()) {
            return;
        }

        ServerLevel server = (ServerLevel) level;
        CompoundTag playerData = player.getPersistentData();

        player.addEffect(new MobEffectInstance(
                MobEffects.HEAL,
                1,
                100,
                true,
                false,
                false
        ));

        playerData.putBoolean(TAG_AGNI_SELF_BURN, true);
        forceEntityBurn(player, 10);

        int selfCooldown = playerData.getInt(TAG_SELF_COOLDOWN);

        if (selfCooldown <= 0) {
            forceHurt(player, player.damageSources().generic(), SELF_DAMAGE);
            playerData.putInt(TAG_SELF_COOLDOWN, SELF_DAMAGE_COOLDOWN_TICKS);
        } else {
            playerData.putInt(TAG_SELF_COOLDOWN, selfCooldown - 1);
        }

        infectNearbyEntities(server, player);

        BlockPos pos = player.blockPosition();
        if (server.isEmptyBlock(pos)) {
            server.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
        }
    }

    private void infectNearbyEntities(ServerLevel server, ServerPlayer player) {
        List<LivingEntity> nearby = server.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(RADIUS),
                entity -> entity != player && entity.isAlive()
        );

        for (LivingEntity target : nearby) {
            infectEntity(target);

            forceEntityBurn(target, 10);

            forceHurt(target, player.damageSources().generic(), INFECT_DAMAGE);
        }
    }

    public static void infectEntity(LivingEntity target) {
        if (target == null || target.level().isClientSide()) {
            return;
        }

        target.getPersistentData().putBoolean(TAG_AGNI_BURN, true);
    }

    public static boolean isAgniBurning(LivingEntity target) {
        if (target == null) {
            return false;
        }

        return target.getPersistentData().getBoolean(TAG_AGNI_BURN);
    }

    public static boolean isAgniSelfBurning(ServerPlayer player) {
        if (player == null) {
            return false;
        }

        return player.getPersistentData().getBoolean(TAG_AGNI_SELF_BURN);
    }

    public static void clearAgniSelfBurn(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        CompoundTag data = player.getPersistentData();

        data.remove(TAG_AGNI_SELF_BURN);
        data.remove(TAG_SELF_COOLDOWN);

        player.setRemainingFireTicks(0);
        player.clearFire();
    }

    public static void forceEntityBurn(LivingEntity target, int seconds) {
        if (target == null || target.level().isClientSide()) {
            return;
        }

        target.setSecondsOnFire(seconds);
        target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), seconds * 20));
    }

    public static boolean hasAgniEquipped(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) {
            return false;
        }

        for (ItemStack stack : player.getArmorSlots()) {
            if (hasAgniModifier(stack)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasAgniModifier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        try {
            ToolStack tool = ToolStack.copyFrom(stack);

            if (tool.isBroken()) {
                return false;
            }

            return tool.getModifierList().stream()
                    .anyMatch(entry -> AGNI_ID.equals(entry.getModifier().getId()));
        } catch (Exception ignored) {
            return false;
        }
    }
}