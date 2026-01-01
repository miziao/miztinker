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

import static com.mizi.miztinker.miztinker.getResource;

public class NBT_Microscope extends NoLevelsModifier implements
        GeneralInteractionModifierHook,
        EntityInteractionModifierHook,
        BlockInteractionModifierHook {

    private static final String MICROSCOPE_ACTIVE = "microscope_active";

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
        if (event.getButton() != 1) return;

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
        if (checkStack(player.getMainHandItem())) return true;
        if (checkStack(player.getOffhandItem())) return true;
        for (ItemStack stack : player.getInventory().items) {
            if (checkStack(stack)) return true;
        }
        return false;
    }

    private boolean checkStack(ItemStack stack) {
        if (stack.isEmpty() || !stack.getOrCreateTag().contains("tic_modifiers")) return false;

        ToolStack tool = ToolStack.from(stack);
        if (tool.getModifierLevel(this) > 0) {
            return tool.getPersistentData().getBoolean(getResource(MICROSCOPE_ACTIVE));
        }
        return false;
    }


    @Override
    public InteractionResult afterEntityUse(IToolStackView tool, ModifierEntry modifier, Player player, LivingEntity target, InteractionHand hand, InteractionSource source) {
        if (!tool.getPersistentData().getBoolean(getResource(MICROSCOPE_ACTIVE)) || player.level().isClientSide) return InteractionResult.PASS;
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (entityId != null) {
            sendIdMessage(player, "Entity", entityId.toString());
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.5f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        Player player = context.getPlayer();
        if (player == null || !tool.getPersistentData().getBoolean(getResource(MICROSCOPE_ACTIVE)) || player.level().isClientSide) return InteractionResult.PASS;
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(player.level().getBlockState(context.getClickedPos()).getBlock());
        if (blockId != null) {
            sendIdMessage(player, "Block", blockId.toString());
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
            boolean nowActive = !data.getBoolean(getResource(MICROSCOPE_ACTIVE));
            data.putBoolean(getResource(MICROSCOPE_ACTIVE), nowActive);

            player.displayClientMessage(Component.literal("§6[NBT显微镜] " + (nowActive ? "§b已开启" : "§7已关闭")), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, nowActive ? 1.2f : 0.8f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void sendIdMessage(Player player, String type, String idStr) {
        player.sendSystemMessage(Component.literal("§7[" + type + " ID] §b" + idStr).withStyle(s -> s
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, idStr))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击复制注册名")))));
    }

    private void inspectItem(Player player, ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String idStr = (id != null) ? id.toString() : "unknown";
        player.sendSystemMessage(Component.literal("§7[Item ID] §b" + idStr).withStyle(s -> s
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, idStr))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击复制注册名")))));

        stack.getTags().forEach(tagKey -> {
            String tagStr = "#" + tagKey.location().toString();
            player.sendSystemMessage(Component.literal("§7[Tag] §d" + tagStr).withStyle(s -> s
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, tagStr))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击复制该 Tag")))));
        });

        CompoundTag nbt = stack.getTag();
        if (nbt != null) {
            String nbtStr = nbt.toString();
            player.sendSystemMessage(Component.literal("§7[NBT] §f" + (nbtStr.length() > 60 ? nbtStr.substring(0, 57) + "..." : nbtStr))
                    .append(Component.literal(" §6[复制]").withStyle(s -> s
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbtStr))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击复制完整 NBT"))))));
        } else {
            player.sendSystemMessage(Component.literal("§8该物品无 NBT"));
        }
    }
}