package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.modifiers.base.DelayedTaskHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

public class EchoingStrikesStart extends NoLevelsModifier implements MeleeHitModifierHook {

    private static final Logger LOGGER = LogManager.getLogger("miztinker");
    private static final ThreadLocal<Boolean> mizi$isEchoing = ThreadLocal.withInitial(() -> false);

    private final int delayIndex;

    public EchoingStrikesStart(int delayIndex) {
        this.delayIndex = delayIndex;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (mizi$isEchoing.get()) return;

        LivingEntity attacker = context.getAttacker();
        LivingEntity target = context.getLivingTarget();

        if (attacker.level().isClientSide || target == null || !target.isAlive()) return;

        ServerLevel serverLevel = (ServerLevel) attacker.level();
        InteractionHand hand = context.getHand();

        int delay = (this.delayIndex + 1) * 5;

        DelayedTaskHandler.add(serverLevel, delay, () -> {
            if (target.isAlive() && !tool.isBroken()) {
                executeEchoHit(tool, attacker, target, hand);
            }
        });
    }

    private void executeEchoHit(IToolStackView tool, LivingEntity attacker, LivingEntity target, InteractionHand hand) {
        mizi$isEchoing.set(true);
        try {
            target.invulnerableTime = 0;

            Player player = ModifierUtil.asPlayer(attacker);
            if (player == null) return;

            player.attack(target);

            if (attacker.level() instanceof ServerLevel world) {
                float pitch = 0.9F + world.random.nextFloat() * 0.2F;
                world.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, pitch);

                world.sendParticles(ParticleTypes.GLOW,
                        target.getX(), target.getY(0.5), target.getZ(),
                        12, 0.2, 0.2, 0.2, 0.05);
            }
        } catch (Exception e) {
            LOGGER.error("EchoingStrikes 回响执行失败", e);
        } finally {
            mizi$isEchoing.set(false);
        }
    }
}