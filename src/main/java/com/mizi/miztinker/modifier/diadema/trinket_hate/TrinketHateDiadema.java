package com.mizi.miztinker.modifier.diadema.trinket_hate;

import com.csdy.tcondiadema.frames.diadema.Diadema;
import com.csdy.tcondiadema.frames.diadema.DiademaType;
import com.csdy.tcondiadema.frames.diadema.movement.DiademaMovement;
import com.csdy.tcondiadema.frames.diadema.range.DiademaRange;
import com.csdy.tcondiadema.diadema.api.ranges.SphereDiademaRange;
import lombok.NonNull;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class TrinketHateDiadema extends Diadema {

    private static final double RADIUS = 8.0;
    private static final int TICK_INTERVAL = 10;
    private int tickCounter = 0;

    public TrinketHateDiadema(DiademaType type, DiademaMovement movement) {
        super(type, movement);
    }

    private final SphereDiademaRange range = new SphereDiademaRange(this, RADIUS);

    @Override
    public @NonNull DiademaRange getRange() {
        return range;
    }

    @Override
    protected void perTick() {
        if (getLevel().isClientSide) {
            return;
        }

        tickCounter++;
        if (tickCounter >= TICK_INTERVAL) {
            tickCounter = 0;
            executeTrinketAnnihilation();
        }
    }

    private void executeTrinketAnnihilation() {
        Entity holder = getCoreEntity();
        if (holder == null) return;

        for (Entity entity : affectingEntities) {

            if (entity instanceof Player target) {
                double yDiff = Math.abs(target.getY() - holder.getY());
                if (yDiff > 4.0) continue;

                CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
                    if (annihilateCurios(handler)) {
                        getLevel().playSound(null, target.getX(), target.getY(), target.getZ(),
                                SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 0.5F);
                    }
                });
            }
        }
    }

    private boolean annihilateCurios(ICuriosItemHandler handler) {
        boolean[] hasAnnihilated = {false};
        handler.getCurios().values().forEach(stacksHandler -> {
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            IDynamicStackHandler cosmetics = stacksHandler.getCosmeticStacks();

            for (int i = 0; i < stacks.getSlots(); i++) {
                if (!stacks.getStackInSlot(i).isEmpty()) {
                    stacks.setStackInSlot(i, ItemStack.EMPTY);
                    hasAnnihilated[0] = true;
                }
            }
            for (int i = 0; i < cosmetics.getSlots(); i++) {
                if (!cosmetics.getStackInSlot(i).isEmpty()) {
                    cosmetics.setStackInSlot(i, ItemStack.EMPTY);
                    hasAnnihilated[0] = true;
                }
            }
        });
        return hasAnnihilated[0];
    }
}