package com.mizi.miztinker.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CompressionSmelteryItem extends Item {
    public CompressionSmelteryItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        Level level = entity.level();
        if (entity.isInWater()) {
            CompoundTag nbt = entity.getPersistentData();
            int timer = nbt.getInt("SmelteryTimer");
            RandomSource random = level.getRandom();

            if (level.isClientSide) {
                for (int i = 0; i < 4; i++) {
                    double offsetX = (random.nextDouble() - 0.5D) * 3.0D;
                    double offsetZ = (random.nextDouble() - 0.5D) * 3.0D;

                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            entity.getX() + offsetX,
                            entity.getY() + 0.2D,
                            entity.getZ() + offsetZ,
                            0, 0.07D, 0);
                }

                if (timer % 5 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double offsetX = (random.nextDouble() - 0.5D) * 2.0D;
                        double offsetZ = (random.nextDouble() - 0.5D) * 2.0D;
                        level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                                entity.getX() + offsetX,
                                entity.getY() + 0.5D,
                                entity.getZ() + offsetZ,
                                0, 0.1D, 0);
                    }
                }

                if (timer % 20 == 0) {
                    level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.8F,
                            1.2F + random.nextFloat() * 0.4F, false);
                }
            } else {
                timer++;
                nbt.putInt("SmelteryTimer", timer);

                if (timer >= 100) {
                    generateStructure((ServerLevel) level, entity.blockPosition());
                    entity.discard();
                }
            }
        } else {
            if (entity.getPersistentData().contains("SmelteryTimer")) {
                entity.getPersistentData().putInt("SmelteryTimer", 0);
            }
        }
        return false;
    }

    private void generateStructure(ServerLevel level, BlockPos pos) {
        StructureTemplateManager manager = level.getStructureManager();

        ResourceLocation structureLocation = ResourceLocation.fromNamespaceAndPath("miztinker", "tinker");
        Optional<StructureTemplate> templateOpt = manager.get(structureLocation);

        templateOpt.ifPresent(template -> {
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(Rotation.NONE)
                    .setIgnoreEntities(false);

            BlockPos spawnPos = new BlockPos(pos.getX() - 3, pos.getY() + 1, pos.getZ() - 1);

            net.minecraft.core.Vec3i size = template.getSize();

            BlockPos endPos = spawnPos.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);

            for (BlockPos targetPos : BlockPos.betweenClosed(spawnPos, endPos)) {
                if (!level.getFluidState(targetPos).isEmpty()) {
                    level.setBlock(targetPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                }
            }

            template.placeInWorld(level, spawnPos, spawnPos, settings, level.random, 2);

            level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    spawnPos.getX() + (size.getX() / 2.0),
                    spawnPos.getY(),
                    spawnPos.getZ() + (size.getZ() / 2.0),
                    30, 1.5, 0.5, 1.5, 0.05);

            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 1, 0, 0, 0, 0);
            level.playSound(null, spawnPos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 0.5F);
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.miztinker.compression_smeltery").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}