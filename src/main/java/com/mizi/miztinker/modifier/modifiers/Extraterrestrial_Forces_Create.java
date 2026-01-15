package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.List;
import java.util.Optional;

import static com.mizi.miztinker.miztinker.getResource;

public class Extraterrestrial_Forces_Create extends NoLevelsModifier implements SlotStackModifierHook, GeneralInteractionModifierHook, InventoryTickModifierHook {

    private static final String MODE_KEY = "create_mode";
    private static final int DRILL = 0;
    private static final int DEPLOYER = 1;
    private static final int FAN = 2;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, @NotNull Level world, @NotNull LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, @NotNull ItemStack stack) {
        if (world.isClientSide || !(holder instanceof Player player)) return;

        boolean inMainHand = ItemStack.matches(stack, player.getMainHandItem());
        boolean inOffHand = ItemStack.matches(stack, player.getOffhandItem());

        if (inMainHand || inOffHand) {
            int mode = tool.getPersistentData().getInt(getResource(MODE_KEY));
            InteractionHand toolHand = inMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

            if (mode == DEPLOYER) {
                handleDeployer(player, world, toolHand);
            } else if (mode == FAN && world instanceof ServerLevel serverLevel) {
                handleFan(player, serverLevel);
            }
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry, ItemStack held, Slot slot, Player player, SlotAccess access) {
        ModDataNBT data = tool.getPersistentData();
        int nextMode = (data.getInt(getResource(MODE_KEY)) + 1) % 3;
        data.putInt(getResource(MODE_KEY), nextMode);

        String modeName = switch (nextMode) {
            case DRILL -> "盾构机";
            case DEPLOYER -> "机械手";
            case FAN -> "鼓风机";
            default -> "未知";
        };

        player.displayClientMessage(Component.literal("异界之力切换: ").withStyle(ChatFormatting.GOLD).append(modeName), true);
        return true;
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (player.level().isClientSide || source != InteractionSource.RIGHT_CLICK) return InteractionResult.PASS;

        int mode = tool.getPersistentData().getInt(getResource(MODE_KEY));
        if (mode != DRILL) return InteractionResult.PASS;

        Direction dir = player.getDirection();
        BlockPos origin = player.blockPosition();
        int playerY = player.blockPosition().getY();

        AABB box;
        float xRot = player.getXRot();

        if (xRot > 60) {
            box = new AABB(origin).inflate(4, 0, 4).expandTowards(0, -8, 0);
        } else if (xRot < -60) {
            box = new AABB(origin).inflate(4, 0, 4).expandTowards(0, 8, 0);
        } else {
            box = new AABB(origin).inflate(4, 4, 0).expandTowards(dir.getStepX() * 8, 0, dir.getStepZ() * 8);
            if (box.minY < playerY) {
                box = new AABB(box.minX, playerY, box.minZ, box.maxX, box.maxY, box.maxZ);
            }
        }

        for (BlockPos pos : BlockPos.betweenClosed((int)box.minX, (int)box.minY, (int)box.minZ, (int)box.maxX, (int)box.maxY, (int)box.maxZ)) {
            BlockState state = player.level().getBlockState(pos);
            if (!state.isAir() && state.getDestroySpeed(player.level(), pos) >= 0) {
                player.level().destroyBlock(pos, true, player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void handleDeployer(Player player, Level world, InteractionHand toolHand) {
        double blockReach = player.getAttributeValue(net.minecraftforge.common.ForgeMod.BLOCK_REACH.get());
        double entityReach = player.getAttributeValue(net.minecraftforge.common.ForgeMod.ENTITY_REACH.get());

        HitResult hit = getCustomHitResult(player, world, blockReach, entityReach);

        InteractionHand actionHand = (toolHand == InteractionHand.OFF_HAND) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack actionStack = player.getItemInHand(actionHand);


        if (player.isShiftKeyDown()) {
            if (world.getGameTime() % 5 == 0) {

                if (hit.getType() == HitResult.Type.ENTITY) {
                    Entity target = ((EntityHitResult) hit).getEntity();
                    if (target instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
                        if (player.distanceTo(target) <= entityReach) {
                            player.attack(target);
                            target.invulnerableTime = 0;
                        }
                    }
                } else if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    BlockPos pos = blockHit.getBlockPos();
                    BlockState state = world.getBlockState(pos);

                    if (!state.isAir() && state.getDestroySpeed(world, pos) >= 0) {
                        float progressPerTick = state.getDestroyProgress(player, world, pos);

                        float totalProgressThisStep = progressPerTick * 5;

                        String PROGRESS_KEY = "miz_break_progress";
                        String POS_KEY = "miz_break_pos";
                        long lastPos = player.getPersistentData().getLong(POS_KEY);
                        float currentProgress = (lastPos == pos.asLong()) ? player.getPersistentData().getFloat(PROGRESS_KEY) : 0.0f;

                        currentProgress += totalProgressThisStep;

                        if (currentProgress >= 1.0f) {
                            world.destroyBlock(pos, true, player);
                            player.getPersistentData().putFloat(PROGRESS_KEY, 0.0f);
                        } else {
                            player.getPersistentData().putLong(POS_KEY, pos.asLong());
                            player.getPersistentData().putFloat(PROGRESS_KEY, currentProgress);

                            world.destroyBlockProgress(player.getId(), pos, (int)(currentProgress * 10));
                        }
                        player.swing(actionHand);
                    }
                }
            }
        } else {
            if (world.getGameTime() % 20 == 0) {
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    InteractionResult result = world.getBlockState(blockHit.getBlockPos()).use(world, player, actionHand, blockHit);
                    if (result != InteractionResult.SUCCESS && result != InteractionResult.CONSUME) {
                        if (!actionStack.isEmpty()) {
                            actionStack.useOn(new net.minecraft.world.item.context.UseOnContext(player, actionHand, blockHit));
                        }
                    }
                    player.swing(actionHand);
                } else if (hit.getType() == HitResult.Type.ENTITY) {
                    Entity target = ((EntityHitResult) hit).getEntity();
                    InteractionResult interactResult = InteractionResult.PASS;
                    if (target instanceof LivingEntity livingTarget) {
                        interactResult = actionStack.interactLivingEntity(player, livingTarget, actionHand);
                    }
                    if (interactResult != InteractionResult.SUCCESS && interactResult != InteractionResult.CONSUME) {
                        target.interact(player, actionHand);
                    }
                    player.swing(actionHand);
                }
            }
        }
    }

    private HitResult getCustomHitResult(Player player, Level world, double blockReach, double entityReach) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(viewVec.scale(blockReach));

        BlockHitResult blockHit = world.clip(new net.minecraft.world.level.ClipContext(
                eyePos, reachVec, net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE, player));

        double dist = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation().distanceTo(eyePos) : blockReach;
        double entityDist = Math.min(dist, entityReach);

        EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                player, eyePos, eyePos.add(viewVec.scale(entityDist)),
                player.getBoundingBox().expandTowards(viewVec.scale(entityDist)).inflate(1.0D),
                (e) -> !e.isSpectator() && e.isPickable(), entityDist * entityDist);

        return entityHit != null ? entityHit : blockHit;
    }

    private void handleFan(Player player, ServerLevel world) {
        Vec3 look = player.getLookAngle();
        Vec3 start = player.getEyePosition();

        AABB burnArea = player.getBoundingBox().expandTowards(look.x * 8, look.y * 8, look.z * 8).inflate(0.5);

        if (world.getGameTime() % 2 == 0) {
            for (int i = 1; i <= 8; i++) {
                world.sendParticles(ParticleTypes.FLAME,
                        start.x + look.x * i, start.y - 0.5 + look.y * i, start.z + look.z * i,
                        1, 0.1, 0.1, 0.1, 0.02);
            }
        }

        List<Entity> entities = world.getEntities(player, burnArea);
        String COOK_TICK_KEY = "miztinker_cook_ticks";

        for (Entity entity : entities) {
            if (entity == player) continue;

            entity.setDeltaMovement(entity.getDeltaMovement().add(look.scale(0.08)));
            entity.hurtMarked = true;

            if (!(entity instanceof ItemEntity)) {
                if (!entity.isInWater() && !entity.fireImmune()) {
                    entity.setSecondsOnFire(3);
                }
            }

            if (entity instanceof ItemEntity itemEntity) {
                ItemStack itemStack = itemEntity.getItem();
                Optional<SmeltingRecipe> recipe = world.getRecipeManager()
                        .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(itemStack), world);

                if (recipe.isPresent()) {
                    int cookTicks = entity.getPersistentData().getInt(COOK_TICK_KEY);
                    cookTicks++;

                    if (cookTicks % 20 == 0) {
                        world.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(), entity.getZ(), 1, 0, 0, 0, 0);
                    }

                    if (cookTicks >= 160) {
                        ItemStack result = recipe.get().getResultItem(world.registryAccess()).copy();
                        result.setCount(itemStack.getCount());
                        itemEntity.setItem(result);
                        world.sendParticles(ParticleTypes.LAVA, entity.getX(), entity.getY(), entity.getZ(), 8, 0.1, 0.1, 0.1, 0.1);
                        entity.getPersistentData().remove(COOK_TICK_KEY);
                    } else {
                        entity.getPersistentData().putInt(COOK_TICK_KEY, cookTicks);
                    }
                }
            }
        }
    }
        }