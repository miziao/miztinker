package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.*;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class NBT_Microscope extends NoLevelsModifier implements
        GeneralInteractionModifierHook,
        EntityInteractionModifierHook,
        BlockInteractionModifierHook {

    public static final ResourceLocation MICROSCOPE_ACTIVE = ResourceLocation.fromNamespaceAndPath("mizi", "microscope_active");

    public NBT_Microscope() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.ENTITY_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_INTERACT);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onScreenMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 1) return; // 右键

        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            Slot slot = screen.getSlotUnderMouse();
            if (slot != null && !slot.getItem().isEmpty()) {
                Player player = Minecraft.getInstance().player;
                if (player != null && isAnyMicroscopeActive(player)) {
                    inspectItem(player, slot.getItem());
                    player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.5f, 1.5f);
                    event.setCanceled(true);
                }
            }
        }
    }

    private boolean isAnyMicroscopeActive(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (checkStackActive(stack)) return true;
        }
        return false;
    }

    private boolean checkStackActive(ItemStack stack) {
        if (stack.isEmpty() || !stack.getOrCreateTag().contains("tic_modifiers")) return false;
        ToolStack tool = ToolStack.from(stack);
        if (tool.getModifierLevel(this) > 0) {
            return tool.getPersistentData().getBoolean(MICROSCOPE_ACTIVE);
        }
        return false;
    }

    @Override
    public InteractionResult afterEntityUse(IToolStackView tool, ModifierEntry modifier, Player player, LivingEntity target, InteractionHand hand, InteractionSource source) {
        if (!tool.getPersistentData().getBoolean(MICROSCOPE_ACTIVE) || player.level().isClientSide) return InteractionResult.PASS;

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (entityId != null) {
            sendIdMessage(player, "entity", entityId.toString());
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.5f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        Player player = context.getPlayer();
        if (player == null || !tool.getPersistentData().getBoolean(MICROSCOPE_ACTIVE) || player.level().isClientSide) return InteractionResult.PASS;

        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(player.level().getBlockState(context.getClickedPos()).getBlock());
        if (blockId != null) {
            sendIdMessage(player, "block", blockId.toString());
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.5f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult onToolUse(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, Player player, @NotNull InteractionHand hand, @NotNull InteractionSource source) {
        if (player.level().isClientSide) return InteractionResult.PASS;

        if (source == InteractionSource.RIGHT_CLICK && player.isCrouching()) {
            ModDataNBT data = tool.getPersistentData();
            boolean nowActive = !data.getBoolean(MICROSCOPE_ACTIVE);
            data.putBoolean(MICROSCOPE_ACTIVE, nowActive);

            Component status = nowActive ?
                    Component.translatable("message.miztinker.microscope.active").withStyle(s -> s.withColor(0x55FFFF)) :
                    Component.translatable("message.miztinker.microscope.inactive").withStyle(s -> s.withColor(0xAAAAAA));

            player.displayClientMessage(Component.translatable("message.miztinker.microscope.prefix").append(status), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, nowActive ? 1.2f : 0.8f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void sendIdMessage(Player player, String type, String idStr) {
        player.sendSystemMessage(Component.translatable("message.miztinker.microscope.id_display", type.toUpperCase(), idStr)
                .withStyle(s -> s.withColor(0x55FFFF)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, idStr))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.miztinker.microscope.click_to_copy")))));
    }

    private void inspectItem(Player player, ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String idStr = (id != null) ? id.toString() : "unknown";

        player.sendSystemMessage(Component.translatable("message.miztinker.microscope.item_id", idStr)
                .withStyle(s -> s.withColor(0x55FFFF)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, idStr))));

        stack.getTags().forEach(tagKey -> {
            String tagStr = "#" + tagKey.location().toString();
            player.sendSystemMessage(Component.translatable("message.miztinker.microscope.tag_display", tagStr)
                    .withStyle(s -> s.withColor(0xFF55FF)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, tagStr))));
        });

        CompoundTag nbt = stack.getTag();
        if (nbt != null) {
            String nbtStr = nbt.toString();
            String displayNbt = nbtStr.length() > 60 ? nbtStr.substring(0, 57) + "..." : nbtStr;

            player.sendSystemMessage(Component.translatable("message.miztinker.microscope.nbt_display", displayNbt)
                    .append(Component.translatable("message.miztinker.microscope.copy_btn").withStyle(s -> s.withColor(0xFFAA00)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbtStr))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.miztinker.microscope.copy_nbt_hover"))))));
        } else {
            player.sendSystemMessage(Component.translatable("message.miztinker.microscope.no_nbt").withStyle(s -> s.withColor(0x888888)));
        }
    }
}