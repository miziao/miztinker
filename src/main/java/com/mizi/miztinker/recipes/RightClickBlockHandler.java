package com.mizi.miztinker.recipes;

import com.mizi.miztinker.recipes.rules.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = "miztinker")
public class RightClickBlockHandler {

    private static final List<IBlockInteractRule> RULES = Arrays.asList(
            new NetherReactorRule(),
            new FumoGoldRule(),
            new MendingMossRule()
    );

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        ItemStack held = player.getMainHandItem();

        ResourceLocation blockId = level.registryAccess()
                .registryOrThrow(Registries.BLOCK)
                .getKey(state.getBlock());

        ResourceLocation heldId = held.isEmpty() ? null :
                level.registryAccess()
                        .registryOrThrow(Registries.ITEM)
                        .getKey(held.getItem());

        for (IBlockInteractRule rule : RULES) {
            if (rule.matches(state, blockId, held, heldId)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                rule.execute(player, level, event.getPos(), held);
                break;
            }
        }
    }
}