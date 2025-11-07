package com.mizi.miztinker.modifier.modifiers;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.illager.AllyVex;
import com.Polarice3.Goety.common.entities.neutral.Minion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class VexSummoner extends NoLevelsModifier implements MeleeHitModifierHook {

    private static final String COUNTER_KEY = "vex_summon_count";

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (context.getAttacker() == null || context.getAttacker().level().isClientSide()) return;
        if (!(context.getAttacker() instanceof Player player)) return;

        // 从工具NBT取计数
        int count = tool.getPersistentData().getInt(ResourceLocation.parse(COUNTER_KEY));
        count++;
        if (count >= 5) {
            count = 0;
            summonAllyVex((ServerLevel) player.level(), player);
        }
        tool.getPersistentData().putInt(ResourceLocation.parse(COUNTER_KEY), count);
    }

    @SuppressWarnings("unchecked")
    private void summonAllyVex(ServerLevel level, Player player) {
        // 1️⃣ 强制转换类型（安全的，因为 AllyVex extends Minion）
        EntityType<? extends Minion> vexType = ModEntityType.ALLY_VEX.get();

        // 2️⃣ 创建实体
        AllyVex vex = new AllyVex(vexType, level);

        // 3️⃣ 设置位置
        vex.moveTo(player.getX(), player.getY() + 1.5, player.getZ(),
                player.getYRot(), player.getXRot());

        // 4️⃣ 绑定主人
        vex.setTrueOwner(player);

        // 5️⃣ 初始化装备、附魔
        vex.finalizeSpawn(level,
                level.getCurrentDifficultyAt(vex.blockPosition()),
                MobSpawnType.MOB_SUMMONED,
                null, null);

        // 6️⃣ 永久存在
        vex.setHasLifespan(false);

        // 7️⃣ 添加到世界
        level.addFreshEntity(vex);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, slimeknights.tconstruct.library.modifiers.ModifierHooks.MELEE_HIT);
    }
}