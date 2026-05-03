package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.sounds.MiztinkerSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.Objects;
import java.util.UUID;

public class JusticeJudgment extends NoLevelsModifier implements MeleeHitModifierHook, InventoryTickModifierHook {


    private static final ResourceLocation COMBO_COUNT = ResourceLocation.fromNamespaceAndPath("miztinker", "combo_count");
    private static final ResourceLocation LAST_DAMAGE = ResourceLocation.fromNamespaceAndPath("miztinker", "last_damage");
    private static final ResourceLocation TARGET_UUID = ResourceLocation.fromNamespaceAndPath("miztinker", "target_uuid");
    private static final ResourceLocation LAST_HIT_TIME = ResourceLocation.fromNamespaceAndPath("miztinker", "last_hit_time");
    public static final ResourceLocation COMBO_START_TIME = ResourceLocation.fromNamespaceAndPath("miztinker", "combo_start_time");

    private static final String ENTITY_START_TIME = "miz_combo_start";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        Level level = attacker.level();

        if (target == null || level.isClientSide) return;

        ModDataNBT tdata = tool.getPersistentData();
        long currentTime = level.getGameTime();

        UUID lastTarget = tdata.get(TARGET_UUID, (tag, key) -> tag.contains(key) ? tag.getUUID(key) : null);
        long lastHit = tdata.get(LAST_HIT_TIME, (tag, key) -> tag.contains(key) ? tag.getLong(key) : 0L);

        boolean isNewCombo = (lastTarget == null) || !lastTarget.equals(target.getUUID()) || (currentTime - lastHit > 20);

        if (isNewCombo) {
            tdata.putInt(COMBO_COUNT, 1);
            target.getPersistentData().putLong(ENTITY_START_TIME, currentTime);
            tdata.put(COMBO_START_TIME, LongTag.valueOf(currentTime));
            attacker.getPersistentData().putLong(ENTITY_START_TIME, currentTime);
        } else {
            int currentCombo = tdata.get(COMBO_COUNT, (tag, key) -> tag.contains(key) ? tag.getInt(key) : 0);
            tdata.putInt(COMBO_COUNT, currentCombo + 1);
        }

        tdata.putFloat(LAST_DAMAGE, damageDealt);

        CompoundTag uuidTag = new CompoundTag();
        uuidTag.putUUID("v", target.getUUID());
        tdata.put(TARGET_UUID, Objects.requireNonNull(uuidTag.get("v")));

        tdata.put(LAST_HIT_TIME, LongTag.valueOf(currentTime));
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide || !isSelected || !(holder instanceof ServerPlayer player)) return;

        ModDataNBT tdata = tool.getPersistentData();
        if (!tdata.contains(TARGET_UUID)) return;

        long currentTime = level.getGameTime();
        UUID targetId = tdata.get(TARGET_UUID, (tag, key) -> tag.contains(key) ? tag.getUUID(key) : null);
        long lastHit = tdata.get(LAST_HIT_TIME, (tag, key) -> tag.contains(key) ? tag.getLong(key) : 0L);

        LivingEntity target = getLivingByUUID(level, targetId);

        if (target != null) {
            long startTime = target.getPersistentData().getLong(ENTITY_START_TIME);
            if (startTime > 0 && currentTime - startTime > 1200) {
                target.hurt(level.damageSources().generic(), Float.MAX_VALUE);
                player.hurt(level.damageSources().generic(), Float.MAX_VALUE);
                clearCombo(tdata, player);
                return;
            }
        }

        if (currentTime - lastHit > 20) {
            int count = tdata.get(COMBO_COUNT, (tag, key) -> tag.contains(key) ? tag.getInt(key) : 0);
            float lastDmg = tdata.get(LAST_DAMAGE, (tag, key) -> tag.contains(key) ? tag.getFloat(key) : 0f);

            if (count > 1 && target != null && target.isAlive()) {
                target.hurt(level.damageSources().mobAttack(player), lastDmg * count);

                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§d§lALL JUSTICE")));

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        MiztinkerSounds.ALL_JUSTICE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            clearCombo(tdata, player);
        }
    }

    private void clearCombo(ModDataNBT tdata, LivingEntity player) {
        tdata.remove(COMBO_COUNT);
        tdata.remove(LAST_DAMAGE);
        tdata.remove(TARGET_UUID);
        tdata.remove(LAST_HIT_TIME);
        tdata.remove(COMBO_START_TIME);
        player.getPersistentData().remove(ENTITY_START_TIME);
    }

    private LivingEntity getLivingByUUID(Level level, UUID uuid) {
        if (level instanceof ServerLevel serverLevel) {
            net.minecraft.world.entity.Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof LivingEntity living) return living;
        }
        return null;
    }
}