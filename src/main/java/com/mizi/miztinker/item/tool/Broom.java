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

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {

        if (!(living instanceof Player player)) {
            return;
        }

        BrushItem brushItem = (BrushItem) Items.BRUSH;
        brushItem.onUseTick(level, living, stack, remainingUseDuration);

        if (!level.isClientSide && remainingUseDuration % 10 == 0) {
            ToolStack tool = ToolStack.from(stack);
            ToolDamageUtil.damage(tool, 1, player, stack);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return Items.BRUSH.getUseDuration(stack);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }
}
