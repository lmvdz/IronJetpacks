package com.blakebr0.ironjetpacks.mixins;

import com.blakebr0.ironjetpacks.interfaces.TickableArmor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Shadow
    @Final
    public DefaultedList<ItemStack> armor;

    @Shadow
    @Final
    public PlayerEntity player;

    @Inject(method = "updateItems", at = @At("RETURN"))
    private void updateItems(CallbackInfo ci) {
        for (ItemStack stack : armor) {
            if (stack.getItem() instanceof TickableArmor tickableArmor) {
                tickableArmor.tickArmor(stack, player);
            }
        }
    }
}
