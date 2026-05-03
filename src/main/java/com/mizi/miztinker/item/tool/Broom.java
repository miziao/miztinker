package com.mizi.miztinker.item.tool;

import com.mizi.miztinker.item.tool.until.ToolDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class Broom extends ModifiableItem {

    public Broom(Properties properties) {
        super(properties, ToolDefinitions.BROOM_TD);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && context.getHand() == player.getUsedItemHand()) {
            BlockState state = context.getLevel().getBlockState(context.getClickedPos());
            if (state.getBlock() instanceof BrushableBlock) {
                player.startUsingItem(context.getHand());
                return InteractionResult.CONSUME;
            }
        }
        return super.useOn(context);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (!(living instanceof Player player)) return;

        ToolStack tool = ToolStack.from(stack);
        float miningSpeed = tool.getStats().get(ToolStats.MINING_SPEED);

        int interval = Math.max(2, 10 - (int)(miningSpeed * 2));

        HitResult hitresult = player.pick(player.getBlockReach(), 0.0F, false);
        if (hitresult instanceof BlockHitResult blockHitResult && hitresult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHitResult.getBlockPos();
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof BrushableBlockEntity brushable) {
                int useTime = this.getUseDuration(stack) - remainingUseDuration;

                if (useTime > 0 && useTime % interval == 0) {
                    boolean finished = brushable.brush(level.getGameTime(), player, blockHitResult.getDirection());

                    if (!level.isClientSide) {
                        ToolDamageUtil.damageAnimated(tool, 1, player, player.getUsedItemHand());
                    }

                    if (finished) {
                        player.releaseUsingItem();
                    }
                }
                return;
            }
        }

        // 如果没对准可刷方块，松开动作
        player.releaseUsingItem();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // 足够长的时间
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }
}