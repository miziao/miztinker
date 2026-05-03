package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class Ravenous extends NoLevelsModifier {
    private static final ResourceLocation REVIVE_COUNT = ResourceLocation.fromNamespaceAndPath("miztinker", "ravenous");

    public Ravenous() {
        // 注册事件监听
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onPlayerDeath);
    }

    private void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        // 遍历所有装备槽位（主手、副手、护甲等）
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.is(TinkerTags.Items.MODIFIABLE)) {
                IToolStackView tool = ToolStack.from(stack);
                if (tool == null || tool.getModifierLevel(this) <= 0) continue;

                ModDataNBT persistent = tool.getPersistentData();
                int reviveCount = persistent.getInt(REVIVE_COUNT);

                // 达到 6 次就摧毁工具
                if (reviveCount >= 6) {
                    stack.shrink(1);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("你的进化已达到顶点。"), true);
                    return;
                }

                // 取消死亡事件（复活）
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth() * 0.5F);
                player.removeAllEffects();

                // 给予增益效果
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2));

                // 每复活一次 +20% 最大生命值
                double newHealth = player.getMaxHealth() * 1.2;
                player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                        .setBaseValue(newHealth);

                // 计数 +1
                persistent.putInt(REVIVE_COUNT, reviveCount + 1);

                // 播放视觉/音效
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            40, 0.5, 0.5, 0.5, 0.1);
                    serverLevel.playSound(null, player.blockPosition(),
                            SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
                }

                return; // 只处理一次复活
            }
        }
    }
}
