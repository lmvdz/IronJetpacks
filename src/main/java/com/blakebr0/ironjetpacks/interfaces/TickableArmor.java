package com.blakebr0.ironjetpacks.interfaces;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface TickableArmor {
    /**
     * Invoked every tick when this item is equipped.
     *
     * @param stack  The item stack of the armor.
     * @param player The player wearing the armor.
     */
    void tickArmor(ItemStack stack, PlayerEntity player);
}
