package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.EnchantmentModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;

public class Terastal_Luck extends Modifier implements
        BlockBreakModifierHook, MeleeHitModifierHook, TooltipModifierHook {

    private static final ResourceLocation TERA_PROGRESS = ResourceLocation.fromNamespaceAndPath("miztinker", "tera_progress");
    private static final ResourceLocation TERA_ACTIVE = ResourceLocation.fromNamespaceAndPath("miztinker", "tera_active");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    private int getTotalLuckLevel(IToolStackView tool, boolean isLooting) {
        if (tool instanceof ToolStack ts) {
            return EnchantmentModifierHook.getEnchantmentLevel(ts.createStack(), isLooting ? Enchantments.MOB_LOOTING : Enchantments.BLOCK_FORTUNE);
        }
        return 0;
    }

    private int getRequiredFragments(int level) {
        return Math.max(1, (int) (50 / Math.pow(2, level - 1)));
    }

    private void tryAddProgress(IToolStackView tool, ModifierEntry modifier, @Nullable Player player) {
        if (player == null || player.level().isClientSide) return;
        ModDataNBT data = tool.getPersistentData();
        if (data.getBoolean(TERA_ACTIVE)) return;

        int current = data.getInt(TERA_PROGRESS) + 1;
        int required = getRequiredFragments(modifier.getLevel());

        if (current >= required) {
            data.putInt(TERA_PROGRESS, 0);
            data.putBoolean(TERA_ACTIVE, true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.2f);
            player.displayClientMessage(Component.translatable("message.miztinker.terastal_luck.ready")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), true);
        } else {
            data.putInt(TERA_PROGRESS, current);
        }
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        Player player = context.getPlayer();
        if (player == null || player.level().isClientSide) return;
        ModDataNBT data = tool.getPersistentData();

        if (data.getBoolean(TERA_ACTIVE)) {
            boolean burstTriggered = spawnExtraBlockDrops(tool, context);
            if (burstTriggered) {
                data.putBoolean(TERA_ACTIVE, false);
            }
        } else {
            tryAddProgress(tool, modifier, player);
        }
    }

    private boolean spawnExtraBlockDrops(IToolStackView tool, ToolHarvestContext context) {
        ServerLevel level = context.getWorld();
        BlockState state = context.getState();
        int fortune = getTotalLuckLevel(tool, false);

        ItemStack toolStack = (tool instanceof ToolStack ts) ? ts.createStack() : ItemStack.EMPTY;

        List<ItemStack> fortuneDrops = Block.getDrops(state, level, context.getPos(), null, context.getPlayer(), toolStack);
        List<ItemStack> normalDrops = Block.getDrops(state, level, context.getPos(), null, context.getPlayer(), ItemStack.EMPTY);

        if (isSameDrops(fortuneDrops, normalDrops)) {
            return false;
        }

        for (ItemStack stack : fortuneDrops) {
            if (stack.isEmpty()) continue;
            int extraCount = stack.getCount() * (fortune + 1);
            ItemStack extraStack = stack.copy();
            extraStack.setCount(extraCount);

            ItemEntity entity = new ItemEntity(level, context.getPos().getX() + 0.5, context.getPos().getY() + 0.5, context.getPos().getZ() + 0.5, extraStack);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }

        level.playSound(null, context.getPos(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 1.0f, 1.5f);
        if (context.getPlayer() != null) {
            context.getPlayer().displayClientMessage(Component.translatable("message.miztinker.terastal_luck.burst")
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC), true);
        }
        return true;
    }

    private boolean isSameDrops(List<ItemStack> drops1, List<ItemStack> drops2) {
        if (drops1.size() != drops2.size()) return false;
        for (int i = 0; i < drops1.size(); i++) {
            if (!ItemStack.isSameItemSameTags(drops1.get(i), drops2.get(i)) ||
                    drops1.get(i).getCount() != drops2.get(i).getCount()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        Player player = context.getAttacker() instanceof Player ? (Player) context.getAttacker() : null;

        if (player != null && target != null) {
            ModDataNBT data = tool.getPersistentData();
            if (!target.isAlive()) {
                if (data.getBoolean(TERA_ACTIVE)) {
                    spawnExtraMobDrops(tool, target, player);
                    data.putBoolean(TERA_ACTIVE, false);
                } else {
                    tryAddProgress(tool, modifier, player);
                }
            } else {
                tryAddProgress(tool, modifier, player);
            }
        }
    }

    private void spawnExtraMobDrops(IToolStackView tool, LivingEntity target, Player player) {
        if (!(target.level() instanceof ServerLevel level)) return;

        int looting = getTotalLuckLevel(tool, true);
        DamageSource ds = level.damageSources().playerAttack(player);

        ResourceLocation lootTableRes = target.getLootTable();
        LootTable table = level.getServer().getLootData().getLootTable(lootTableRes);

        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, target)
                .withParameter(LootContextParams.ORIGIN, target.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, ds)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, player)
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, player)
                .withLuck(player.getLuck());

        List<ItemStack> rawDrops = table.getRandomItems(builder.create(LootContextParamSets.ENTITY));

        for (ItemStack stack : rawDrops) {
            if (stack.isEmpty()) continue;
            int burstCount = stack.getCount() * (looting + 1);
            ItemStack burstStack = stack.copy();
            burstStack.setCount(burstCount);

            ItemEntity entity = new ItemEntity(level, target.getX(), target.getY(), target.getZ(), burstStack);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }

        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        ModDataNBT data = tool.getPersistentData();
        if (data.getBoolean(TERA_ACTIVE)) {
            tooltip.add(Component.translatable("tooltip.miztinker.terastal_luck.active").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        } else {
            int current = data.getInt(TERA_PROGRESS);
            int required = getRequiredFragments(modifier.getLevel());
            tooltip.add(Component.translatable("tooltip.miztinker.terastal_luck.progress", current, required).withStyle(ChatFormatting.GRAY));
        }
    }
}