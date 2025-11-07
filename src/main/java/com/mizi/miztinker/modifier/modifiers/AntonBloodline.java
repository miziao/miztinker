package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

@Mod.EventBusSubscriber
public class AntonBloodline extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    /** 穿在盔甲栏上的持续效果 **/
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        if (!(holder instanceof Player player)) return;
        if (!isCorrectSlot) return;
        if (world.isClientSide) return;

        ServerLevel level = (ServerLevel) world;

        // 查找附近 5 格内的点燃 TNT
        List<PrimedTnt> tntList = level.getEntitiesOfClass(PrimedTnt.class,
                player.getBoundingBox().inflate(5.0D));

        for (PrimedTnt tnt : tntList) {
            Vec3 pos = tnt.position();

            // 删除 TNT 实体
            tnt.discard();

            // 生成红色粒子效果
            DustParticleOptions redParticle =
                    new DustParticleOptions(new Vector3f(1.0f, 0.1f, 0.1f), 1.0f);

            level.sendParticles(redParticle,
                    pos.x, pos.y + 0.5, pos.z,
                    10, 0.3, 0.3, 0.3, 0.02);
        }
    }

    /** 限制弹射物伤害 **/
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getSource().getDirectEntity() instanceof Projectile) {
            // 限制最大伤害为 5
            float newDamage = Math.min(event.getAmount(), 5.0F);
            event.setAmount(newDamage);
        }
    }
}