package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.util.C;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.RawDataModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.RestrictedCompoundTag;

import java.util.List;
import java.util.Random;

import static com.mizi.miztinker.miztinker.getResource;

public class TrifoldEntity extends Modifier implements ToolStatsModifierHook, RawDataModifierHook, VolatileDataModifierHook, TooltipModifierHook {

    private static final ResourceLocation KEY_SLOTS = getResource("trifold_entity_slots");
    private static final ResourceLocation LAST_LEVEL = getResource("trifold_entity_last_level");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS, ModifierHooks.RAW_DATA, ModifierHooks.VOLATILE_DATA, ModifierHooks.TOOLTIP);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        int level = modifier.getLevel();
        float multiplier;

        if (level == 1) {
            multiplier = 0.8f;
        } else {
            multiplier = -0.3f * level;
        }

        ToolStats.DURABILITY.add(builder, multiplier);
        ToolStats.ATTACK_DAMAGE.add(builder, multiplier);
        ToolStats.ATTACK_SPEED.add(builder, multiplier);
        ToolStats.MINING_SPEED.add(builder, multiplier);
        ToolStats.ARMOR.add(builder, multiplier);
        ToolStats.ARMOR_TOUGHNESS.add(builder, multiplier);
    }

    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
        if (context.getPersistentData() instanceof ModDataNBT persistentData) {
            if (persistentData.contains(KEY_SLOTS, 10)) {
                CompoundTag slots = persistentData.getCompound(KEY_SLOTS);
                for (String key : slots.getAllKeys()) {
                    SlotType slotType = SlotType.getIfPresent(key);
                    if (slotType != null) {
                        volatileData.addSlots(slotType, slots.getInt(key));
                    }
                }
            }
        }
    }

    @Override
    public void addRawData(IToolStackView tool, ModifierEntry modifier, RestrictedCompoundTag restrictedData) {
        ModDataNBT persistentData = tool.getPersistentData();
        int currentLevel = modifier.getLevel();
        int lastLevel = persistentData.getInt(LAST_LEVEL);

        if (currentLevel != lastLevel) {
            CompoundTag slotsTag = new CompoundTag();
            Random rand = new Random();
            int count = (currentLevel == 1) ? 7 : 1;

            for (int i = 0; i < count; i++) {
                String slotName = getRandomSlot(rand).getName();
                slotsTag.putInt(slotName, slotsTag.getInt(slotName) + 1);
            }

            persistentData.put(KEY_SLOTS, slotsTag);
            persistentData.putInt(LAST_LEVEL, currentLevel);
        }
    }

    @Override
    public void removeRawData(IToolStackView tool, Modifier modifier, RestrictedCompoundTag restrictedData) {
        ModDataNBT persistentData = tool.getPersistentData();
        persistentData.remove(KEY_SLOTS);
        persistentData.remove(LAST_LEVEL);
    }

    private SlotType getRandomSlot(Random rand) {
        int r = rand.nextInt(4);
        return switch (r) {
            case 0 -> SlotType.UPGRADE;
            case 1 -> SlotType.ABILITY;
            case 2 -> SlotType.DEFENSE;
            default -> SlotType.SOUL;
        };
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        if (player != null && modifier.getLevel() == 1) {
            String translatedText = Component.translatable("modifier.miztinker.trifold_entity.peak_warning").getString();

            tooltip.add(C.getRainbowComponent(translatedText));
        }

    }
}