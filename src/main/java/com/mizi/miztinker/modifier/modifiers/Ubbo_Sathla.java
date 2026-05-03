package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.util.List;

public class Ubbo_Sathla extends Modifier
        implements InventoryTickModifierHook, MeleeHitModifierHook, TooltipModifierHook {

    private static final ResourceLocation SAVED_MOB = ResourceLocation.fromNamespaceAndPath("miztinker", "ubbo_saved_mob");
    private static final ResourceLocation SAVED_MOB_NBT = ResourceLocation.fromNamespaceAndPath("miztinker", "ubbo_saved_mob_nbt");
    private static final ResourceLocation TICK_COUNTER = ResourceLocation.fromNamespaceAndPath("miztinker", "ubbo_tick_counter");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier,
                              ToolAttackContext context, float damageDealt) {

        if (damageDealt <= 0) return;
        LivingEntity target = context.getLivingTarget();
        Player attacker = context.getPlayerAttacker();
        if (target == null || attacker == null) return;

        ModDataNBT data = tool.getPersistentData();

        ResourceLocation mobId = EntityType.getKey(target.getType());

        data.putString(SAVED_MOB, mobId.toString());

        int lvl = modifier.getLevel();

        if (lvl >= 3) {
            CompoundTag full = new CompoundTag();
            target.saveWithoutId(full);

            CompoundTag clean = new CompoundTag();

            String[] whitelist = new String[]{
                    "Attributes",
                    "ArmorItems",
                    "HandItems",
                    "CustomName",
                    "Health"
            };

            for (String key : whitelist) {
                if (full.contains(key)) {
                    clean.put(key, full.get(key));
                }
            }

            data.put(SAVED_MOB_NBT, clean);

            attacker.displayClientMessage(
                    Component.literal("§a乌波-萨斯拉：已记录的完整的 DNA"),
                    true
            );
        }

        else {
            data.remove(SAVED_MOB_NBT);

            attacker.displayClientMessage(
                    Component.literal("§a乌波-萨斯拉：已记录生物 §e" + mobId),
                    true
            );
        }
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level level,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        if (level.isClientSide) return;
        if (!(holder instanceof Player player)) return;

        if (!isSelected) {
            tool.getPersistentData().remove(TICK_COUNTER);
            return;
        }

        ModDataNBT data = tool.getPersistentData();

        if (!data.contains(SAVED_MOB)) return;

        if (!player.isCrouching()) {
            data.remove(TICK_COUNTER);
            return;
        }

        if (player.totalExperience < 2) return;

        player.giveExperiencePoints(-2);

        int tick = data.getInt(TICK_COUNTER) + 1;
        data.putInt(TICK_COUNTER, tick);

        int lvl = Math.max(1, entry.getLevel());
        double needTick = 50.0 * Math.pow(0.5, lvl - 1);

        if (tick >= needTick) {
            data.putInt(TICK_COUNTER, 0);
            spawnMob(level, player, data);
        }
    }

    private void spawnMob(Level level, Player player, ModDataNBT data) {
        if (!(level instanceof ServerLevel server)) return;

        String mobIdStr = data.getString(SAVED_MOB);
        ResourceLocation id = ResourceLocation.parse(mobIdStr);

        EntityType<?> type = server.registryAccess()
                .registryOrThrow(Registries.ENTITY_TYPE)
                .get(id);

        if (type == null) {
            player.displayClientMessage(
                    Component.literal("§c乌波-萨斯拉：记录的生物 DNA 无效"),
                    true
            );
            return;
        }

        double px = player.getX() + (server.random.nextDouble() - 0.5) * 4;
        double py = player.getY();
        double pz = player.getZ() + (server.random.nextDouble() - 0.5) * 4;

        Entity entity;

        /* 完整(白名单) NBT 召唤 */
        if (data.contains(SAVED_MOB_NBT)) {
            CompoundTag tag = data.getCompound(SAVED_MOB_NBT);
            tag.putString("id", mobIdStr);

            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(px));
            pos.add(DoubleTag.valueOf(py));
            pos.add(DoubleTag.valueOf(pz));
            tag.put("Pos", pos);

            entity = type.create(server);
            if (entity == null) return;

            entity.setLevel(server);
            entity.load(tag);
            entity.moveTo(px, py, pz, server.random.nextFloat() * 360F, 0F);

            // ⭐ 设置装备掉落率 100%
            if (entity instanceof Mob mob) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() == EquipmentSlot.Type.ARMOR || slot.getType() == EquipmentSlot.Type.HAND) {
                        mob.setDropChance(slot, 1.0f); // 1.0 = 100% 掉落
                    }
                }
            }

            player.displayClientMessage(
                    Component.literal("§d乌波-萨斯拉：已召唤完整的 DNA 实体！"),
                    true
            );
        }

        /* 普通召唤 */
        else {
            entity = type.create(server);
            if (entity == null) return;

            entity.moveTo(px, py, pz, server.random.nextFloat() * 360F, 0F);

            // ⭐ 同样设置掉落率
            if (entity instanceof Mob mob) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() == EquipmentSlot.Type.ARMOR || slot.getType() == EquipmentSlot.Type.HAND) {
                        mob.setDropChance(slot, 1.0f);
                    }
                }
            }

            player.displayClientMessage(
                    Component.literal("§d乌波-萨斯拉：已召唤 §e" + mobIdStr),
                    true
            );
        }

        server.addFreshEntity(entity);
    }

    /* ============================================================
       4) Tooltip
       ============================================================ */
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry entry, @Nullable Player player,
                           List<Component> tooltip, TooltipKey key, TooltipFlag flag) {

        ModDataNBT data = tool.getPersistentData();

        if (data.contains(SAVED_MOB)) {
            tooltip.add(Component.literal("§6乌波-萨斯拉：记录实体 DNA："));
            tooltip.add(Component.literal(" §e" + data.getString(SAVED_MOB)));

            if (data.contains(SAVED_MOB_NBT)) {
                tooltip.add(Component.literal("§b（已记录完整 DNA）"));
            }

        } else {
            tooltip.add(Component.literal("§7乌波-萨斯拉：尚未记录生物"));
        }
    }
}