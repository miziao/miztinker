package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.lang.reflect.Method;

import static com.mizi.miztinker.miztinker.getResource;

public class VexSummoner extends NoLevelsModifier implements MeleeHitModifierHook {

    private static final String COUNTER_KEY = "vex_summon_count";


    private static final ResourceLocation ALLY_VEX_ID =
            ResourceLocation.fromNamespaceAndPath("goety", "ally_vex");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(
            IToolStackView tool,
            ModifierEntry modifier,
            ToolAttackContext context,
            float damageDealt
    ) {
        if (context.getAttacker().level().isClientSide()) {
            return;
        }

        if (!(context.getAttacker() instanceof Player player)) {
            return;
        }

        int count = tool.getPersistentData().getInt(getResource(COUNTER_KEY));
        count++;

        if (count >= 5) {
            count = 0;

            if (player.level() instanceof ServerLevel serverLevel) {
                summonAllyVex(serverLevel, player);
            }
        }

        tool.getPersistentData().putInt(getResource(COUNTER_KEY), count);
    }

    private void summonAllyVex(ServerLevel level, Player player) {

        if (!ModList.get().isLoaded("goety")) {
            return;
        }

        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(ALLY_VEX_ID);

        if (entityType == null) {
            return;
        }

        BlockPos spawnPos = player.blockPosition().above();

        Entity entity = entityType.spawn(
                level,
                null,
                player,
                spawnPos,
                MobSpawnType.MOB_SUMMONED,
                true,
                false
        );

        if (entity == null) {
            return;
        }

        entity.moveTo(
                player.getX(),
                player.getY() + 1.5D,
                player.getZ(),
                player.getYRot(),
                player.getXRot()
        );

        invokeMethod(
                entity,
                "setTrueOwner",
                new Class<?>[]{Player.class},
                new Object[]{player}
        );

        invokeMethod(
                entity,
                "setHasLifespan",
                new Class<?>[]{boolean.class},
                new Object[]{false}
        );
    }

    private static void invokeMethod(
            Entity entity,
            String methodName,
            Class<?>[] parameterTypes,
            Object[] args
    ) {
        try {
            Method method = entity.getClass().getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            method.invoke(entity, args);
        } catch (Exception ignored) {
        }
    }
}