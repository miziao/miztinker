package com.mizi.miztinker.recipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = "miztinker")
public class RightClickBlockHandler {

    private static final Map<Integer, TimerData> timers = new ConcurrentHashMap<>();

    /** 定时任务结构：延迟执行传送/效果 */
    static class TimerData {
        final WeakReference<Player> playerRef;
        final long startTick;
        final Runnable effect;

        TimerData(Player player, long startTick, Runnable effect) {
            this.playerRef = new WeakReference<>(player);
            this.startTick = startTick;
            this.effect = effect;
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        ItemStack held = player.getMainHandItem();

        ResourceLocation blockId = level.registryAccess().registryOrThrow(Registries.BLOCK).getKey(block);
        ResourceLocation heldId = held.isEmpty() ? null :
                level.registryAccess().registryOrThrow(Registries.ITEM).getKey(held.getItem());

        /*---------------------------------
         * ✅ 空手右键 Nether_Reactor 触发传送
         *--------------------------------*/
        if (held.isEmpty() && new ResourceLocation("miztinker:nether_reactor").equals(blockId)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            timers.put(player.getId(), new TimerData(player, level.getGameTime(), () -> {
                if (!(level instanceof ServerLevel)) return;
                if (player.getServer() == null) return;

                // 获取下界维度
                ServerLevel nether = player.getServer().getLevel(Level.NETHER);
                if (nether == null) {
                    // 若无下界维度，则回主世界出生点
                    ServerLevel overworld = player.getServer().overworld();
                    player.changeDimension(overworld, new ITeleporter() {
                        @Override
                        public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld,
                                                  float yaw, Function<Boolean, Entity> repositionEntity) {
                            Entity e = repositionEntity.apply(false);
                            e.setPos(
                                    overworld.getSharedSpawnPos().getX() + 0.5,
                                    overworld.getSharedSpawnPos().getY(),
                                    overworld.getSharedSpawnPos().getZ() + 0.5
                            );
                            return e;
                        }
                    });
                    player.displayClientMessage(Component.literal("§7你可能正在游玩过于古老的移动版,不存在下界这个维度"), true);
                    return;
                }

                // ✅ 在下界随机生成位置（±128格范围）
                double randX = (nether.random.nextDouble() - 0.5) * 256.0;
                double randZ = (nether.random.nextDouble() - 0.5) * 256.0;
                double fixedY = 100.0; // ✅ 固定Y高度

                // ✅ 执行传送（不继承主世界坐标）
                player.changeDimension(nether, new ITeleporter() {
                    @Override
                    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld,
                                              float yaw, Function<Boolean, Entity> repositionEntity) {
                        Entity e = repositionEntity.apply(false);
                        e.setPos(randX, fixedY, randZ);
                        e.setYRot(player.getYRot());
                        e.setXRot(player.getXRot());
                        return e;
                    }
                });

                // ✅ 粒子 + 声音效果
                for (int i = 0; i < 40; i++) {
                    double px = randX + (nether.random.nextDouble() - 0.5) * 2.0;
                    double py = fixedY + nether.random.nextDouble() * 1.5;
                    double pz = randZ + (nether.random.nextDouble() - 0.5) * 2.0;

                    nether.sendParticles(ParticleTypes.FLAME, px, py, pz, 1, 0, 0, 0, 0.01);
                    nether.sendParticles(ParticleTypes.LAVA, px, py, pz, 1, 0, 0, 0, 0.01);
                    nether.sendParticles(ParticleTypes.SMOKE, px, py, pz, 1, 0, 0, 0, 0.01);
                }

                nether.playSound(null, randX, fixedY, randZ,
                        SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1.0f, 1.0f);
            }));
        }

        /*---------------------------------
         * ✅ 手持金锭右键书架 → 转化 Fumo 金锭
         *--------------------------------*/
        if (blockId != null && heldId != null && heldId.equals(new ResourceLocation("minecraft:gold_ingot")) &&
                blockId.equals(new ResourceLocation("minecraft:bookshelf"))) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            ItemStack fumoStack = new ItemStack(
                    Objects.requireNonNull(level.registryAccess().registryOrThrow(Registries.ITEM)
                            .get(new ResourceLocation("miztinker:fumo_gold_ingot"))),
                    held.getCount()
            );

            if (!player.getInventory().add(fumoStack)) {
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.addFreshEntity(new ItemEntity(serverLevel, player.getX(), player.getY(), player.getZ(), fumoStack));
                }
            }

            held.shrink(held.getCount());

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1, player.getZ(),
                        10, 0.25, 0.25, 0.25, 0.01
                );
            }
        }
    }

    /** 定时器执行（2秒后触发） */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.player.level() instanceof ServerLevel level)) return;

        long gameTime = level.getGameTime();
        Iterator<Map.Entry<Integer, TimerData>> it = timers.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, TimerData> entry = it.next();
            TimerData td = entry.getValue();
            Player player = td.playerRef.get();

            if (player == null || player.isRemoved()) {
                it.remove();
                continue;
            }

            if (gameTime - td.startTick >= 40L) { // 2秒后执行
                td.effect.run();
                it.remove();
            } else if ((gameTime - td.startTick) % 20 == 0) {
                level.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1, player.getZ(),
                        6, 0.2, 0.2, 0.2, 0.01
                );
            }
        }
    }
}