package com.mizi.miztinker.entity;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.definition.module.weapon.MeleeHitToolHook;
import slimeknights.tconstruct.library.tools.helper.ModifierLootingHandler;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleSupplier;

public class AttackUtil {

    public static void forceHurt(LivingEntity living, DamageSource p_21016_, float p_21017_) {
        if (living.isSleeping() && !living.level().isClientSide) {
            living.stopSleeping();
        }
        living.noActionTime = 0;
        float f = p_21017_;
        boolean flag = false;
        float f1 = 0.0F;
        Entity entity1;
        LivingEntity livingentity1;
        living.walkAnimation.setSpeed(0.0F);
        living.lastHurt = p_21017_;
        living.invulnerableTime = 0;
        actuallyHurt(living, p_21016_, p_21017_);
        living.hurtDuration = 10;
        living.hurtTime = living.hurtDuration;
        entity1 = p_21016_.getEntity();
        if (entity1 != null) {
            if (entity1 instanceof LivingEntity) {
                livingentity1 = (LivingEntity) entity1;
                if (!p_21016_.is(DamageTypeTags.NO_ANGER)) {
                    living.setLastHurtByMob(livingentity1);
                }
            }
            if (entity1 instanceof Player) {
                Player player1 = (Player) entity1;
                living.lastHurtByPlayerTime = 100;
                living.lastHurtByPlayer = player1;
            } else if (entity1 instanceof TamableAnimal) {
                TamableAnimal tamableEntity = (TamableAnimal) entity1;
                if (tamableEntity.isTame()) {
                    living.lastHurtByPlayerTime = 100;
                    LivingEntity livingentity2 = tamableEntity.getOwner();
                    if (livingentity2 instanceof Player) {
                        Player player = (Player) livingentity2;
                        living.lastHurtByPlayer = player;
                    } else {
                        living.lastHurtByPlayer = null;
                    }
                }
            }
        }
        if (flag) {
            living.level().broadcastEntityEvent(living, (byte) 29);
        } else {
            living.level().broadcastDamageEvent(living, p_21016_);
        }
        living.hurtMarked = true;
        boolean flag2 = !flag || p_21017_ > 0.0F;
        if (flag2) {
            living.lastDamageSource = p_21016_;
            living.lastDamageStamp = living.level().getGameTime();
        }
        if (living instanceof ServerPlayer) {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer) living, p_21016_, f, p_21017_, flag);
            if (f1 > 0.0F && f1 < 3.4028235E37F) {
                ((ServerPlayer) living).awardStat(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD), Math.round(f1 * 10.0F));
            }
        }
        if (entity1 instanceof ServerPlayer) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer) entity1, living, p_21016_, f, p_21017_, flag);
        }
    }

    public static void actuallyHurt(LivingEntity living, DamageSource p_21240_, float p_21241_) {
        float f1 = Math.max(p_21241_ - living.getAbsorptionAmount(), 0.0F);
        living.setAbsorptionAmount(living.getAbsorptionAmount() - (p_21241_ - f1));
        float f = p_21241_ - f1;
        if (f > 0.0F && f < 3.4028235E37F) {
            Entity entity = p_21240_.getEntity();
            if (entity instanceof ServerPlayer) {
                ServerPlayer serverplayer = (ServerPlayer) entity;
                serverplayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f * 10.0F));
            }
        }
        living.getCombatTracker().recordDamage(p_21240_, f1);
        living.setHealth(living.getHealth() - f1);
        living.setAbsorptionAmount(living.getAbsorptionAmount() - f1);
        living.gameEvent(GameEvent.ENTITY_DAMAGE);
    }

    private static final float DEGREE_TO_RADIANS = ((float)Math.PI / 180F);
    private static final AttributeModifier ANTI_KNOCKBACK_MODIFIER;
    static {
        ANTI_KNOCKBACK_MODIFIER = new AttributeModifier("tconstruct.anti_knockback", (double)1.0F, AttributeModifier.Operation.ADDITION);
    }
    private static void disableKnockback(AttributeInstance instance) {
        instance.addTransientModifier(ANTI_KNOCKBACK_MODIFIER);
    }

    private static void enableKnockback(AttributeInstance instance) {
        instance.removeModifier(ANTI_KNOCKBACK_MODIFIER);
    }

    private static Optional<AttributeInstance> getKnockbackAttribute(@Nullable LivingEntity living) {
        return Optional.ofNullable(living).map((e) -> e.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).filter((attribute) -> !attribute.hasModifier(ANTI_KNOCKBACK_MODIFIER));
    }

    public static boolean attackEntity(IToolStackView tool, LivingEntity attackerLiving, InteractionHand hand, Entity targetEntity, DoubleSupplier cooldownFunction, boolean isExtraAttack, EquipmentSlot sourceSlot, float SetDamage, float DamageMultiplier, boolean SetCritical, boolean notDamageTool, boolean removeInvTime, boolean removeknockback) {
        if (!tool.isBroken() && tool.hasTag(TinkerTags.Items.MELEE)) {
            if (!attackerLiving.level().isClientSide && targetEntity.isAttackable() && !targetEntity.skipAttackInteraction(attackerLiving)) {
                LivingEntity targetLiving = ToolAttackUtil.getLivingEntity(targetEntity);
                Player attackerPlayer = null;
                if (attackerLiving instanceof Player) {
                    Player player = (Player)attackerLiving;
                    attackerPlayer = player;
                }

                float damage = SetDamage < 0.0F ? ToolAttackUtil.getAttributeAttackDamage(tool, attackerLiving, sourceSlot) : SetDamage;
                float cooldown = (float)cooldownFunction.getAsDouble();
                boolean fullyCharged = cooldown > 0.9F;
                boolean isCritical = !isExtraAttack && fullyCharged && attackerLiving.fallDistance > 0.0F && !attackerLiving.onGround() && !attackerLiving.onClimbable() && !attackerLiving.isInWater() && !attackerLiving.hasEffect(MobEffects.BLINDNESS) && !attackerLiving.isPassenger() && targetLiving != null && !attackerLiving.isSprinting() || SetCritical;
                ToolAttackContext context = new ToolAttackContext(attackerLiving, attackerPlayer, hand, sourceSlot, targetEntity, targetLiving, isCritical, cooldown, isExtraAttack);
                float baseDamage = damage;
                List<ModifierEntry> modifiers = tool.getModifierList();

                for(ModifierEntry entry : modifiers) {
                    damage = ((MeleeDamageModifierHook)entry.getHook(ModifierHooks.MELEE_DAMAGE)).getMeleeDamage(tool, entry, context, baseDamage, damage);
                }

                if (damage <= 0.0F) {
                    return !isExtraAttack;
                } else {
                    float knockback = (float)attackerLiving.getAttributeValue(Attributes.ATTACK_KNOCKBACK) / 2.0F;
                    if (targetLiving != null) {
                        knockback += 0.4F;
                    }

                    SoundEvent sound;
                    if (attackerLiving.isSprinting() && fullyCharged) {
                        sound = SoundEvents.PLAYER_ATTACK_KNOCKBACK;
                        knockback += 0.5F;
                    } else if (fullyCharged) {
                        sound = SoundEvents.PLAYER_ATTACK_STRONG;
                    } else {
                        sound = SoundEvents.PLAYER_ATTACK_WEAK;
                    }

                    float criticalModifier = isCritical ? 1.5F : 1.0F;
                    if (attackerPlayer != null) {
                        CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(attackerPlayer, targetEntity, isCritical, isCritical ? 1.5F : 1.0F);
                        isCritical = hitResult != null;
                        if (isCritical) {
                            criticalModifier = hitResult.getDamageModifier();
                        }
                    }

                    if (isCritical) {
                        damage *= criticalModifier;
                    }

                    if (DamageMultiplier >= 0.0F) {
                        damage *= DamageMultiplier;
                    }

                    boolean isMagic = damage > baseDamage;
                    if (cooldown < 1.0F) {
                        damage *= 0.2F + cooldown * cooldown * 0.8F;
                    }

                    float oldHealth = 0.0F;
                    if (targetLiving != null) {
                        oldHealth = targetLiving.getHealth();
                    }

                    float baseKnockback = knockback;

                    for(ModifierEntry entry : modifiers) {
                        knockback = ((MeleeHitModifierHook)entry.getHook(ModifierHooks.MELEE_HIT)).beforeMeleeHit(tool, entry, context, damage, baseKnockback, knockback);
                    }

                    ModifierLootingHandler.setLootingSlot(attackerLiving, sourceSlot);
                    Optional<AttributeInstance> knockbackModifier = getKnockbackAttribute(targetLiving);
                    boolean canceledKnockback = false;
                    if (knockback < 0.4F) {
                        canceledKnockback = true;
                        knockbackModifier.ifPresent(AttackUtil::disableKnockback);
                    } else if (targetLiving != null) {
                        knockback -= 0.4F;
                    }

                    boolean didHit;
                    if (isExtraAttack) {
                        didHit = ToolAttackUtil.dealDefaultDamage(attackerLiving, targetEntity, damage);
                    } else {
                        didHit = MeleeHitToolHook.dealDamage(tool, context, damage);
                    }

                    ModifierLootingHandler.setLootingSlot(attackerLiving, EquipmentSlot.MAINHAND);
                    if (canceledKnockback) {
                        knockbackModifier.ifPresent(AttackUtil::enableKnockback);
                    }

                    if (!didHit) {
                        if (!isExtraAttack) {
                            attackerLiving.level().playSound((Player)null, attackerLiving.getX(), attackerLiving.getY(), attackerLiving.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, attackerLiving.getSoundSource(), 1.0F, 1.0F);
                        }

                        for(ModifierEntry entry : modifiers) {
                            ((MeleeHitModifierHook)entry.getHook(ModifierHooks.MELEE_HIT)).failedMeleeHit(tool, entry, context, damage);
                        }

                        return !isExtraAttack;
                    } else {
                        float damageDealt = damage;
                        if (targetLiving != null) {
                            damageDealt = oldHealth - targetLiving.getHealth();
                        }

                        if (knockback > 0.0F) {
                            if (targetLiving != null) {
                                targetLiving.knockback((double)knockback, (double) Mth.sin(attackerLiving.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(attackerLiving.getYRot() * ((float)Math.PI / 180F))));
                            } else {
                                targetEntity.push((double)(-Mth.sin(attackerLiving.getYRot() * ((float)Math.PI / 180F)) * knockback), 0.1, (double)(Mth.cos(attackerLiving.getYRot() * ((float)Math.PI / 180F)) * knockback));
                            }

                            attackerLiving.setDeltaMovement(attackerLiving.getDeltaMovement().multiply(0.6, (double)1.0F, 0.6));
                            attackerLiving.setSprinting(false);
                        }

                        if (targetEntity.hurtMarked && targetEntity instanceof ServerPlayer) {
                            ServerPlayer serverPlayer = (ServerPlayer)targetEntity;
                            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(targetEntity));
                            targetEntity.hurtMarked = false;
                        }

                        if (attackerPlayer != null) {
                            if (isCritical) {
                                sound = SoundEvents.PLAYER_ATTACK_CRIT;
                                attackerPlayer.crit(targetEntity);
                            }

                            if (isMagic) {
                                attackerPlayer.magicCrit(targetEntity);
                            }

                            attackerLiving.level().playSound((Player)null, attackerLiving.getX(), attackerLiving.getY(), attackerLiving.getZ(), sound, attackerLiving.getSoundSource(), 1.0F, 1.0F);
                        }

                        if (damageDealt > 2.0F) {
                            Level var33 = attackerLiving.level();
                            if (var33 instanceof ServerLevel) {
                                ServerLevel server = (ServerLevel)var33;
                                int particleCount = (int)(damageDealt * 0.5F);
                                server.sendParticles(ParticleTypes.DAMAGE_INDICATOR, targetEntity.getX(), targetEntity.getY((double)0.5F), targetEntity.getZ(), particleCount, 0.1, (double)0.0F, 0.1, 0.2);
                            }
                        }

                        attackerLiving.setLastHurtMob(targetEntity);
                        if (targetLiving != null) {
                            EnchantmentHelper.doPostHurtEffects(targetLiving, attackerLiving);
                        }

                        for(ModifierEntry entry : modifiers) {
                            ((MeleeHitModifierHook)entry.getHook(ModifierHooks.MELEE_HIT)).afterMeleeHit(tool, entry, context, damageDealt);
                        }

                        if (removeInvTime) {
                            targetEntity.invulnerableTime = 0;
                        } else {
                            float speed = (Float)tool.getStats().get(ToolStats.ATTACK_SPEED);
                            int time = Math.round(20.0F / speed);
                            if (time < targetEntity.invulnerableTime) {
                                targetEntity.invulnerableTime = (targetEntity.invulnerableTime + time) / 2;
                            }
                        }

                        if (attackerPlayer != null) {
                            if (targetLiving != null) {
                                if (!attackerLiving.level().isClientSide && !isExtraAttack) {
                                    ItemStack held = attackerLiving.getItemBySlot(sourceSlot);
                                    if (!held.isEmpty()) {
                                        held.hurtEnemy(targetLiving, attackerPlayer);
                                    }
                                }

                                attackerPlayer.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
                            }

                            attackerPlayer.causeFoodExhaustion(0.1F);
                            if (!isExtraAttack) {
                                attackerPlayer.awardStat(Stats.ITEM_USED.get(tool.getItem()));
                            }
                        }

                        if (!tool.hasTag(TinkerTags.Items.UNARMED) && !notDamageTool) {
                            int durabilityLost = targetLiving != null ? 1 : 0;
                            if (!tool.hasTag(TinkerTags.Items.MELEE_PRIMARY)) {
                                durabilityLost *= 2;
                            }

                            ToolDamageUtil.damageAnimated(tool, durabilityLost, attackerLiving);
                        }

                        return true;
                    }
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
