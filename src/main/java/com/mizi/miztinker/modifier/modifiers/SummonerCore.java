package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SummonerCore extends NoLevelsModifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }


    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0) return;

        Player player = context.getPlayerAttacker();
        LivingEntity target = context.getLivingTarget();

        if (player != null && target != null && !player.level().isClientSide) {
            player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32)).forEach(mob -> {
                if (isOwnedBy(mob, player)) {
                    mob.setTarget(target);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (isAnyKindPet(entity)) {
            UUID ownerUUID = getOwnerUUID(entity);
            if (ownerUUID != null) {
                Player owner = entity.level().getPlayerByUUID(ownerUUID);

                if (owner != null && !owner.level().isClientSide && attacker != owner && hasModifierInHand(owner)) {
                    float damageAmount = event.getAmount();

                    event.setAmount(0);
                    event.setCanceled(true);

                    owner.hurt(event.getSource(), damageAmount);
                    return;
                }
            }
        }
        
        if (attacker instanceof LivingEntity pet && isAnyKindPet(pet)) {
            UUID ownerUUID = getOwnerUUID(pet);
            if (ownerUUID != null) {
                Player owner = pet.level().getPlayerByUUID(ownerUUID);
                if (owner != null && hasModifierInHand(owner)) {
                    ItemStack mainHand = owner.getMainHandItem();
                    ToolStack tool = ToolStack.from(mainHand);
                    float weaponDamage = tool.getStats().get(ToolStats.ATTACK_DAMAGE);
                    event.setAmount(event.getAmount() + weaponDamage);
                }
            }
        }
    }


    private static boolean isOwnedBy(Entity entity, Player player) {
        UUID ownerId = getOwnerUUID(entity);
        return ownerId != null && ownerId.equals(player.getUUID());
    }

    private static boolean isAnyKindPet(Entity entity) {
        return entity instanceof OwnableEntity || entity instanceof TamableAnimal;
    }

    private static UUID getOwnerUUID(Entity entity) {
        if (entity instanceof OwnableEntity ownable) return ownable.getOwnerUUID();
        if (entity instanceof TamableAnimal tamable) return tamable.getOwnerUUID();
        return null;
    }

    private static boolean hasModifierInHand(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !stack.getOrCreateTag().contains("tic_stats")) return false;

        return ModifierUtil.getModifierLevel(stack, MiztinkerModifiers.SUMMONER_CORE_STATIC_MODIFIER.getId()) > 0;
    }
}