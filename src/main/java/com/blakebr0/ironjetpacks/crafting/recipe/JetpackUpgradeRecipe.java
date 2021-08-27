package com.blakebr0.ironjetpacks.crafting.recipe;

import com.blakebr0.ironjetpacks.crafting.ModRecipeSerializers;
import com.blakebr0.ironjetpacks.item.JetpackItem;
import com.blakebr0.ironjetpacks.mixins.ShapedRecipeAccessor;
import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class JetpackUpgradeRecipe extends ShapedRecipe {
    public JetpackUpgradeRecipe(Identifier id, String group, int recipeWidth, int recipeHeight, DefaultedList<Ingredient> inputs, ItemStack output) {
        super(id, group, recipeWidth, recipeHeight, inputs, output);
    }
    
    @Override
    public ItemStack craft(CraftingInventory inv) {
        ItemStack jetpack = inv.getStack(4);
        ItemStack result = this.getOutput().copy();
        
        if (!jetpack.isEmpty() && jetpack.getItem() instanceof JetpackItem) {
            NbtCompound tag = jetpack.getNbt();
            if (tag != null) {
                result.setNbt(tag);
                return result;
            }
        }
        
        return result;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_JETPACK_UPGRADE.get();
    }
    
    public static class Serializer implements RecipeSerializer<JetpackUpgradeRecipe> {
        @Override
        public JetpackUpgradeRecipe read(Identifier recipeId, JsonObject json) {
            ShapedRecipe recipe = RecipeSerializer.SHAPED.read(recipeId, json);
            return new JetpackUpgradeRecipe(recipeId, ((ShapedRecipeAccessor) recipe).getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput());
        }
        
        @Override
        public JetpackUpgradeRecipe read(Identifier recipeId, PacketByteBuf buffer) {
            int i = buffer.readVarInt();
            int j = buffer.readVarInt();
            String s = buffer.readString(32767);
            DefaultedList<Ingredient> inputs = DefaultedList.ofSize(i * j, Ingredient.EMPTY);
            
            for (int k = 0; k < inputs.size(); k++) {
                inputs.set(k, Ingredient.fromPacket(buffer));
            }
            
            ItemStack output = buffer.readItemStack();
            return new JetpackUpgradeRecipe(recipeId, s, i, j, inputs, output);
        }
        
        @Override
        public void write(PacketByteBuf buffer, JetpackUpgradeRecipe recipe) {
            buffer.writeVarInt(recipe.getWidth());
            buffer.writeVarInt(recipe.getHeight());
            buffer.writeString(((ShapedRecipeAccessor) recipe).getGroup());
            
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.write(buffer);
            }
            
            buffer.writeItemStack(recipe.getOutput());
        }
    }
}
