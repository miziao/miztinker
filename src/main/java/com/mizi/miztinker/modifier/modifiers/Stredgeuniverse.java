package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.hook.MiztinkerHooks;
import com.mizi.miztinker.modifier.hook.LeftClickModifierHook;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.wzz.stredgeuniverse.entity.MeteoriteSwordEntity;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Stredgeuniverse extends NoLevelsModifier implements LeftClickModifierHook {

    @Override
    public void onLeftClickEmpty(IToolStackView tool, ModifierEntry entry, Player player, Level world, EquipmentSlot slot) {
        if (!world.isClientSide) {
            createTriangleSwordArray(player, world);
        }
    }

    @Override
    public void onLeftClickBlock(IToolStackView tool, ModifierEntry entry, Player player, Level world, EquipmentSlot slot, BlockState state, BlockPos pos) {
        if (!world.isClientSide) {
            createTriangleSwordArray(player, world);
        }
    }

    public static void createTriangleSwordArray(Player player, Level world) {
        if (world.isClientSide) return;

        Vec3 playerPos = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);

        int[] rainbowColors = new int[]{0xFF0000, 0xFF8000, 0xFFFF00, 0x00FF00, 0x00FFFF, 0x0000FF, 0x8000FF, 0xFF0080};

        int swordCount = 8;
        float radius = 3.0f;
        int delay = 0;

        for (int i = 0; i < swordCount; i++) {
            double angle = (2 * Math.PI / swordCount) * i;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Vec3 spawnPos = playerPos.add(x, -0.4, z);

            Vec3 shootDir = new Vec3(x, 0.0, z).normalize();

            MeteoriteSwordEntity sword = new MeteoriteSwordEntity(SlashBlade.RegistryEvents.StormSwords, world);
            sword.setOwner(player);
            sword.setDamage(10000.0);
            sword.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            sword.setDelay(delay);
            sword.setColor(rainbowColors[i % rainbowColors.length]);
            sword.shoot(shootDir.x, shootDir.y, shootDir.z, 3.0f, 0.2f);

            world.addFreshEntity(sword);
            delay += 2;
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, MiztinkerHooks.LEFT_CLICK);
    }
}