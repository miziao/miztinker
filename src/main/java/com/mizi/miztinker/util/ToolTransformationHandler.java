package com.mizi.miztinker.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "miztinker")
public class ToolTransformationHandler {

    private static final ConcurrentHashMap<UUID, ItemStack> LAST_HELD_ITEM = new ConcurrentHashMap<>();

    private static final String IRON_ID = "iron";
    private static final MaterialId HEAVY_IRON_ID = new MaterialId("miztinker", "heavy_iron");
    private static final MaterialVariantId HEAVY_IRON = MaterialVariantId.create(HEAVY_IRON_ID, "default");
    private static final MaterialVariantId SCRAP = MaterialVariantId.create(new MaterialId("miztinker", "scrap"), "default");

    private static final ModifierId MAGNETIC_FORCE_ID = new ModifierId("miztinker", "magnetic_force");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) {
            return;
        }

        Player player = event.player;
        ItemStack currentStack = player.getMainHandItem();
        ItemStack lastStack = LAST_HELD_ITEM.get(player.getUUID());

        if (currentStack != lastStack) {
            LAST_HELD_ITEM.put(player.getUUID(), currentStack);

            if (!currentStack.isEmpty() && ToolStack.isInitialized(currentStack)) {
                ToolStack tool = ToolStack.from(currentStack);
                processToolTransformation(tool, player);
            }
        }
    }

    private static void processToolTransformation(ToolStack tool, Player player) {
        MaterialNBT materials = tool.getMaterials();

        for (int i = 0; i < materials.size(); i++) {
            if (materials.get(i).getVariant().getId().equals(HEAVY_IRON_ID)) {
                return;
            }
        }

        List<Integer> ironIndices = new ArrayList<>();
        for (int i = 0; i < materials.size(); i++) {
            if (materials.get(i).getVariant().getId().getPath().equals(IRON_ID)) {
                ironIndices.add(i);
            }
        }

        if (ironIndices.size() >= 2) {
            int originalIronCount = ironIndices.size();

            tool.replaceMaterial(ironIndices.get(0), HEAVY_IRON);
            for (int j = 1; j < ironIndices.size(); j++) {
                tool.replaceMaterial(ironIndices.get(j), SCRAP);
            }

            int bonusLevel = originalIronCount - 1;
            tool.addModifier(MAGNETIC_FORCE_ID, bonusLevel);

            tool.rebuildStats();

            Level world = player.level();
            float[] pitches = {0.6F, 1.0F, 1.4F};
            for (float p : pitches) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.ANVIL_LAND,
                        net.minecraft.sounds.SoundSource.PLAYERS,
                        1.2F, p);
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.5F, 1.5F);

            player.displayClientMessage(Component.literal("§6 100万匹磁场转动!分子重组!"), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_HELD_ITEM.remove(event.getEntity().getUUID());
    }
}