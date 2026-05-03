package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.List;

import static com.mizi.miztinker.miztinker.getResource;

public class Death_eye extends NoLevelsModifier implements SlotStackModifierHook, InventoryTickModifierHook {

    private static final String GLOW_ACTIVE = "glow_active";
    private static final float RADIUS = 16f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry,
                                            ItemStack held, Slot slot, Player player, SlotAccess access) {
        ModDataNBT data = tool.getPersistentData();
        boolean nowActive = !data.getBoolean(getResource(GLOW_ACTIVE));
        data.putBoolean(getResource(GLOW_ACTIVE), nowActive);

        if (nowActive) {
            player.displayClientMessage(Component.translatable("message.miztinker.death_eye.on"), true);
        } else {
            player.displayClientMessage(Component.translatable("message.miztinker.death_eye.off"), true);
        }
        return true;
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        holder.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false));

        if (!(holder instanceof Player player)) return;

        boolean active = tool.getPersistentData().getBoolean(getResource(GLOW_ACTIVE));

        if (!active) {
            return;
        }

        AABB area = player.getBoundingBox().inflate(RADIUS);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        if (!world.isClientSide) {
            for (LivingEntity e : entities) {
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0, false, false));
            }
        } else {
            for (LivingEntity e : entities) {
                ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
                if (id != null) {
                    e.setCustomName(Component.literal(id.toString()));
                    e.setCustomNameVisible(true);
                }
            }
        }
    }
}