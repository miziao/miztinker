package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.RequirementsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.List;
import java.util.UUID;

public class GreyMatter extends NoLevelsModifier
        implements SlotStackModifierHook,
        InventoryTickModifierHook,
        RequirementsModifierHook,
        ValidateModifierHook,
        EquipmentChangeModifierHook {

    private static final ResourceLocation SHRUNK_KEY = new ResourceLocation("miztinker", "grey_matter_shrunk");
    private static final UUID HEALTH_UUID = UUID.fromString("5f5e555d-aaaa-4f8e-b5aa-31c1e4a6a9cc");
    private static final float SHRINK_SCALE = 0.1f; // 0.1格
    private static final double SHRUNK_MAX_HEALTH = 5.0; // 最大生命值变为5点
    private static final long DUPLICATE_INTERVAL_TICKS = 5 * 60 * 20L; // 5分钟 = 5*60*20 ticks
    private static final double DUPLICATE_RANGE = 1.5; // 3x3 区域 -> 半径1.5

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public @Nullable Component validate(IToolStackView tool, ModifierEntry entry) {
        // 按需校验，示例允许为 null（通过）
        return null;
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            IToolStackView tool,
            ModifierEntry entry,
            ItemStack held,
            Slot slot,
            Player player,
            SlotAccess access
    ) {
        // 必须空手
        if (!held.isEmpty()) return false;

        // 只在服务端处理
        if (player.level().isClientSide) return false;

        CompoundTag data = player.getPersistentData();
        boolean shrunk = data.getBoolean(SHRUNK_KEY.toString());

        if (!shrunk) {
            enterShrunk(player); // 切换状态
        } else {
            exitShrunk(player);
        }

        // 吞掉右键，避免原版逻辑
        return true;
    }

    /** InventoryTick 的完整签名（实现接口要求） */
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier,
                                Level level, LivingEntity living,
                                int slot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!(living instanceof Player player)) return;
        if (level.isClientSide) return;
        if (slot != EquipmentSlot.HEAD.getIndex()) return;
        if (!isCorrectSlot) return;

        CompoundTag data = player.getPersistentData();
        boolean shrunk = data.getBoolean(SHRUNK_KEY.toString());
        if (!shrunk) return; //

        // ✅ 仅当已经处于缩小状态时，每隔一段时间刷新 buff
        long time = level.getGameTime();
        if (time % 20L == 0L) { // 每秒刷新一次（避免 buff 消失）
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 25, 9, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, 4, true, false, false));
        }

        // ✅ 只有在缩小状态时才处理掉落物翻倍
        if (time % DUPLICATE_INTERVAL_TICKS == 0L) {
            if (level instanceof ServerLevel serverLevel) {
                duplicateNearbyDrops(serverLevel, player);
            }
        }
    }

    /** 进入缩小 */
    private void enterShrunk(Player player) {
        CompoundTag tag = player.getPersistentData();
        tag.putBoolean(SHRUNK_KEY.toString(), true);

        ScaleData scale = ScaleTypes.BASE.getScaleData(player);
        scale.setTargetScale(SHRINK_SCALE);

        // 给固定的最大生命上限（使用 addTransientModifier 并计算差值）
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getModifier(HEALTH_UUID) == null) {
            // Minecraft 默认生命 20.0，直接把差值作为 modifier
            double delta = SHRUNK_MAX_HEALTH - 20.0;
            health.addTransientModifier(new AttributeModifier(HEALTH_UUID, "Grey Matter Shrunk Health", delta, AttributeModifier.Operation.ADDITION));
            // 同步并修正当前血量
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth((float) player.getMaxHealth());
            }
        }

        player.displayClientMessage(Component.literal("§3英雄变身 小奇兵!"), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 退出缩小 */
    private void exitShrunk(Player player) {
        CompoundTag tag = player.getPersistentData();
        tag.remove(SHRUNK_KEY.toString());

        ScaleData scale = ScaleTypes.BASE.getScaleData(player);
        scale.setTargetScale(1.0f);

        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getModifier(HEALTH_UUID) != null) {
            health.removeModifier(HEALTH_UUID);
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }

        player.displayClientMessage(Component.literal("§3解除变身"), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 0.9F);
    }

    /** 掉落物翻倍：在玩家周围复制 ItemEntity（生成新的实体） */
    private void duplicateNearbyDrops(ServerLevel level, Player player) {
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(DUPLICATE_RANGE),
                item -> !item.getItem().isEmpty());

        for (ItemEntity item : items) {
            ItemStack stack = item.getItem();
            if (stack.isEmpty()) continue;
            ItemStack dup = stack.copy();
            ItemEntity newItem = new ItemEntity(level, item.getX(), item.getY(), item.getZ(), dup);
            newItem.setDeltaMovement(item.getDeltaMovement());
            level.addFreshEntity(newItem);
        }

        player.displayClientMessage(Component.literal("§a你使用了小奇兵的智慧使得周围的掉落物翻倍了!"), true);
        level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.2f);
    }

    /** 当卸下头盔时清理状态，避免残留缩小/属性 */
    @Override
    public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
        if (context.getChangedSlot() == EquipmentSlot.HEAD && context.getEntity() instanceof Player) {
            Player player = (Player) context.getEntity();
            CompoundTag tag = player.getPersistentData();
            if (tag.getBoolean(SHRUNK_KEY.toString())) {
                exitShrunk(player);
            }
        }
    }
}