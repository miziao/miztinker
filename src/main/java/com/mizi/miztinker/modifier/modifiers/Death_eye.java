package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.network.chat.Component;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.List;

public class Death_eye extends NoLevelsModifier implements SlotStackModifierHook, InventoryTickModifierHook {

    private static final String GLOW_ACTIVE = "glow_active";
    private static final float RADIUS = 16f;
    private static final ResourceLocation DEATH_EYE_MODE = new ResourceLocation("miztinker", "death_eye_mode");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK); // 必须加这个
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry,
                                            ItemStack held, Slot slot, Player player, SlotAccess access) {
        ModDataNBT data = tool.getPersistentData();
        boolean nowActive = !data.getBoolean(ResourceLocation.parse(GLOW_ACTIVE));
        data.putBoolean(ResourceLocation.parse(GLOW_ACTIVE), nowActive);

        if (nowActive) {
            player.displayClientMessage(Component.literal("§u死神之眼已开启！"), true);
        } else {
            player.displayClientMessage(Component.literal("§u死神之眼已关闭。"), true);
        }
        return true;
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        holder.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 40, 0, false, true));

        if (!(holder instanceof Player player)) return;

        boolean active = tool.getPersistentData().getBoolean(ResourceLocation.parse(GLOW_ACTIVE));
        if (!active) return;

        if (!world.isClientSide) {
            AABB area = player.getBoundingBox().inflate(RADIUS);
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && e.isAlive());

            for (LivingEntity e : entities) {
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
            }
        } else {
            // 客户端显示注册名
            AABB areaClient = player.getBoundingBox().inflate(RADIUS);
            List<LivingEntity> entitiesClient = world.getEntitiesOfClass(LivingEntity.class, areaClient,
                    e -> e != player && e.isAlive());

            for (LivingEntity e : entitiesClient) {
                // 获取生物注册名
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
                e.setCustomName(Component.literal(id.toString()));
                e.setCustomNameVisible(true);
            }
            }
        }
    }