package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;


public class FutureDiary extends NoLevelsModifier implements DamageBlockModifierHook, InventoryTickModifierHook {

    private static final Random random = new Random();
    private static final Map<ServerPlayer, String> lastBlocked = new WeakHashMap<>();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    /** Tick 处理损坏 → 死亡 */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        // 只处理玩家，且服务端执行
        if (!(holder instanceof Player player)) return;
        if (level.isClientSide()) return;

        // 遍历玩家背包
        for (ItemStack invStack : player.getInventory().items) {
            if (invStack.isEmpty()) continue;

            // 匠魂工具视图
            IToolStackView tcon = ToolStack.from(invStack);

            if (ModifierUtil.getModifierLevel(invStack, this.getId()) <= 0) continue;
            if (!tcon.isBroken()) continue;

            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(
                        Component.literal("你的未来日记已经损坏……Dead End...")
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                );
            }

            applyBrokenInstantKill(player, player);
            invStack.setCount(0);
            break;
        }
    }

    /** 100% 闪避伤害并提示 */
    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry entry,
                                   EquipmentContext context, EquipmentSlot slot,
                                   DamageSource source, float amount) {

        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (slot != EquipmentSlot.OFFHAND) return false;

        // 显示一次伤害来源
        String id = source.getMsgId();
        if (!id.equals(lastBlocked.get(player))) {
            Component msg = source.getLocalizedDeathMessage(player);
            player.sendSystemMessage(
                    Component.literal("未来日记预知了你的死亡：避免了 " + msg.getString())
                            .withStyle(ChatFormatting.AQUA)
            );
            lastBlocked.put(player, id);
        }

        // 极低概率损坏工具（耐久归零）
        if (random.nextFloat() < 0.001f) {
            tool.setDamage(tool.getDamage() + tool.getCurrentDurability());
            player.sendSystemMessage(
                    Component.literal("在战斗中你的未来日记被损坏了！")
                            .withStyle(ChatFormatting.RED)
            );
        }

        return true;
    }

    /** 立即死亡实现 */
    public static void applyBrokenInstantKill(LivingEntity target, LivingEntity owner) {
        if (!target.level().isClientSide()) {
            target.kill();  // 100% 强制死亡，不会被匠魂拦截
        }
    }
}