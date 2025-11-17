package com.mizi.miztinker.modifier.modifiers;
import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.RequirementsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.List;

public class AwakenKnight_of_Night extends NoLevelsModifier implements
        GeneralInteractionModifierHook,
        InventoryTickModifierHook,
        RequirementsModifierHook,
        ValidateModifierHook {

    private static final ResourceLocation ACTIVE = new ResourceLocation("miztinker", "timestop_active");

    /** 前置要求：必须拥有 knight_of_night */
    @Override
    public @Nullable Component validate(IToolStackView tool, ModifierEntry entry) {
        if (tool.getModifierLevel(MiztinkerModifiers.KNIGHT_OF_NIGHT.getId()) > 0)
            return null;
        return requirementsError(entry);
    }

    @Override
    public Component requirementsError(ModifierEntry entry) {
        return Component.translatable("modifier.miztinker.AwakenKnight_of_Night.requirements");
    }

    /** 注册 Hook */
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.REQUIREMENTS);
        hookBuilder.addHook(this, ModifierHooks.VALIDATE);
    }

    /** 下蹲 + 右键 切换时停 */
    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier,
                                       Player player, InteractionHand hand,
                                       InteractionSource source) {

        if (player.level().isClientSide) return InteractionResult.PASS;
        if (source != InteractionSource.RIGHT_CLICK) return InteractionResult.PASS;

        if (!player.isCrouching() || tool.isBroken()) return InteractionResult.PASS;

        ModDataNBT data = tool.getPersistentData();
        boolean nowActive = !data.getBoolean(ACTIVE);
        data.putBoolean(ACTIVE, nowActive);

        if (nowActive) {
            player.sendSystemMessage(Component.literal("§eThe World!"));
        } else {
            player.sendSystemMessage(Component.literal("§a这才是我的逃跑路线哒!"));
        }

        return InteractionResult.SUCCESS;
    }

    /** Tick：开启状态 → 时停 */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world,
                                LivingEntity holder, int slot, boolean isSelected,
                                boolean isCorrectSlot, net.minecraft.world.item.ItemStack stack) {

        if (!(holder instanceof Player player)) return;

        boolean active = tool.getPersistentData().getBoolean(ACTIVE);
        if (!active) return;

        if (world.isClientSide) return;

        ServerLevel server = (ServerLevel) world;
        AABB area = new AABB(
                server.getWorldBorder().getMinX(), server.getMinBuildHeight(),
                server.getWorldBorder().getMinZ(),
                server.getWorldBorder().getMaxX(), server.getMaxBuildHeight(),
                server.getWorldBorder().getMaxZ()
        );

        List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && e != player);

        for (LivingEntity e : list) {
            e.addEffect(new MobEffectInstance(
                    MiztinkerEffect.HorologiumNoAI.get(),
                    40,
                    0,
                    false, false, false
            ));
        }
    }
}