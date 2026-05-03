package com.mizi.miztinker.recipes;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = "miztinker")
public class VillagerUpgradeHandler {

    private static final ResourceLocation TARGET_ITEM = ResourceLocation.parse("miztinker:villager_business_card");

    @SubscribeEvent
    public static void onRightClickVillager(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getTarget() instanceof Villager oldVillager)) return;

        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();

        // 检查玩家手持物品
        ResourceLocation heldId = held.isEmpty() ? null :
                player.level().registryAccess().registryOrThrow(Registries.ITEM)
                        .getKey(held.getItem());
        if (!TARGET_ITEM.equals(heldId)) return;

        // 阻止原交互
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        VillagerData oldData = oldVillager.getVillagerData();
        VillagerProfession profession = oldData.getProfession();
        VillagerType type = oldData.getType();

        // 无职业 → 提示不可升职
        if (profession == VillagerProfession.NONE) {
            player.displayClientMessage(Component.literal("§c当前村民无职业，不可升职！"), true);
            return; // 不执行后续
        }

        // 删除旧村民
        double x = oldVillager.getX();
        double y = oldVillager.getY();
        double z = oldVillager.getZ();
        oldVillager.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);

        // 创建新村民
        Villager newVillager = EntityType.VILLAGER.create(level);
        if (newVillager == null) return;

        newVillager.setPos(x, y, z);
        newVillager.setVillagerData(new VillagerData(type, profession, 5)); // 升为大师级
        level.addFreshEntity(newVillager);

        // 消耗道具
        held.shrink(1);

        // 粒子效果
        for (int i = 0; i < 20; i++) {
            double px = x + (level.random.nextDouble() - 0.5) * 1.5;
            double py = y + level.random.nextDouble() * 1.5;
            double pz = z + (level.random.nextDouble() - 0.5) * 1.5;
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0, 0, 0, 0);
        }

        // 播放声音
        level.playSound(null, x, y, z,
                net.minecraft.sounds.SoundEvents.VILLAGER_YES,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f, 1.0f);

        // 安全显示职业名
        ResourceLocation profId = player.level().registryAccess()
                .registryOrThrow(Registries.VILLAGER_PROFESSION)
                .getKey(profession);

        String profName = (profId != null) ? profId.getPath() : "未知职业";
        player.displayClientMessage(Component.literal(
                "§a村民已升职为大师级 " + profName + "！"
        ), true);
    }
}