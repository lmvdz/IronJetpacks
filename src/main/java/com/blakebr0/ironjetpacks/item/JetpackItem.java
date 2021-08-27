package com.blakebr0.ironjetpacks.item;

import com.blakebr0.ironjetpacks.IronJetpacks;
import com.blakebr0.ironjetpacks.client.model.JetpackModel;
import com.blakebr0.ironjetpacks.config.ModConfigs;
import com.blakebr0.ironjetpacks.handler.InputHandler;
import com.blakebr0.ironjetpacks.interfaces.DurabilityBarItem;
import com.blakebr0.ironjetpacks.interfaces.TickableArmor;
import com.blakebr0.ironjetpacks.lib.ModTooltips;
import com.blakebr0.ironjetpacks.mixins.ServerPlayNetworkHandlerAccessor;
import com.blakebr0.ironjetpacks.registry.Jetpack;
import com.blakebr0.ironjetpacks.util.JetpackUtils;
import com.blakebr0.ironjetpacks.util.UnitUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import team.reborn.energy.*;

import java.util.List;
public class JetpackItem extends DyeableArmorItem implements Colored, DyeableItem, Enableable, EnergyHolder, TickableArmor, ArmorRenderer, DurabilityBarItem {
    private final Jetpack jetpack;

    @Environment(EnvType.CLIENT)
    private JetpackModel model;

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {

        if (model == null) {
            model = getArmorModel();
        }

        contextModel.setAttributes(model);
        model.setVisible(false);
        model.body.visible = slot == EquipmentSlot.CHEST;
        model.leftArm.visible = slot == EquipmentSlot.CHEST;
        model.rightArm.visible = slot == EquipmentSlot.CHEST;
        model.head.visible = slot == EquipmentSlot.HEAD;

        model.setAngles(entity, 0F, 0F, 0F, model.head.yaw, model.head.pitch);

        ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, getArmorModel(), new Identifier(getArmorTexture(slot.equals(EquipmentSlot.LEGS))));
    }


    public JetpackItem(Jetpack jetpack, Settings settings) {
        super(JetpackUtils.makeArmorMaterial(jetpack), EquipmentSlot.CHEST, settings.maxDamage(0).rarity(jetpack.rarity));
        this.jetpack = jetpack;
        ArmorRenderer.register(this, this);
    }

    @Override
    public Text getName(ItemStack stack) {
        String name = StringUtils.capitalize(this.jetpack.name.replace(" ", "_"));
        return new TranslatableText("item.iron-jetpacks.jetpack", name);
    }

    /*
     * Jetpack logic is very much like Simply Jetpacks, since I used it to learn how to make this work
     * Credit to Tonius & Tomson124
     * https://github.com/Tomson124/SimplyJetpacks-2/blob/1.12/src/main/java/tonius/simplyjetpacks/item/rewrite/ItemJetpack.java
     */
    @Override
    public void tickArmor(ItemStack stack, PlayerEntity player) {
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        Item item = chest.getItem();
        if (!chest.isEmpty() && item instanceof JetpackItem jetpack) {
            if (jetpack.isEngineOn(chest)) {
                boolean hover = jetpack.isHovering(chest);
                if (InputHandler.isHoldingUp(player) || hover && !player.isOnGround()) {
                    Jetpack info = jetpack.getJetpack();

                    double hoverSpeed = InputHandler.isHoldingDown(player) ? info.speedHover : info.speedHoverSlow;
                    double currentAccel = info.accelVert * (player.getVelocity().getY() < 0.3D ? 2.5D : 1.0D);
                    double currentSpeedVertical = info.speedVert * (player.isSubmergedInWater() ? 0.4D : 1.0D);

                    double usage = player.isSprinting() ? info.usage * info.sprintFuel : info.usage;

                    boolean creative = info.creative;

                    EnergyHandler energy = jetpack.getEnergyStorage(chest);
                    if (!creative) {
                        energy.extract(usage);
                    }

                    if (energy.getEnergy() > 0 || creative) {
                        double motionY = player.getVelocity().getY();
                        if (InputHandler.isHoldingUp(player)) {
                            if (!hover) {
                                this.fly(player, Math.min(motionY + currentAccel, currentSpeedVertical));
                            } else {
                                if (InputHandler.isHoldingDown(player)) {
                                    this.fly(player, Math.min(motionY + currentAccel, -info.speedHoverSlow));
                                } else {
                                    this.fly(player, Math.min(motionY + currentAccel, info.speedHover));
                                }
                            }
                        } else {
                            this.fly(player, Math.min(motionY + currentAccel, -hoverSpeed));
                        }

                        float speedSideways = (float) (player.isSneaking() ? info.speedSide * 0.5F : info.speedSide);
                        float speedForward = (float) (player.isSprinting() ? speedSideways * info.sprintSpeed : speedSideways);

                        if (InputHandler.isHoldingForwards(player)) {
                            player.updateVelocity(1, new Vec3d(0, 0, speedForward));
                        }

                        if (InputHandler.isHoldingBackwards(player)) {
                            player.updateVelocity(1, new Vec3d(0, 0, -speedSideways * 0.8F));
                        }

                        if (InputHandler.isHoldingLeft(player)) {
                            player.updateVelocity(1, new Vec3d(speedSideways, 0, 0));
                        }

                        if (InputHandler.isHoldingRight(player)) {
                            player.updateVelocity(1, new Vec3d(-speedSideways, 0, 0));
                        }

                        if (!player.world.isClient()) {
                            player.fallDistance = 0.0F;

                            if (player instanceof ServerPlayerEntity) {
                                ((ServerPlayNetworkHandlerAccessor) ((ServerPlayerEntity) player).networkHandler).setFloatingTicks(0);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return ModConfigs.get().general.enchantableJetpacks && this.jetpack.enchantablilty > 0;
    }

    @Environment(EnvType.CLIENT)
    public double getDurabilityBarProgress(ItemStack stack) {
        EnergyHandler energy = this.getEnergyStorage(stack);
        double stored = energy.getMaxStored() - energy.getEnergy();
        return stored / energy.getMaxStored();
    }

    @Environment(EnvType.CLIENT)
    public boolean hasDurabilityBar(ItemStack stack) {
        return !this.jetpack.creative;
    }

    @Override
    public int getDurabilityBarColor(ItemStack stack) {
        return DurabilityBarItem.super.getDurabilityBarColor(stack);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext advanced) {
        if (!this.jetpack.creative) {
            EnergyHandler energy = this.getEnergyStorage(stack);
            tooltip.add(new LiteralText(UnitUtils.formatEnergy(energy.getEnergy(), null)).formatted(Formatting.GRAY).append(" / ").append(new LiteralText(UnitUtils.formatEnergy(jetpack.capacity, null))));
        } else {
            tooltip.add(new LiteralText("-1 E / ").formatted(Formatting.GRAY).append(ModTooltips.INFINITE.color(Formatting.GRAY)).append(" E"));
        }

        Text tier = ModTooltips.TIER.args(this.jetpack.creative ? "Creative" : this.jetpack.tier).formatted(this.jetpack.rarity.formatting);
        Text engine = ModTooltips.ENGINE.color(isEngineOn(stack) ? Formatting.GREEN : Formatting.RED);
        Text hover = ModTooltips.HOVER.color(isHovering(stack) ? Formatting.GREEN : Formatting.RED);

        tooltip.add(ModTooltips.STATE_TOOLTIP_LAYOUT.args(tier, engine, hover));

        if (ModConfigs.getClient().general.enableAdvancedInfoTooltips) {
            tooltip.add(new LiteralText(""));
            if (!Screen.hasShiftDown()) {
                tooltip.add(new TranslatableText("tooltip.iron-jetpacks.hold_shift_for_info"));
            } else {
                tooltip.add(ModTooltips.FUEL_USAGE.args(this.jetpack.usage + " E/t"));
                tooltip.add(ModTooltips.VERTICAL_SPEED.args(this.jetpack.speedVert));
                tooltip.add(ModTooltips.VERTICAL_ACCELERATION.args(this.jetpack.accelVert));
                tooltip.add(ModTooltips.HORIZONTAL_SPEED.args(this.jetpack.speedSide));
                tooltip.add(ModTooltips.HOVER_SPEED.args(this.jetpack.speedHoverSlow));
                tooltip.add(ModTooltips.DESCEND_SPEED.args(this.jetpack.speedHover));
                tooltip.add(ModTooltips.SPRINT_MODIFIER.args(this.jetpack.sprintSpeed));
                tooltip.add(ModTooltips.SPRINT_FUEL_MODIFIER.args(this.jetpack.sprintFuel));
            }
        }
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (this.isIn(group)) {
            stacks.add(new ItemStack(this));

            if (!jetpack.creative) {
                ItemStack stack = new ItemStack(this);
                Energy.of(stack).set(jetpack.capacity);
                stacks.add(stack);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public JetpackModel getArmorModel() {
        if (model == null) model = new JetpackModel(this);
        return model;
    }

    @Environment(EnvType.CLIENT)
    public String getArmorTexture(boolean overlay) {
        return !overlay ? IronJetpacks.MOD_ID + ":textures/armor/jetpack.png" : IronJetpacks.MOD_ID + ":textures/armor/jetpack_overlay.png";
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int getColorTint(int i) {
        return i == 1 ? this.jetpack.color : -1;
    }

    @Override
    public boolean hasColor(ItemStack stack) {
        return true;
    }

    @Override
    public int getColor(ItemStack stack) {
        return this.jetpack.color;
    }

    @Override
    public void removeColor(ItemStack stack) {

    }

    @Override
    public void setColor(ItemStack stack, int color) {

    }

    @Override
    public boolean isEnabled() {
        return !this.jetpack.disabled;
    }

    public Jetpack getJetpack() {
        return this.jetpack;
    }

    public EnergyHandler getEnergyStorage(ItemStack stack) {
        return Energy.of(stack);
    }

    // No output
    @Override
    public double getMaxOutput(EnergySide side) {
        return 0;
    }

    @Override
    public double getMaxInput(EnergySide side) {
        return jetpack.capacity / 20.0;
    }

    @Override
    public double getMaxStoredPower() {
        return jetpack.capacity;
    }

    @Override
    public EnergyTier getTier() {
        return EnergyTier.INFINITE;
    }

    public boolean isEngineOn(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        return tag != null && tag.contains("Engine") && tag.getBoolean("Engine");
    }

    public void toggleEngine(ItemStack stack) {
        NbtCompound tag = stack.getOrCreateNbt();
        boolean current = tag.contains("Engine") && tag.getBoolean("Engine");
        tag.putBoolean("Engine", !current);
        stack.setNbt(tag);
    }

    public boolean isHovering(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        return tag != null && tag.contains("Hover") && tag.getBoolean("Hover");
    }

    public void toggleHover(ItemStack stack) {
        NbtCompound tag = stack.getOrCreateNbt();
        boolean current = tag.contains("Hover") && tag.getBoolean("Hover");
        tag.putBoolean("Hover", !current);
        stack.setNbt(tag);
    }

    private void fly(PlayerEntity player, double y) {
        Vec3d motion = player.getVelocity();
        player.setVelocity(motion.getX(), y, motion.getZ());
    }
}
