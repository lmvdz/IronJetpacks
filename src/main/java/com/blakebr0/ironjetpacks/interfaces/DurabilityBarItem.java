package com.blakebr0.ironjetpacks.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public interface DurabilityBarItem {
    @Environment(EnvType.CLIENT)
    double getDurabilityBarProgress(ItemStack stack);

    @Environment(EnvType.CLIENT)
    boolean hasDurabilityBar(ItemStack stack);

    @Environment(EnvType.CLIENT)
    default int getDurabilityBarColor(ItemStack stack) {
        return MathHelper.hsvToRgb(Math.max(0.0F, 1 - (float) getDurabilityBarProgress(stack)) / 3.0F, 1.0F, 1.0F);
    }
}
