package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.UUID;

import static com.mizi.miztinker.miztinker.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class RinAwakeningEvent {

    @SubscribeEvent
    public static void onRinDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.getCustomName() != null && entity.getCustomName().getString().equalsIgnoreCase("Rin")) {

            if (entity instanceof OwnableEntity ownable) {
                UUID ownerUUID = ownable.getOwnerUUID();
                if (ownerUUID == null) return;

                Player owner = entity.level().getPlayerByUUID(ownerUUID);

                if (owner != null && owner.distanceTo(entity) < 32.0F && owner.hasLineOfSight(entity)) {


                    ItemStack helmetStack = owner.getItemBySlot(EquipmentSlot.HEAD);

                    if (!helmetStack.isEmpty() && ToolStack.isInitialized(helmetStack)) {
                        ToolStack tool = ToolStack.from(helmetStack);

                        if (tool.getModifiers().getLevel(MiztinkerModifiers.KAMUI_EYE_STATIC_MODIFIER.get().getId()) <= 0) {

                            tool.addModifier(MiztinkerModifiers.KAMUI_EYE_STATIC_MODIFIER.get().getId(), 1);
                            tool.rebuildStats();

                            if (owner instanceof ServerPlayer serverPlayer) {
                                serverPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
                                serverPlayer.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, false));

                                Component titleText = Component.translatable("chat.miztinker.kamui_awakening")
                                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

                                serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
                                serverPlayer.connection.send(new ClientboundSetTitleTextPacket(titleText));

                            }
                        }
                    }
                }
            }
        }
    }
}