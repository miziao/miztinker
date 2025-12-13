package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.network.packets.PlaySoundPacket;
import com.mizi.miztinker.sounds.MiztinkerSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;
import java.util.List;

public class Plumber extends NoLevelsModifier implements InventoryTickModifierHook {

    private static final double MIN_FALL_SPEED = 0.5;
    private static final double BOUNCE_UP = 1.0;
    private static final float DAMAGE = 10000f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!(holder instanceof Player player)) return;
        if (!isCorrectSlot) return;
        if (player.isFallFlying()) return;

        Vec3 motion = player.getDeltaMovement();
        if (motion.y > -MIN_FALL_SPEED) return;

        List<LivingEntity> entities = world.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(0.5, 0.1, 0.5),
                e -> e != player && player.getY() > e.getY() + e.getBbHeight() * 0.5
        );

        for (LivingEntity entity : entities) {

            entity.hurt(player.damageSources().playerAttack(player), DAMAGE);

            player.setDeltaMovement(motion.x, BOUNCE_UP, motion.z);

            // 🔥 使用网络包播放音效
            notifyPlayersSound(player, MiztinkerSounds.MARIO.get().getLocation(), 1.0f, 1.0f);

            break;
        }
    }

    /**
     * 🔥 给范围内的所有玩家播放音效（服务器端执行）
     */
    private void notifyPlayersSound(Entity source, ResourceLocation soundRL, float volume, float pitch) {
        if (source.level().isClientSide()) return;

        List<ServerPlayer> players = source.level().getEntitiesOfClass(
                ServerPlayer.class,
                source.getBoundingBox().inflate(20)
        );

        for (ServerPlayer serverPlayer : players) {
            MiztinkerNetwork.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new PlaySoundPacket(
                            source.position(),   // ✔ 声音从事件中心发出
                            soundRL,             // ✔ 使用传入参数，而不是写死
                            volume,
                            pitch
                    )
            );
        }
    }
}
