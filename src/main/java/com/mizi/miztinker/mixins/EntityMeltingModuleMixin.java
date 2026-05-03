package com.mizi.miztinker.mixins;

import com.mizi.miztinker.util.SmelteryComponentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.EntityMeltingModule;

import java.util.function.Supplier;

@Mixin(value = EntityMeltingModule.class, remap = false)
public abstract class EntityMeltingModuleMixin {

    @Shadow @Final private MantleBlockEntity parent;

    @Shadow @Final private Supplier<AABB> bounds;

    @Inject(method = "interactWithEntities", at = @At("HEAD"), cancellable = true)
    private void mizi$preemptInteracting(CallbackInfoReturnable<Boolean> cir) {
        if (SmelteryComponentHelper.isNutrientModuleActive(this.parent)) {

            Level level = this.parent.getLevel();
            if (level == null || level.isClientSide) return;

            AABB boundingBox = this.bounds.get();
            if (boundingBox != null) {
                for (Entity entity : level.getEntitiesOfClass(Entity.class, boundingBox)) {
                    if (entity.isAlive() && entity instanceof LivingEntity living) {
                        float healAmount = 2.0f;

                        living.heal(healAmount);

                        if (living instanceof Player player) {
                            player.getFoodData().eat((int)healAmount, healAmount);
                        }
                    }
                }
            }

            cir.setReturnValue(false);
        }
    }
}