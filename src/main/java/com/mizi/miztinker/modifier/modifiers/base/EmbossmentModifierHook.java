package com.mizi.miztinker.modifier.modifiers.base;

import java.util.Collection;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationContainer;

public interface EmbossmentModifierHook {
    boolean applyItem(EmbossmentContext context, int inputIndex, boolean secondary);

    record AllMerger(Collection<EmbossmentModifierHook> modules) implements EmbossmentModifierHook {
        @Override
        public boolean applyItem(EmbossmentContext context, int inputIndex, boolean secondary) {
            for (EmbossmentModifierHook module : modules) {
                if (module.applyItem(context, inputIndex, secondary)) {
                    return true;
                }
            }
            return false;
        }
    }

    static class DefaultClass implements EmbossmentModifierHook {
        @Override
        public boolean applyItem(EmbossmentContext context, int inputIndex, boolean secondary) {
            return false;
        }
    }




    @Getter
    public static class EmbossmentContext {
        @Setter
        private ItemStack toolStack;
        private ITinkerStationContainer inv;
        @Setter
        private Component errorMsg;

        public EmbossmentContext(ItemStack toolStack, ITinkerStationContainer inv) {
            this.toolStack = toolStack;
            this.inv = inv;
            this.errorMsg = Component.translatable("recipe.miztinker.embossment_not_allowed");
        }

        public ItemStack getInputStack(int index) { return inv.getInput(index); }

    }
}