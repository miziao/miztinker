package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Command extends NoLevelsModifier implements EquipmentChangeModifierHook {
    private static final Set<UUID> temporaryOps = new HashSet<>();

    public Command() {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE);
    }

    @Override
    public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        if (context.getEntity() instanceof ServerPlayer player) {
            grantCommandPermissions(player);
        }
    }

    @Override
    public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        if (context.getEntity() instanceof ServerPlayer player) {
            player.server.execute(() -> {
                if (!hasCommandModifierEquipped(player)) {
                    revokeCommandPermissions(player);
                }
            });
        }
    }

    private boolean hasCommandModifierEquipped(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (ModifierUtil.getModifierLevel(player.getItemBySlot(slot), this.getId()) > 0) return true;
        }
        return ModifierUtil.getModifierLevel(player.getMainHandItem(), this.getId()) > 0 ||
                ModifierUtil.getModifierLevel(player.getOffhandItem(), this.getId()) > 0;
    }

    private void grantCommandPermissions(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!player.server.getPlayerList().isOp(player.getGameProfile()) && !temporaryOps.contains(uuid)) {
            temporaryOps.add(uuid);
            player.server.getPlayerList().op(player.getGameProfile());
            syncPlayerCommands(player);
        }
    }

    private void revokeCommandPermissions(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (temporaryOps.remove(uuid)) {
            player.server.getPlayerList().deop(player.getGameProfile());
            syncPlayerCommands(player);
        }
    }

    private void syncPlayerCommands(ServerPlayer player) {
        player.server.getCommands().sendCommands(player);
        player.getAbilities().mayBuild = player.createCommandSourceStack().hasPermission(2);
        player.onUpdateAbilities();
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player && event.getPlacedBlock().getBlock() instanceof CommandBlock) {
            if (hasCommandModifierEquipped(player)) event.setCanceled(false);
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getLevel();
        Player player = event.getEntity();
        BlockState state = world.getBlockState(event.getPos());

        if (state.getBlock() instanceof CommandBlock && hasCommandModifierEquipped(player)) {
            BlockEntity be = world.getBlockEntity(event.getPos());
            if (be instanceof CommandBlockEntity commandBe) {
                if (world.isClientSide) {
                    player.openCommandBlock(commandBe);
                } else {
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }
}