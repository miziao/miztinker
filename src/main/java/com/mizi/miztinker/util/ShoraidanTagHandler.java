package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.modifiers.Shoraidan;
import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mod.EventBusSubscriber
public class ShoraidanTagHandler {
    private static final String MARK_TAG = "miztinker_shoraidan_marked";

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof LivingEntity target) || target instanceof Player) {
            return;
        }

        Player player = event.getLevel().getNearestPlayer(target, 128);
        if (player != null && hasShoraidan(player)) {
            target.addTag(MARK_TAG);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.level().isClientSide && target.getTags().contains(MARK_TAG)) {
            target.removeTag(MARK_TAG);

            Player player = target.level().getNearestPlayer(target, 128);
            if (player != null && hasShoraidan(player)) {
                Shoraidan.execute(target, player);
            }
        }
    }

    private static boolean hasShoraidan(Player player) {
        return checkTool(player.getMainHandItem()) || checkTool(player.getOffhandItem());
    }

    private static boolean checkTool(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty() || !stack.is(slimeknights.tconstruct.common.TinkerTags.Items.MODIFIABLE)) return false;
        try {
            return ToolStack.from(stack).getModifierLevel(MiztinkerModifiers.SHORAIDAN_STATIC_MODIFIER.get()) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}