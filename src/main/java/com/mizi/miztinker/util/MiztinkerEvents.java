package com.mizi.miztinker.util;

import com.mizi.miztinker.item.BottledCryingObsidianItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker")
public class MiztinkerEvents {

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();

        if (stack.getItem() instanceof BottledCryingObsidianItem &&
                target instanceof Villager villager &&
                villager.getVillagerData().getProfession() == VillagerProfession.FARMER) {

            BottledCryingObsidianItem.performRitual(stack, player, villager);

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));
        }
    }
}