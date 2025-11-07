package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.util.List;

public class SoulEat extends Modifier implements MeleeHitModifierHook, MeleeDamageModifierHook, TooltipModifierHook {

    static final String TAG_SOUL_BONUS = "soul_bonus";
    private static final String TAG_SOUL_KILLS = "soul_kills";
    private static final float BASE_RATIO = 0.1f; // 每级吸收10%

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    /** 击杀检测：在近战命中后触发 */
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        Player player = context.getPlayerAttacker();
        Level level = context.getAttacker().level();

        if (level.isClientSide || player == null || target == null || target.isAlive()) return;

        ModDataNBT data = tool.getPersistentData();
        String baseKey = getId().toString();

        float bonus = data.getFloat(ResourceLocation.parse(baseKey + "." + TAG_SOUL_BONUS));
        int kills = data.getInt(ResourceLocation.parse(baseKey + "." + TAG_SOUL_KILLS));

        float ratio = BASE_RATIO * modifier.getLevel();
        float gain = target.getMaxHealth() * ratio;

        data.putFloat(ResourceLocation.parse(baseKey + "." + TAG_SOUL_BONUS), bonus + gain);
        //if (Thread.currentThread().getName().contains("Server"))
        //Minecraft.getInstance().player.sendSystemMessage(Component.literal("On set : " + bonus + gain));
        data.putInt(ResourceLocation.parse(baseKey + "." + TAG_SOUL_KILLS), kills + 1);


        player.displayClientMessage(Component.literal(
                String.format("§d噬魂吸收: +%.2f 伤害 (Lv.%d)", gain, modifier.getLevel())
        ), true);
    }

    /** 应用额外伤害 */
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        ModDataNBT data = tool.getPersistentData();
        float bonus = data.getFloat(ResourceLocation.parse(getId().toString() + "." + TAG_SOUL_BONUS));
        return damage + bonus;
    }

    /** 显示提示信息 */
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag flag) {
        ModDataNBT data = tool.getPersistentData();
        String baseKey = getId().toString();

        float bonus = data.getFloat(ResourceLocation.parse(baseKey + "." + TAG_SOUL_BONUS));
        int kills = data.getInt(ResourceLocation.parse(baseKey + "." + TAG_SOUL_KILLS));

        if (kills > 0) {
            tooltip.add(Component.literal("击杀数: " + kills).withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.literal(String.format("噬魂加成: +%.2f 伤害", bonus)).withStyle(ChatFormatting.RED));
        }

        float ratio = BASE_RATIO * modifier.getLevel() * 100f;
        tooltip.add(Component.literal(String.format("每击杀吸收 %.0f%% 生命值", ratio)).withStyle(ChatFormatting.GRAY));
    }

}