package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class TemporalAmplifier extends Modifier implements AttributesModifierHook, EquipmentChangeModifierHook, TooltipModifierHook, InventoryTickModifierHook {

    private final Map<String, Long> equippedTimeMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> currentBonusLevelMap = new ConcurrentHashMap<>();

    private static final int BASE_MAX_LEVEL = 100;
    private static final int SECONDS_PER_LEVEL = 300; // 5分钟一级

    private static final double BONUS_SPEED = 0.005;
    private static final double BONUS_ATTACK = 0.5;
    private static final double BONUS_ARMOR = 0.4;
    private static final double BONUS_TOUGHNESS = 0.2;
    private static final double BONUS_HEALTH = 1.0;
    private static final double BONUS_ATK_SPEED = 0.04;

    private static final String KEY_PROGRESS = "modifier.miztinker.temporal_amplifier.progress";

    public TemporalAmplifier() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.ATTRIBUTES);
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }


    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isHeld, ItemStack stack) {
        if (!world.isClientSide && holder instanceof Player player) {

            EquipmentSlot currentSlot = null;
            if (isSelected) {
                currentSlot = EquipmentSlot.MAINHAND;
            } else {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() == EquipmentSlot.Type.ARMOR && player.getItemBySlot(slot) == stack) {
                        currentSlot = slot;
                        break;
                    }
                }
            }

            if (currentSlot != null) {
                String key = getCombineKey(player, currentSlot);

                if (!equippedTimeMap.containsKey(key)) {
                    equippedTimeMap.put(key, world.getGameTime());
                }

                int modLevel = modifier.getLevel();
                updateAndApply(player, currentSlot, key, modLevel, world.getGameTime());
            }
        }
    }

    private void updateAndApply(Player player, EquipmentSlot slot, String key, int modifierLevel, long currentTime) {
        Long startTime = equippedTimeMap.get(key);
        if (startTime == null) return;

        long elapsedSeconds = (currentTime - startTime) / 20;
        int dynamicMax = BASE_MAX_LEVEL * modifierLevel;
        int newLevel = Math.min((int) (elapsedSeconds / SECONDS_PER_LEVEL), dynamicMax);

        currentBonusLevelMap.put(key, newLevel);

        if (newLevel > 0) {
            refreshAttributes(player, slot, newLevel);
        }
    }

    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry entry, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
        if (slot.getType() == EquipmentSlot.Type.ARMOR || slot == EquipmentSlot.MAINHAND) {
            registerAttr(consumer, Attributes.MOVEMENT_SPEED, slot, "speed", AttributeModifier.Operation.MULTIPLY_BASE);
            registerAttr(consumer, Attributes.ATTACK_DAMAGE, slot, "attack", AttributeModifier.Operation.ADDITION);
            registerAttr(consumer, Attributes.ARMOR, slot, "armor", AttributeModifier.Operation.ADDITION);
            registerAttr(consumer, Attributes.ARMOR_TOUGHNESS, slot, "toughness", AttributeModifier.Operation.ADDITION);
            registerAttr(consumer, Attributes.MAX_HEALTH, slot, "health", AttributeModifier.Operation.ADDITION);
            registerAttr(consumer, Attributes.ATTACK_SPEED, slot, "atk_speed", AttributeModifier.Operation.ADDITION);
        }
    }

    private void registerAttr(BiConsumer<Attribute, AttributeModifier> consumer, Attribute attr, EquipmentSlot slot, String name, AttributeModifier.Operation op) {
        consumer.accept(attr, new AttributeModifier(getAttrUuid(slot, name), "temporal_base_" + name, 0.0, op));
    }

    private UUID getAttrUuid(EquipmentSlot slot, String name) {
        return UUID.nameUUIDFromBytes(("temporal_v3_" + name + "_" + slot.getName()).getBytes());
    }

    @Override
    public void onEquip(IToolStackView tool, ModifierEntry entry, EquipmentChangeContext context) {
        if (context.getEntity() instanceof Player player && !player.level().isClientSide) {
            String key = getCombineKey(player, context.getChangedSlot());
            if (!equippedTimeMap.containsKey(key)) {
                equippedTimeMap.put(key, player.level().getGameTime());
            }
        }
    }

    @Override
    public void onUnequip(IToolStackView tool, ModifierEntry entry, EquipmentChangeContext context) {
        if (context.getEntity() instanceof Player player && !player.level().isClientSide) {
            cleanupSlot(player, context.getChangedSlot());
        }
    }

    private void cleanupSlot(Player player, EquipmentSlot slot) {
        String key = getCombineKey(player, slot);
        removeAllAttributes(player, slot);
        equippedTimeMap.remove(key);
        currentBonusLevelMap.remove(key);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        String uuidPrefix = event.getEntity().getUUID().toString();
        equippedTimeMap.keySet().removeIf(key -> key.startsWith(uuidPrefix));
        currentBonusLevelMap.keySet().removeIf(key -> key.startsWith(uuidPrefix));
    }

    private void refreshAttributes(Player player, EquipmentSlot slot, int bonusLevel) {
        apply(player, Attributes.MOVEMENT_SPEED, getAttrUuid(slot, "speed"), bonusLevel * BONUS_SPEED, AttributeModifier.Operation.MULTIPLY_BASE);
        apply(player, Attributes.ATTACK_DAMAGE, getAttrUuid(slot, "attack"), bonusLevel * BONUS_ATTACK, AttributeModifier.Operation.ADDITION);
        apply(player, Attributes.ARMOR, getAttrUuid(slot, "armor"), bonusLevel * BONUS_ARMOR, AttributeModifier.Operation.ADDITION);
        apply(player, Attributes.ARMOR_TOUGHNESS, getAttrUuid(slot, "toughness"), bonusLevel * BONUS_TOUGHNESS, AttributeModifier.Operation.ADDITION);
        apply(player, Attributes.MAX_HEALTH, getAttrUuid(slot, "health"), bonusLevel * BONUS_HEALTH, AttributeModifier.Operation.ADDITION);
        apply(player, Attributes.ATTACK_SPEED, getAttrUuid(slot, "atk_speed"), bonusLevel * BONUS_ATK_SPEED, AttributeModifier.Operation.ADDITION);
    }

    private void apply(Player player, Attribute attr, UUID uuid, double value, AttributeModifier.Operation op) {
        var instance = player.getAttribute(attr);
        if (instance != null) {
            instance.removeModifier(uuid);
            instance.addTransientModifier(new AttributeModifier(uuid, "temporal_active", value, op));
        }
    }

    private void removeAllAttributes(Player player, EquipmentSlot slot) {
        if (slot == null) return;
        String[] types = {"speed", "attack", "armor", "toughness", "health", "atk_speed"};
        Attribute[] attrs = {Attributes.MOVEMENT_SPEED, Attributes.ATTACK_DAMAGE, Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS, Attributes.MAX_HEALTH, Attributes.ATTACK_SPEED};
        for (int i = 0; i < attrs.length; i++) {
            var inst = player.getAttribute(attrs[i]);
            if (inst != null) inst.removeModifier(getAttrUuid(slot, types[i]));
        }
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltips, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        if (player != null) {
            int currentLevel = 0;
            String playerUuidPrefix = player.getUUID().toString();

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                String key = getCombineKey(player, slot);
                if (currentBonusLevelMap.containsKey(key)) {
                    ItemStack stackInSlot = player.getItemBySlot(slot);
                    if (!stackInSlot.isEmpty()) {
                        if (player.getItemBySlot(slot).getOrCreateTag().equals(player.getMainHandItem().getOrCreateTag())) {
                            currentLevel = currentBonusLevelMap.getOrDefault(key, 0);
                        }
                    }
                }
            }

            if (currentLevel == 0) {
                for (Map.Entry<String, Integer> entry : currentBonusLevelMap.entrySet()) {
                    if (entry.getKey().startsWith(playerUuidPrefix)) {
                        currentLevel = Math.max(currentLevel, entry.getValue());
                    }
                }
            }

            int max = BASE_MAX_LEVEL * modifier.getLevel();
            int progress = (int) (((double) currentLevel / max) * 100);

            tooltips.add(Component.translatable(KEY_PROGRESS, progress + "%")
                    .append(Component.literal(" (" + currentLevel + "/" + max + ")")));
        }
    }

    private String getCombineKey(Player player, EquipmentSlot slot) {
        return player.getUUID() + "_" + slot.getName();
    }
}