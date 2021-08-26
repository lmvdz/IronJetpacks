package com.blakebr0.ironjetpacks.util;

import com.blakebr0.ironjetpacks.handler.InputHandler;
import com.blakebr0.ironjetpacks.item.JetpackItem;
import com.blakebr0.ironjetpacks.registry.Jetpack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class JetpackUtils {
    public static boolean isFlying(PlayerEntity player) {
        ItemStack stack = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!stack.isEmpty()) {
            Item item = stack.getItem();
            if (item instanceof JetpackItem) {
                JetpackItem jetpack = (JetpackItem) item;
                if (jetpack.isEngineOn(stack) && (jetpack.getEnergyStorage(stack).getEnergy() > 0 || player.isCreative() || jetpack.getJetpack().creative)) {
                    if (jetpack.isHovering(stack)) {
                        return !player.isOnGround();
                    } else {
                        return InputHandler.isHoldingUp(player);
                    }
                }
            }
        }
        
        return false;
    }
    
    public static ArmorMaterial makeArmorMaterial(Jetpack jetpack) {
        return new ArmorMaterial() {
            @Override
            public int getDurability(EquipmentSlot slot) {
                return 0;
            }
            
            @Override
            public int getProtectionAmount(EquipmentSlot slot) {
                return jetpack.armorPoints;
            }
            
            @Override
            public int getEnchantability() {
                return jetpack.enchantablilty;
            }
            
            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
            }
            
            @Override
            public Ingredient getRepairIngredient() {
                return null;
            }
            
            @Environment(EnvType.CLIENT)
            @Override
            public String getName() {
                return "jetpack";
            }
            
            @Override
            public float getToughness() {
                return 0;
            }
    
            @Override
            public float getKnockbackResistance() {
                return 0;
            }
        };
    }
}
