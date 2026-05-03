package com.mizi.miztinker.recipes;

import com.mizi.miztinker.modifier.register.MiztinkerItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "miztinker")
public class BlockDropEventHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide() || event.getPlayer().isCreative()) {
            return;
        }

        BlockState state = event.getState();

        if (state.is(BlockTags.LOGS)) {

            if (RANDOM.nextFloat() < 0.002f) {
                Level level = (Level) event.getLevel();
                BlockPos pos = event.getPos();

                ItemStack dropStack = new ItemStack(MiztinkerItems.LONGINUS_KAI.get());

                ItemEntity itemEntity = new ItemEntity(
                        level,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        dropStack
                );

                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
    }
}