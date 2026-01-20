package com.mizi.miztinker.item.tool;

import com.mizi.miztinker.item.tool.until.ToolDefinitions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.helper.TooltipBuilder;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;

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
    public List<Component> getStatInformation(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
        TooltipBuilder builder = new TooltipBuilder(tool, tooltips);
        if (tool.hasTag(TinkerTags.Items.DURABILITY)) builder.add(ToolStats.DURABILITY);
        if (tool.hasTag(TinkerTags.Items.HARVEST)) {
            builder.add(ToolStats.HARVEST_TIER);
            builder.add(ToolStats.MINING_SPEED);
        }
        tooltips.add(Component.translatable("item.miztinker.broom.description").withStyle(ChatFormatting.AQUA));

        builder.addAllFreeSlots();
        return tooltips;
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
