package com.mizi.miztinker.item.tool;

import com.mizi.miztinker.item.tool.until.ToolDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.helper.TooltipBuilder;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.shared.TinkerCommons;

import java.util.Iterator;
import java.util.List;


public class lollipop extends ModifiableItem {
    public lollipop(Properties properties) {
        super(properties, ToolDefinitions.LOLLIPOP_TD);
    }
    @Override
    public boolean mineBlock(ItemStack p_41416_, Level p_41417_, BlockState p_41418_, BlockPos p_41419_, LivingEntity p_41420_) {
        return true;
    }

    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        ToolStack tool=ToolStack.from(stack);
        return tool.getStats().get(ToolStats.MINING_SPEED);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    private static final Lazy<ItemStack> BACON_STACK = Lazy.of(() -> new ItemStack(TinkerCommons.bacon));
    private void eat(IToolStackView tool, LivingEntity entity) {
        if (entity instanceof Player player && player.canEat(false)) {
            Level world = entity.level();
            int a = 6;
            player.getFoodData().eat(a, a*0.5f);
            ModifierUtil.foodConsumer.onConsume(player, BACON_STACK.get(), a, 0.6F);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.4F);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
            if (ToolDamageUtil.directDamage(tool, a*3 , player, player.getUseItem())) {
                player.broadcastBreakEvent(player.getUsedItemHand());
            }
        }
    }

    public List<Component> getStatInformation(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
        tooltips = this.getTooltipStats(tool, player, tooltips, key, tooltipFlag);
        return tooltips;
    }
    public List<Component> getTooltipStats(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
        TooltipBuilder builder = new TooltipBuilder(tool, tooltips);
        if (tool.hasTag(TinkerTags.Items.DURABILITY)) {
            builder.add(ToolStats.DURABILITY);
        }
        if (tool.hasTag(TinkerTags.Items.MELEE)) {
            builder.add(ToolStats.ATTACK_DAMAGE);
            builder.add(ToolStats.ATTACK_SPEED);
        }
        if (tool.hasTag(TinkerTags.Items.HARVEST)) {
            builder.add(ToolStats.HARVEST_TIER);
            builder.add(ToolStats.MINING_SPEED);
        }
        builder.addAllFreeSlots();
        for (ModifierEntry entry : tool.getModifierList()) {
            entry.getHook(ModifierHooks.TOOLTIP).addTooltip(tool, entry, player, tooltips, key, tooltipFlag);
        }
        return tooltips;
    }
}