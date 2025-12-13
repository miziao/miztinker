package com.mizi.miztinker.item.tool;


import com.mizi.miztinker.entity.MiztinkerEntityRegister;
import com.mizi.miztinker.entity.ScabbardEntity.ScabbardEntity;
import com.mizi.miztinker.entity.ScabbardEntity.UltimateSlashEntity;
import com.mizi.miztinker.item.tool.until.MiztinkerTools;
import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.network.packets.HudCharge.MurasamaEnergyPointCharge;
import com.mizi.miztinker.network.packets.HudCharge.MurasamaEnergyQuantityCharge;
import com.mizi.miztinker.network.packets.ScabbardPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSwapItemsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.helper.TooltipBuilder;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.modifiers.ability.interaction.BlockingModifier;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.mizi.miztinker.item.tool.until.ToolDefinitions.MIRASAMA;
import static com.mizi.miztinker.miztinker.getResource;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.forceAddEffect;
import static com.momosensei.momotinker.util.AttackUtil.getCooldownFunctionFloat;
import static slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook.KEY_DRAWTIME;

public class murasama extends ModifiableItem {
    public murasama(Properties properties) {
        super(properties, MIRASAMA);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingSwapItems);
        MinecraftForge.EVENT_BUS.addListener(this::OnLivingAttack);
        MinecraftForge.EVENT_BUS.addListener(this::OnLivingHurt);
        MinecraftForge.EVENT_BUS.addListener(this::LeftClick);
        MinecraftForge.EVENT_BUS.addListener(this::LeftClickBlock);
        MinecraftForge.EVENT_BUS.addListener(this::OnEntityDeath);
        MinecraftForge.EVENT_BUS.addListener(this::OnPlayerTick);
    }
    public static final ResourceLocation murasam_slash_cooldown = getResource("murasam_slash_cooldown");
    public static final ResourceLocation player_murasama_lock_a = getResource("player_murasama_lock_a");
    public static final ResourceLocation player_murasama_lock_b = getResource("player_murasama_lock_b");
    public static final ResourceLocation tool_murasama_lock_a = getResource("tool_murasama_lock_a");
    public static final ResourceLocation tool_murasama_lock_b = getResource("tool_murasama_lock_b");

    public void onLivingSwapItems(LivingSwapItemsEvent event) {
        if (event.isCancelable() && event.getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof murasama) {
            ToolStack tool = ToolStack.from(player.getMainHandItem());
            if (player instanceof ServerPlayer serverPlayer&&tool.getPersistentData().getFloat(ascending_cut)==60) {
                if (!CanCreateScabbard(serverPlayer)
                        &&player.getPersistentData().getFloat(murasam_slash_cooldown.toString())==0
                        &&(tool.getPersistentData().getBoolean(tool_murasama_lock_a)||isTrueNameA(tool)||isTrueNameB(tool))){
                    createScabbard(serverPlayer,0);
                }
            }
            event.setCanceled(true);
        }
    }
    public void OnLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getMainHandItem().getItem() instanceof murasama) {
                ToolStack tool = ToolStack.from(player.getMainHandItem());
                if (isTrueNameB(tool)){
                    event.setAmount(event.getAmount() * 0.01f);
                }
            }
        }
    }
    private void OnPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Player player1=event.player;
        if (player1.level instanceof ServerLevel&&player1 instanceof ServerPlayer player) {
            if (player.getPersistentData().getFloat(murasam_slash_cooldown.toString())>0){
                player.getPersistentData().putFloat(murasam_slash_cooldown.toString(),player.getPersistentData().getFloat(murasam_slash_cooldown.toString())-1);
            }else
            if (player.getPersistentData().getFloat(murasam_slash_cooldown.toString())<0){
                player.getPersistentData().putFloat(murasam_slash_cooldown.toString(),0);
            }
        }
    }

    public static final ResourceLocation cannot_create_scabbard = getResource("cannot_create_scabbard");
    public static final ResourceLocation is_smash_down = getResource("is_smash_down");
    public static final ResourceLocation can_cut_entity = getResource("can_cut_entity");

    public static void createScabbard(ServerPlayer player,int form) {
        if (!(ToolStack.from(player.getMainHandItem()).getItem() instanceof murasama) || player.getAttackStrengthScale(0) != 1) {
            return;
        }
        ToolStack tool = ToolStack.from(player.getMainHandItem());
        if (tool.isBroken()) {
            return;
        }

        Level level = player.level();
        EntityType<ScabbardEntity> entityType = getScabbardType();
        ScabbardEntity scabbard = new ScabbardEntity(entityType, level);

        scabbard.setOwner(player);
        scabbard.noPhysics = true;
        scabbard.setToolStack(tool);
        scabbard.main_hand=tool;
        if (form==0) {
            scabbard.setForm(0);
            scabbard.setSpawnRotation(player.getYRot(), player.getXRot());
            scabbard.setPos(player.getX(), player.getY() + 0.5 * player.getBbHeight(), player.getZ());
        }else if (form==1){
            scabbard.setForm(1);
            scabbard.setPos(player.getX(), player.getY(), player.getZ());
            scabbard.setSpawnRotation(player.getYRot(), 90.0F);

            ModDataNBT data = tool.getPersistentData();
            data.putFloat(ascending_points,data.getFloat(ascending_points)-1);
            player.getPersistentData().putBoolean(cannot_create_scabbard.toString(), true);
            player.getPersistentData().putBoolean(is_smash_down.toString(),true);
        }

        level.addFreshEntity(scabbard);
        player.getPersistentData().putBoolean(cannot_create_scabbard.toString(), true);
        tool.getPersistentData().putFloat(ascending_cut,0);
        ToolDamageUtil.damageAnimated(tool, 1, player, InteractionHand.MAIN_HAND);
    }
    public static void createDimensionSlash(ServerPlayer player) {
        if (!(ToolStack.from(player.getMainHandItem()).getItem() instanceof murasama) || player.getAttackStrengthScale(0) != 1) {
            return;
        }
        ToolStack tool = ToolStack.from(player.getMainHandItem());
        if (tool.isBroken()) {
            return;
        }

        Level level = player.level();
        EntityType<UltimateSlashEntity> entityType = getDimensionSlashType();
        UltimateSlashEntity entity = new UltimateSlashEntity(entityType, level);

        entity.setOwner(player);
        entity.noPhysics = true;
        entity.setToolStack(tool);
        entity.setPos(player.getX(), player.getY(), player.getZ());

        level.addFreshEntity(entity);
    }
    public static EntityType<ScabbardEntity> getScabbardType() {
        return MiztinkerEntityRegister.scabbard_entity.get();
    }
    public static EntityType<UltimateSlashEntity> getDimensionSlashType() {
        return MiztinkerEntityRegister.ultimate_slash.get();
    }

    public static final ResourceLocation ascending_cooldown = getResource("ascending_cooldown");
    public static final ResourceLocation ascending_cut = getResource("ascending_cut");
    public static final ResourceLocation ascending_points = getResource("ascending_points");

    private final Map<UUID, Integer> cutCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> pointsCache = new ConcurrentHashMap<>();

    public void OnLivingAttack(LivingAttackEvent event) {
        Entity a = event.getEntity();
        Entity b = event.getSource().getEntity();
        if (b instanceof Player player && a != null && player.getMainHandItem().is(MiztinkerTools.murasama.get())) {
            ModDataNBT c = ToolStack.from(player.getMainHandItem()).getPersistentData();
            c.putFloat(ascending_cooldown, 12);
            int d = (int) Math.floor(getCooldownFunctionFloat(player, InteractionHand.MAIN_HAND)*8+4);
            if (c.getFloat(ascending_cooldown)>0&&c.getFloat(ascending_cut)<60&&!CanCreateScabbard(player)&&player.getPersistentData().getFloat(murasam_slash_cooldown.toString())==0){
                c.putFloat(ascending_cut,c.getFloat(ascending_cut)+d);
            }
            if (isTrueNameB(ToolStack.from(player.getMainHandItem()))){
                forceAddEffect(a,new MobEffectInstance(MiztinkerEffect.DestinedDeath.get(),100,0));
            }
        }
        if (a instanceof Player player && b != null && player.getMainHandItem().is(MiztinkerTools.murasama.get())) {
            ModDataNBT c = ToolStack.from(player.getMainHandItem()).getPersistentData();
            c.putFloat(ascending_cooldown, 12);
        }
    }

    public static boolean CanCreateScabbard(Entity owner) {
        String s = cannot_create_scabbard.toString();
        return owner.getPersistentData().getBoolean(s);
    }
    private void LeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        Player player=event.getEntity();
        if (player != null && player.getMainHandItem().getItem() instanceof murasama ) {
            MiztinkerNetwork.sendToServer(new ScabbardPacket(player.getId()));
        }
    }
    private void LeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player=event.getEntity();
        if (player !=null && player.getMainHandItem().getItem() instanceof murasama) {
            MiztinkerNetwork.sendToServer(new ScabbardPacket(player.getId()));
        }
    }
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (player instanceof ServerPlayer serverPlayer){
            MiztinkerNetwork.sendToServer(new ScabbardPacket(serverPlayer.getId()));
        }
        return super.onLeftClickEntity(stack, player, target);
    }

    private void OnEntityDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            CompoundTag tag = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            String a = player_murasama_lock_a.toString();
            String b = player_murasama_lock_b.toString();
            if (event.getEntity() instanceof WitherBoss &&!tag.getBoolean(a)) {
                player.getPersistentData().getBoolean(a);
                tag.putBoolean(a, true);
                player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
            }
            if (event.getEntity() instanceof EnderDragon &&!tag.getBoolean(b)) {
                player.getPersistentData().getBoolean(b);
                tag.putBoolean(b, true);
                player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
            }
        }

    }
    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        InventoryTickModifierHook.heldInventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        ModDataNBT data = ToolStack.from(stack).getPersistentData();
        if (entityIn instanceof Player player && player.tickCount % 20 == 0) {
            if (data.getFloat(ascending_cooldown) == 0) {
                if (data.getFloat(ascending_cut) > 0) {
                    data.putFloat(ascending_cut, data.getFloat(ascending_cut) - 10);
                }
            } else if (data.getFloat(ascending_cooldown) > 0) {
                data.putFloat(ascending_cooldown, data.getFloat(ascending_cooldown) - 1);
                if (data.getFloat(ascending_cut) < 60) {
                    data.putFloat(ascending_cut, data.getFloat(ascending_cut) + 2);
                }
            }

            if (data.getFloat(ascending_cooldown) < 0) {
                data.putFloat(ascending_cooldown, 0);
            }

            if (data.getFloat(ascending_cut) < 0) {
                data.putFloat(ascending_cut, 0);
            }
            if (data.getFloat(ascending_cut) > 60) {
                data.putFloat(ascending_cut, 60);
            }

            if (data.getFloat(ascending_points) < 0) {
                data.putFloat(ascending_points, 0);
            }

            CompoundTag tag = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            String a = player_murasama_lock_a.toString();
            String b = player_murasama_lock_b.toString();
            if (tag.getBoolean(a)) {
                data.putBoolean(tool_murasama_lock_a,true);
            }
            if (tag.getBoolean(b)) {
                data.putBoolean(tool_murasama_lock_b,true);
            }
        }
        if (entityIn instanceof Player player && player.tickCount % 60 == 0) {
            if (data.getInt(ascending_cooldown) == 0 && data.getFloat(ascending_points) > 0) {
                data.putFloat(ascending_points, data.getFloat(ascending_points) - 1);
            }
        }
        if (entityIn instanceof ServerPlayer player1 && stack == player1.getMainHandItem()) {
            UUID playerId = player1.getUUID();

            float perc = Mth.clamp(data.getFloat(ascending_cut) / 60, 0, 1);
            int currentStage = (int) Math.floor(perc * 10);
            if (cutCache.getOrDefault(playerId, -1) != currentStage) {
                MiztinkerNetwork.sendToPlayer(new MurasamaEnergyQuantityCharge(perc), player1);
                cutCache.put(playerId, currentStage);
            }

            float perc1 = Mth.clamp(data.getFloat(ascending_points) / 10, 0, 1);
            int currentStage1 = (int) Math.floor(perc1 * 10);
            if (pointsCache.getOrDefault(playerId, -1) != currentStage1) {
                MiztinkerNetwork.sendToPlayer(new MurasamaEnergyPointCharge(perc1), player1);
                pointsCache.put(playerId, currentStage1);
            }
        }
    }
    private final static String TRUE_NAME_A = "Jetstream_Sam";
    private final static String TRUE_NAME_B = "Lost_In_Tianyi";
    public static boolean isTrueNameA(ToolStack toolStack) {
        Component nameComponent = toolStack.createStack().getHoverName();
        String toolName = nameComponent.getString();
        return toolName.toLowerCase().contains(TRUE_NAME_A.toLowerCase());
    }
    public static boolean isTrueNameB(ToolStack toolStack) {
        Component nameComponent = toolStack.createStack().getHoverName();
        String toolName = nameComponent.getString();
        return toolName.toLowerCase().contains(TRUE_NAME_B.toLowerCase());
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int duration) {
        ToolStack tool = ToolStack.from(stack);
        if (tool.isBroken()){
            tool.getPersistentData().remove(KEY_DRAWTIME);
            return;
        }
        if (livingEntity instanceof ServerPlayer player) {
            player.awardStat(Stats.ITEM_USED.get(this));
            ToolDamageUtil.damageAnimated(tool, 1, player);
        }
        tool.getPersistentData().remove(KEY_DRAWTIME);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int chargeRemaining) {
        ModDataNBT data = ToolStack.from(stack).getPersistentData();
        if (data.getFloat(ascending_cut)<60&&living.getPersistentData().getFloat(murasam_slash_cooldown.toString())==0){
            data.putFloat(ascending_cut,data.getFloat(ascending_cut)+2);
            if (data.getFloat(ascending_cooldown)<=4) {
                data.putFloat(ascending_cooldown,4);
            }
        }
    }
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ToolStack tool = ToolStack.from(stack);
        player.startUsingItem(hand);
        if (tool.isBroken()){
            return InteractionResultHolder.fail(stack);
        }
        if (!tool.isBroken()) {
            return InteractionResultHolder.pass(stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return BlockingModifier.blockWhileCharging(ToolStack.from(stack), UseAnim.BLOCK);
    }
    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        return !player.isCreative();
    }

    public List<Component> getStatInformation(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
        tooltips = this.getStats(tool, player, tooltips, key, tooltipFlag);
        return tooltips;
    }
    public List<Component> getStats(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
        TooltipBuilder builder = new TooltipBuilder(tool, tooltips);
        if (tool.hasTag(TinkerTags.Items.DURABILITY)) {
            builder.add(ToolStats.DURABILITY);
        }
        if (tool.hasTag(TinkerTags.Items.MELEE)) {
            builder.add(ToolStats.ATTACK_DAMAGE);
            builder.add(ToolStats.ATTACK_SPEED);
        }
        builder.add(Component.translatable("item.miztinker.tooltip.murasama0"));

        Component a = Component.translatable("item.miztinker.tooltip.murasama_lock");
        Component b = Component.translatable("item.miztinker.tooltip.murasama_unlock");
        if (!tool.getPersistentData().getBoolean(tool_murasama_lock_a)&&!isTrueNameA((ToolStack) tool)&&!isTrueNameB((ToolStack) tool)){
            builder.add(Component.translatable("item.miztinker.tooltip.murasama1").append(a));
            builder.add(Component.translatable("item.miztinker.tooltip.murasama2").append(a));
        } else {
            builder.add(Component.translatable("item.miztinker.tooltip.murasama1").append(b));
            builder.add(Component.translatable("item.miztinker.tooltip.murasama2").append(b));
        }
        if (!tool.getPersistentData().getBoolean(tool_murasama_lock_b)&&!isTrueNameA((ToolStack) tool)&&!isTrueNameB((ToolStack) tool)){
            builder.add(Component.translatable("item.miztinker.tooltip.murasama3").append(a));
        } else {
            builder.add(Component.translatable("item.miztinker.tooltip.murasama3").append(b));
        }
        builder.add(Component.translatable("item.miztinker.tooltip.murasama4").withStyle(ChatFormatting.GOLD));
        if ((tool.getPersistentData().getBoolean(tool_murasama_lock_b)&&tool.getPersistentData().getBoolean(tool_murasama_lock_a))||isTrueNameA((ToolStack) tool)||isTrueNameB((ToolStack) tool)){
            builder.add(Component.translatable("item.miztinker.tooltip.murasama5").withStyle(ChatFormatting.BLUE));
            builder.add(Component.translatable("item.miztinker.tooltip.murasama6").withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.ITALIC));
        } else {
            builder.add(Component.translatable("item.miztinker.tooltip.murasama5a").withStyle(ChatFormatting.BLUE));
        }

        if (isTrueNameA((ToolStack) tool)||isTrueNameB((ToolStack) tool)) {
            builder.add(Component.translatable("item.miztinker.tooltip.murasama8").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.ITALIC));
        }
        if (isTrueNameB((ToolStack) tool)) {
            builder.add(Component.translatable("item.miztinker.tooltip.murasama9").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.ITALIC));
            builder.add(Component.translatable("item.miztinker.tooltip.murasama10").withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.ITALIC));
        } else {
            builder.add(Component.translatable("item.miztinker.tooltip.murasama7").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.ITALIC));
        }
        builder.add(Component.translatable("item.miztinker.tooltip.murasama11").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.ITALIC));

        builder.addAllFreeSlots();
        Iterator var7 = tool.getModifierList().iterator();
        while(var7.hasNext()) {
            ModifierEntry entry = (ModifierEntry)var7.next();
            entry.getHook(ModifierHooks.TOOLTIP).addTooltip(tool, entry, player, tooltips, key, tooltipFlag);
        }
        return tooltips;
    }
}