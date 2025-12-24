package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

import java.util.Objects;

public class LearningDevice extends NoLevelsModifier implements MeleeHitModifierHook, BlockBreakModifierHook {

    public static final ModifierId ID = new ModifierId("miztinker:learningdevice");
    private static final ThreadLocal<Boolean> GUARD = ThreadLocal.withInitial(() -> false);
    private static final ModifierId IMPROVABLE_ID = new ModifierId("tinkerslevellingaddon:improvable");

    public LearningDevice() {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK);
    }

    /** 攻击事件监听 - 这里是经验值实际增加的地方 */
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (context.getAttacker() instanceof ServerPlayer player && tool instanceof ToolStack ts) {
            // 计算应该获得的经验值
            int xpAmount = calculateAttackXp(damageDealt);
            if (xpAmount > 0) {
                copyXpToInventory(player, xpAmount);
            }
        }
    }

    /** 挖掘事件监听 */
    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        net.minecraft.world.entity.player.Player player = context.getPlayer();
        if (player instanceof ServerPlayer sp && !player.level().isClientSide && tool instanceof ToolStack ts) {
            copyXpToInventory(sp, 1); // 挖掘基础经验为1
        }
    }

    /** 计算攻击经验值 */
    private int calculateAttackXp(float damageDealt) {
        // 参考原版经验计算逻辑
        return Math.round(damageDealt);
    }

    /** 将经验同步到背包、盔甲和副手 */
    private static void copyXpToInventory(ServerPlayer player, int amount) {
        if (GUARD.get() || amount <= 0) return;
        GUARD.set(true);

        try {
            // 遍历主背包
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (addXpToStack(stack, amount, player)) {
                    player.getInventory().setItem(i, stack);
                }
            }
            // 遍历盔甲
            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                ItemStack stack = player.getInventory().armor.get(i);
                if (addXpToStack(stack, amount, player)) {
                    player.getInventory().armor.set(i, stack);
                }
            }
            // 副手
            ItemStack offhand = player.getOffhandItem();
            if (addXpToStack(offhand, amount, player)) {
                player.getInventory().setItem(player.getInventory().selected, offhand);
            }

            player.containerMenu.broadcastChanges();
        } finally {
            GUARD.set(false);
        }
    }

    private static boolean addXpToStack(ItemStack stack, int amount, ServerPlayer player) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof ModifiableItem)) return false;

        ToolStack tool = ToolStack.from(stack);

        // 检查是否有 Improvable 修饰器
        if (tool.getModifierLevel(IMPROVABLE_ID) <= 0) return false;

        // 调用官方API添加经验
        try {
            Class<?> utilClass = Class.forName("pyre.tinkerslevellingaddon.util.ToolLevellingUtil");
            java.lang.reflect.Method addExp = utilClass.getMethod("addExperience", ToolStack.class, int.class, ServerPlayer.class);
            addExp.invoke(null, tool, amount, player);

            // 更新物品栈
            stack.setTag(tool.createStack().getTag());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}