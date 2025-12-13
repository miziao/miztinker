package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;

import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mod.EventBusSubscriber(modid = "miztinker")
public class OverHeal extends NoLevelsModifier implements InventoryTickModifierHook {

    /** Tinkers Construct 识别用的 ID */
    public static final ModifierId ID = new ModifierId("miztinker", "overheal");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    /** 监听治疗事件，把溢出的生命值转换为吸收护盾 */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity living = event.getEntity();
        if (!(living instanceof Player player)) return;

        Level world = player.level();
        if (world == null || world.isClientSide) return;

        // 玩家任意护甲栏上是否有 OverHeal
        boolean hasOverheal = false;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;

            ItemStack armor = player.getItemBySlot(slot);
            if (armor.isEmpty()) continue;

            IToolStackView tool = ToolStack.from(armor);

            if (!tool.isBroken() && tool.getModifierLevel(ID) > 0) {
                hasOverheal = true;
                break;
            }
        }

        if (!hasOverheal) return;

        float heal = event.getAmount();
        float current = player.getHealth();
        float max = player.getMaxHealth();

        float newHealth = current + heal;

        // 超出上限部分 → 吸收（absorption）
        if (newHealth > max) {
            float overheal = newHealth - max;
            float oldAbsorb = player.getAbsorptionAmount();
            player.setAbsorptionAmount(oldAbsorb + overheal);
        }
    }

    /** 工具脱下时清除吸收，保持 TCon 风格 */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        if (world.isClientSide) return;
        if (!(holder instanceof Player player)) return;

        // 若工具脱下或破损 → 清空吸收护盾
        if (!isCorrectSlot || tool.isBroken()) {
            if (player.getAbsorptionAmount() > 0f) {
                player.setAbsorptionAmount(0f);
            }
        }
    }
}