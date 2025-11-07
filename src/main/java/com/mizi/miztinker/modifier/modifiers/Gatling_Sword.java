package com.mizi.miztinker.modifier.modifiers;

import cn.mmf.slashblade_addon.entity.GaleSwordsEntity;
import cn.mmf.slashblade_addon.registry.SBAEntitiesRegistry;
import com.c2h6s.etstlib.register.EtSTLibHooks;
import com.c2h6s.etstlib.tool.hooks.LeftClickModifierHook;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;


public class Gatling_Sword extends NoLevelsModifier implements LeftClickModifierHook {

    private static final int SWORD_COUNT = 8; // 固定发射数量
    private static final float SWORD_SPEED = 1.8f;
    private static final boolean CRITICAL = true;
    private static final int SWORD_COLOR = 0xf7892d;


    /** 左键空空点击 */
    @Override
    public void onLeftClickEmpty(@NotNull IToolStackView tool, @NotNull ModifierEntry entry,
                                 @NotNull Player player, @NotNull Level world,
                                 @NotNull EquipmentSlot slot) {
        if (!world.isClientSide()) {
            createTriangleSwordArray(tool, entry, player, world);
        }
    }

    @Override
    public void onLeftClickBlock(@NotNull IToolStackView tool, @NotNull ModifierEntry entry,
                                 @NotNull Player player, @NotNull Level world,
                                 @NotNull EquipmentSlot slot,
                                 @NotNull BlockState state, @NotNull BlockPos pos) {
        if (!world.isClientSide()) {
            createTriangleSwordArray(tool, entry, player, world);
        }
    }

    /** 发射幻影剑逻辑（固定 8 发，伤害=匠魂攻击力） */
    private void createTriangleSwordArray(IToolStackView tool, ModifierEntry entry, Player player, Level world) {
        float baseDamage = tool.getStats().get(ToolStats.ATTACK_DAMAGE);
        double damage = baseDamage * 0.5;
        RandomSource random = world.getRandom();

        for (int i = 0; i < SWORD_COUNT; i++) {
            GaleSwordsEntity sword = new GaleSwordsEntity(SBAEntitiesRegistry.BlisteringSwords, world);

            sword.setSpeed(SWORD_SPEED);
            sword.setIsCritical(CRITICAL);
            sword.setOwner(player);
            sword.setColor(SWORD_COLOR);
            sword.setRoll(0);
            sword.setDamage(damage);
            sword.startRiding(player, true);
            sword.setDelay(20 + i);

            boolean isRight = sword.getDelay() % 2 == 0;
            double xOffset = random.nextDouble() * 2.5 * (isRight ? 1 : -1);
            double yOffset = random.nextFloat() * 2;
            double zOffset = random.nextFloat() * 0.5;

            sword.setPos(player.position().add(xOffset, yOffset, zOffset));
            sword.setOffset(new Vec3(xOffset, yOffset, zOffset));

            world.addFreshEntity(sword);
            player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 0.2F, 1.45F);
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, EtSTLibHooks.LEFT_CLICK);
    }
}
