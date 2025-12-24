package com.mizi.miztinker.item;

import com.mizi.miztinker.modifier.register.MiztinkerItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.ItemEntity;


public class MozhuaCapItem extends Item {

    public MozhuaCapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 只允许主手
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide) {
            int count = stack.getCount();
            if (count > 0) {
                ItemStack result = new ItemStack(MiztinkerItems.PINK.get(), count);

                ItemEntity entity = new ItemEntity(
                        level,
                        player.getX(),
                        player.getY() + 0.5D,
                        player.getZ(),
                        result
                );
                level.addFreshEntity(entity);

                stack.shrink(count);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}