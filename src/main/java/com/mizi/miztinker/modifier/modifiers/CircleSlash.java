package com.mizi.miztinker.modifier.modifiers;

import com.c2h6s.etstlib.register.EtSTLibHooks;
import com.c2h6s.etstlib.tool.hooks.LeftClickModifierHook;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import mods.flammpfeil.slashblade.util.KnockBacks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Random;


public class CircleSlash extends NoLevelsModifier implements LeftClickModifierHook {

    private static boolean LOADED = false;

    static {
        try {
            // 检测两个类是否都存在
            Class.forName("mods.flammpfeil.slashblade.SlashBlade");
            Class.forName("com.c2h6s.etstlib.register.EtSTLibHooks");
            LOADED = true;
        } catch (Throwable ignored) {
            LOADED = false;
        }
    }

    private static final int SWORD_COUNT = 4;        // 剑气数量
    private static final float SLASH_RADIUS = 2.5f;   // 圆形半径
    private static final int SWORD_COLOR = 0x40E0D0;  // 剑气颜色

    @Override
    public void onLeftClickEmpty(IToolStackView tool, ModifierEntry entry, Player player, Level world, EquipmentSlot slot) {
        if (!world.isClientSide) {
            spawnCircleSwords(player, world);
        }
    }

    @Override
    public void onLeftClickBlock(IToolStackView tool, ModifierEntry entry, Player player, Level world,
                                 EquipmentSlot slot, BlockState state, BlockPos pos) {
        if (!world.isClientSide) {
            spawnCircleSwords(player, world);
        }
    }

    private void spawnCircleSwords(Player player, Level world) {
        if (world.isClientSide()) return;

        double armor = player.getArmorValue();
        double toughness = Math.max(player.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 1);
        float damage = (float) (armor / toughness * 0.5d); // 转 float


        for (int i = 0; i < SWORD_COUNT; i++) {
            float angle = (360f / SWORD_COUNT) * i;

            EntitySlashEffect sword = new EntitySlashEffect(SlashBlade.RegistryEvents.SlashEffect, world);

            // 必须设置 owner
            sword.setOwner(player);

            // 基础伤害
            sword.setDamage(damage);

            sword.setIsCritical(true);
            sword.setKnockBack(KnockBacks.cancel);
            sword.setColor(SWORD_COLOR);
            sword.setRotationRoll(angle);
            sword.setCycleHit(false);
            sword.setIndirect(true);

            double xOffset = Math.cos(Math.toRadians(angle)) * SLASH_RADIUS;
            double yOffset = player.getBbHeight() * 0.75;
            double zOffset = Math.sin(Math.toRadians(angle)) * SLASH_RADIUS;

            sword.absMoveTo(
                    player.getX() + xOffset,
                    player.getY() + yOffset,
                    player.getZ() + zOffset,
                    player.getYRot() + angle,
                    0F
            );

            // 必须添加到世界
            world.addFreshEntity(sword);
        }

        player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 0.2F, 1.45F);
    }


    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, EtSTLibHooks.LEFT_CLICK);
    }
}