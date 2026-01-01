package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Random;

public class Happy_NewYear extends NoLevelsModifier implements GeneralInteractionModifierHook {

    private static final Random RANDOM = new Random();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        Level level = player.level();
        if (!level.isClientSide && source == InteractionSource.RIGHT_CLICK) {

            ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
            CompoundTag fireworkTag = fireworkStack.getOrCreateTagElement("Fireworks");

            byte flightLevel = (byte) (RANDOM.nextInt(3) + 1);
            fireworkTag.putByte("Flight", flightLevel);

            ListTag explosionsList = new ListTag();
            int explosionCount = RANDOM.nextInt(4) + 1;
            for (int i = 0; i < explosionCount; i++) {
                explosionsList.add(generateRandomExplosion());
            }
            fireworkTag.put("Explosions", explosionsList);

            FireworkRocketEntity firework = new FireworkRocketEntity(level, player, player.getX(), player.getEyeY(), player.getZ(), fireworkStack);

            int flightTicks = (flightLevel * 5) + RANDOM.nextInt(6) + RANDOM.nextInt(7);
            CompoundTag entityData = new CompoundTag();
            firework.saveWithoutId(entityData);
            entityData.putInt("LifeTime", flightTicks);
            firework.load(entityData);

            firework.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.6F, 1.0F);
            level.addFreshEntity(firework);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private CompoundTag generateRandomExplosion() {
        CompoundTag tag = new CompoundTag();

        tag.putByte("Type", (byte) RANDOM.nextInt(5));

        tag.putBoolean("Flicker", RANDOM.nextBoolean());
        tag.putBoolean("Trail", RANDOM.nextBoolean());

        tag.putIntArray("Colors", generateRandomColors(1 + RANDOM.nextInt(3)));

        if (RANDOM.nextBoolean()) {
            tag.putIntArray("FadeColors", generateRandomColors(1 + RANDOM.nextInt(2)));
        }

        return tag;
    }

    private int[] generateRandomColors(int count) {
        int[] colors = new int[count];
        for (int i = 0; i < count; i++) {
            // 将随机范围限制在 120-255，保证色彩饱和度
            int r = 120 + RANDOM.nextInt(136);
            int g = 120 + RANDOM.nextInt(136);
            int b = 120 + RANDOM.nextInt(136);
            colors[i] = (r << 16) | (g << 8) | b;
        }
        return colors;
    }
}