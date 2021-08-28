package com.blakebr0.ironjetpacks.client.model;

import com.blakebr0.ironjetpacks.item.JetpackItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import team.reborn.energy.EnergyHandler;

/*
 * This is a slightly modified version of the model from Simply Jetpacks
 * https://github.com/Tomson124/SimplyJetpacks-2/blob/1.12/src/main/java/tonius/simplyjetpacks/client/model/ModelJetpack.java
 */
@Environment(EnvType.CLIENT)
public class JetpackModel extends BipedEntityModel<LivingEntity> {
    private final JetpackItem jetpack;

    public JetpackModel(JetpackItem jetpack) {
        super(getJetpackModelData(new Dilation(0F), 0F).getRoot().createPart(64, 64));
        this.jetpack = jetpack;

        this.body.visible = true;
        this.rightArm.visible = false;
        this.leftArm.visible = false;
        this.head.visible = false;
        this.hat.visible = false;
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;
    }

    public static ModelData getJetpackModelData(Dilation dilation, float pivotOffsetY) {
        ModelData bipedModelData = getModelData(dilation, pivotOffsetY);
        ModelPartData bipedBodyModelPartData = bipedModelData.getRoot().getChild("body");
        bipedBodyModelPartData.addChild("middle", ModelPartBuilder.create().mirrored(true).uv(0, 54).cuboid(-2F, 5F, 3.6F, 4, 3, 2), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
        bipedBodyModelPartData.addChild("leftCanister", ModelPartBuilder.create().mirrored(true).uv(0, 32).cuboid(0.5F, 2F, 2.6F, 4, 7, 4), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
        bipedBodyModelPartData.addChild("rightCanister", ModelPartBuilder.create().mirrored(true).uv(17, 32).cuboid(-4.5F, 2F, 2.6F, 4, 7, 4), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
        bipedBodyModelPartData.addChild("leftTip1", ModelPartBuilder.create().mirrored(true).uv(0, 45).cuboid(1F, 0F, 3.1F, 3, 2, 3), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
        bipedBodyModelPartData.addChild("leftTip2", ModelPartBuilder.create().mirrored(true).uv(0, 50).cuboid(1.5F, -1F, 3.6F, 2, 1, 2), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
        bipedBodyModelPartData.addChild("rightTip1", ModelPartBuilder.create().mirrored(true).uv(17, 45).cuboid(-4F, 0F, 3.1F, 3, 2, 3), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
        bipedBodyModelPartData.addChild("rightTip2", ModelPartBuilder.create().mirrored(true).uv(17, 50).cuboid(-3.5F, -1F, 3.6F, 2, 1, 2), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
        bipedBodyModelPartData.addChild("leftExhaust1", ModelPartBuilder.create().mirrored(true).uv(35, 32).cuboid(1F, 9F, 3.1F, 3, 1, 3), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
        bipedBodyModelPartData.addChild("leftExhaust2", ModelPartBuilder.create().mirrored(true).uv(35, 37).cuboid(0.5F, 10F, 2.6F, 4, 3, 4), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
        bipedBodyModelPartData.addChild("rightExhaust1", ModelPartBuilder.create().mirrored(true).uv(48, 32).cuboid(-4F, 9F, 3.1F, 3, 1, 3), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
        bipedBodyModelPartData.addChild("rightExhaust2", ModelPartBuilder.create().mirrored(true).uv(35, 45).cuboid(-4.5F, 10F, 2.6F, 4, 3, 4), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));

        for (int i = 0; i < 6; i++) {
            bipedBodyModelPartData.addChild("energyBarLeft"+i, ModelPartBuilder.create().mirrored(false).uv(16 + (i * 4), 55).cuboid(2F, 3F, 5.8F, 1, 5, 1, dilation), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
            bipedBodyModelPartData.addChild("energyBarRight"+i, ModelPartBuilder.create().mirrored(false).uv(16 + (i * 4), 55).cuboid(-3F, 3F, 5.8F, 1, 5, 1, dilation), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
        }
        return bipedModelData;
    }

    @Override
    public void setAngles(LivingEntity entity, float limbAngle, float limbDistance, float animationProgress, float netHeadYaw, float headPitch) {
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, netHeadYaw, headPitch);

        if (this.jetpack.getJetpack().creative) {
            this.resetEnergyBars();
            this.body.getChild("energyBarLeft5").visible = true;
            this.body.getChild("energyBarRight5").visible = true;
        } else {
            ItemStack chest = entity.getEquippedStack(EquipmentSlot.CHEST);
            EnergyHandler energy = this.jetpack.getEnergyStorage(chest);
            double stored = energy.getEnergy() / energy.getMaxStored();

            int state = 0;
            state = (int) Math.min(Math.max(0, Math.ceil(stored * 5)), 5);
            System.out.println(state + " " + stored);
            if (stored > 0.8) {
                state = 5;
            } else if (stored > 0.6) {
                state = 4;
            } else if (stored > 0.4) {
                state = 3;
            } else if (stored > 0.2) {
                state = 2;
            } else if (stored > 0) {
                state = 1;
            }

            this.resetEnergyBars();
            this.body.getChild("energyBarLeft"+state).visible = true;
            this.body.getChild("energyBarRight"+state).visible = true;
        }
    }

    private void resetEnergyBars() {
        for (int i = 0; i < 6; i++) {
            this.body.getChild("energyBarLeft"+i).visible = false;
            this.body.getChild("energyBarRight"+i).visible = false;
        }
    }
}
