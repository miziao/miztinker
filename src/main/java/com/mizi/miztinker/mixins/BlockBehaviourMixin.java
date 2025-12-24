package com.mizi.miztinker.mixins;


import com.mizi.miztinker.modifier.modifiers.base.GhostfreakHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehaviourMixin {

    // 辅助方法：判断关联的实体是否为幽灵模式下的玩家
    private boolean isGhost(CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            return entity instanceof Player player && GhostfreakHelper.isGhostActive(player);
        }
        return false;
    }

    /**
     * 1. 核心碰撞箱：决定实体是否能“站”在上面或被“挡住”
     */
    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true)
    private void onGetCollisionShape(BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (isGhost(context)) {
            cir.setReturnValue(Shapes.empty());
        }
    }

    /**
     * 2. 视觉/点击形状：这能防止幽灵玩家被非完整方块的特殊选框卡住
     * 针对某些栅栏或杆状物，这一步可以确保移动逻辑完全忽略它们
     */
    @Inject(method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true)
    private void onGetShape(BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (isGhost(context)) {
            cir.setReturnValue(Shapes.empty());
        }
    }
}