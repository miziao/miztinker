package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Objects;

public class EZ_Constant extends NoLevelsModifier implements InventoryTickModifierHook {

    private static final String TAG_BROADCAST = "ez_constant_has_broadcast";
    private static final String TAG_DIED = "ezc_died";

    public EZ_Constant() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    /** 进入世界、换维度后广播一次 & 血量为0触发掉落 */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world,
                                LivingEntity holder, int slot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        if (world.isClientSide) return;
        if (!(holder instanceof ServerPlayer player)) return;

        var tag = player.getPersistentData();

        // =================== 进入世界广播一次 ===================
        if (!tag.getBoolean(TAG_BROADCAST)) {
            Objects.requireNonNull(world.getServer()).getPlayerList().broadcastSystemMessage(
                    Component.literal("来试一下米妮"),
                    false
            );
            tag.putBoolean(TAG_BROADCAST, true);
        }

        // =================== 玩家血量为0触发掉落 ===================
        if (player.getHealth() <= 0 && !tag.getBoolean(TAG_DIED)) {
            tag.putBoolean(TAG_DIED, true);

            ServerLevel serverWorld = (ServerLevel) player.getCommandSenderWorld();
            BlockPos pos = player.blockPosition();

            // 广播死亡消息
            if (player.getServer() != null) {
                player.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("完了完了,我没有史莱姆ang"),
                        false
                );
            }

            // 掉落物品
            drop(serverWorld, pos, "minecraft", "netherite_ingot", 591);
            drop(serverWorld, pos, "minecraft", "gold_ingot", 60);
            drop(serverWorld, pos, "iceandfire", "silver_ingot", 15);
            drop(serverWorld, pos, "minecraft", "copper_ingot", 3);
        }
    }

    // ======================= 工具方法 =======================
    private boolean playerHasThisModifier(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items)
            if (itemHasModifier(stack)) return true;
        for (ItemStack stack : player.getInventory().armor)
            if (itemHasModifier(stack)) return true;
        for (ItemStack stack : player.getInventory().offhand)
            if (itemHasModifier(stack)) return true;
        return false;
    }

    private boolean itemHasModifier(ItemStack stack) {
        if (!(stack.getItem() instanceof ModifiableItem)) return false;
        ToolStack tool = ToolStack.from(stack);
        return tool.getModifierLevel(getId()) > 0;
    }

    private void drop(ServerLevel world, BlockPos pos, String namespace, String itemName, int count) {
        ItemStack stack = new ItemStack(Objects.requireNonNull(
                ForgeRegistries.ITEMS.getValue(new ResourceLocation(namespace, itemName))
        ), count);
        world.addFreshEntity(new ItemEntity(
                world,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                stack
        ));
    }
}