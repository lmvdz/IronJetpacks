package com.blakebr0.ironjetpacks.mixins;

import com.blakebr0.ironjetpacks.item.JetpackItem;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {

    @Shadow
    @Final
    private static Map<String, Identifier> ARMOR_TEXTURE_CACHE;

    @Inject(method = "getArmorTexture", at = @At("HEAD"), cancellable = true)
    public void getArmorTexture(ArmorItem item, boolean legs, String overlay, CallbackInfoReturnable<Identifier> cir) {
        if (item instanceof JetpackItem jetpackItem) {
            cir.setReturnValue(ARMOR_TEXTURE_CACHE.computeIfAbsent(jetpackItem.getArmorTexture(legs), Identifier::new));
        }
    }
}
