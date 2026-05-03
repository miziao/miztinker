package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.network.EntityColorPacket;
import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.util.ClientOnlyUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.List;
import java.util.Objects;

import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.isFromDummmmmmyMod;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.reflectionPenetratingDamage;
import static net.minecraft.world.entity.ai.attributes.Attributes.ARMOR;

public class ColorModifier extends NoLevelsModifier implements MeleeHitModifierHook, TooltipModifierHook, MeleeDamageModifierHook {

    public static final String MODID = "miztinker";
    public static final ResourceLocation ENTITY_COLOR_RED = new ResourceLocation(MODID, "entity_color_red");
    public static final ResourceLocation ENTITY_COLOR_GREEN = new ResourceLocation(MODID, "entity_color_green");
    public static final ResourceLocation ENTITY_COLOR_BLUE = new ResourceLocation(MODID, "entity_color_blue");

    // 2. 修正 getMeleeDamage 中的判断逻辑
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry entry, ToolAttackContext context, float baseDamage, float damage) {
        if (context.getAttacker().level().isClientSide && !context.isExtraAttack() && !context.isProjectile()) {
            LivingEntity target = context.getLivingTarget();
            if (target != null) {
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                    int[] rgb = com.mizi.miztinker.util.ClientOnlyUtils.getEntityColorFromClient(target);
                    MiztinkerNetwork.sendToServer(new EntityColorPacket(rgb[0], rgb[1], rgb[2]));
                });
            }
        }
        return damage;
    }

    /**
     * 钩子：近战击中后处理
     * 逻辑：在服务端读取 NBT 中保存的颜色数据并触发特殊效果
     */
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        Player holder = context.getPlayerAttacker(); // 获取攻击者玩家

        // 仅在服务端逻辑处理，且确保有攻击目标和攻击者
        if (target == null || holder == null || holder.level().isClientSide) {
            return;
        }

        ModDataNBT data = tool.getPersistentData();
        int r = data.getInt(ENTITY_COLOR_RED);
        int g = data.getInt(ENTITY_COLOR_GREEN);
        int b = data.getInt(ENTITY_COLOR_BLUE);

        // 如果 RGB 全为 0，说明尚未获取到数据（或者是纯黑色实体），跳过逻辑
        if (r == 0 && g == 0 && b == 0) {
            return;
        }

        // --- 绿色：治疗效果 ---
        if (g > 0) {
            holder.heal((float) g / 20f); // 调整了数值比例防止过强
        }

        // --- 蓝色：削减护甲 ---
        if (target.getAttribute(ARMOR) != null) {
            float currentArmor = (float) Objects.requireNonNull(target.getAttribute(ARMOR)).getBaseValue();
            // 设置下限为 0，防止护甲负数导致溢出伤害
            Objects.requireNonNull(target.getAttribute(ARMOR)).setBaseValue(Math.max(0, currentArmor - (float) b / 50f));
        }

        // --- 红色：反击/穿透伤害 ---
        if (!isFromDummmmmmyMod(target) && r > 0) {
            reflectionPenetratingDamage(target, holder, (float) r / 10f);
        }
    }

    /**
     * 钩子：物品工具提示
     * 逻辑：显示当前存储在工具上的 RGB 颜色数值
     */
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        ModDataNBT data = tool.getPersistentData();
        int red = data.getInt(ENTITY_COLOR_RED);
        int green = data.getInt(ENTITY_COLOR_GREEN);
        int blue = data.getInt(ENTITY_COLOR_BLUE);

        // 动态彩虹色标题
        long time = player != null ? player.level().getGameTime() : System.currentTimeMillis() / 50;
        float hue = (time % 120) / 120.0f;
        int rainbow = java.awt.Color.getHSBColor(hue, 0.7f, 0.9f).getRGB() & 0xFFFFFF;

        // 修改第一行名称颜色
        if (!tooltip.isEmpty()) {
            tooltip.set(0, tooltip.get(0).copy().withStyle(s -> s.withColor(rainbow)));
        }

        // 按住 Shift 显示详细 RGB
        if (tooltipKey == TooltipKey.SHIFT) {
            MutableComponent rComp = Component.literal(String.valueOf(red)).withStyle(s -> s.withColor(ChatFormatting.RED));
            MutableComponent gComp = Component.literal(String.valueOf(green)).withStyle(s -> s.withColor(ChatFormatting.GREEN));
            MutableComponent bComp = Component.literal(String.valueOf(blue)).withStyle(s -> s.withColor(ChatFormatting.BLUE));
            MutableComponent separator = Component.literal(", ").withStyle(ChatFormatting.GRAY);

            tooltip.add(Component.translatable("tooltip.miztinker.rgb_values")
                    .withStyle(s -> s.withColor(rainbow))
                    .append(Component.literal(": "))
                    .append(rComp).append(separator).append(gComp).append(separator).append(bComp));
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }
}