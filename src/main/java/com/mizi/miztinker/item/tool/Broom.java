package com.mizi.miztinker.item.tool;

import com.mizi.miztinker.item.tool.until.ToolDefinitions;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class Broom extends ModifiableItem {

    public Broom(Properties properties) {
        super(properties, ToolDefinitions.BROOM_TD);
    }

    /* =======================
       ① 右键方块：开始使用
       ======================= */
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());

        // 只对可疑方块生效
        if (state.getBlock() instanceof BrushableBlock) {
            player.startUsingItem(context.getHand());
            return InteractionResult.CONSUME;
        }

        return super.useOn(context);
    }

    /* =======================
       ② 每 tick 刷洗（核心）
       ======================= */
    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {

        // 只允许玩家刷
        if (!(living instanceof Player player)) {
            return;
        }

        // 把逻辑交给原版刷子
        BrushItem brushItem = (BrushItem) Items.BRUSH;
        brushItem.onUseTick(level, living, stack, remainingUseDuration);

        // 服务端掉耐久（匠魂方式）
        if (!level.isClientSide && remainingUseDuration % 10 == 0) {
            ToolStack tool = ToolStack.from(stack);
            ToolDamageUtil.damage(tool, 1, player, stack);
        }
    }

    /* =======================
       ③ 使用时长（必须）
       ======================= */
    @Override
    public int getUseDuration(ItemStack stack) {
        return Items.BRUSH.getUseDuration(stack);
    }

    /* =======================
       ④ 使用动画（必须）
       ======================= */
    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }
}
