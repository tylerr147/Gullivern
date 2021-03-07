package com.tyler.resize;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ForgeMod;
import com.tyler.resize.attributes.Attributes;

import java.util.UUID;

public class SizeManager {
    private static final UUID uuidHeight = UUID.fromString("5440b01a-974f-4495-bb9a-c7c87424bca4");
    private static final UUID uuidWidth = UUID.fromString("3949d2ed-b6cc-4330-9c13-98777f48ea51");
    private static final UUID uuidReach1 = UUID.fromString("854e0004-c218-406c-a9e2-590f1846d80b");
    private static final UUID uuidReach2 = UUID.fromString("216080dc-22d3-4eff-a730-190ec0210d5c");
    private static final UUID uuidHealth = UUID.fromString("3b901d47-2d30-495c-be45-f0091c0f6fb2");
    private static final UUID uuidStrength = UUID.fromString("558f55be-b277-4091-ae9b-056c7bc96e84");
    private static final UUID uuidSpeed = UUID.fromString("f2fb5cda-3fbe-4509-a0af-4fc994e6aeca");

    public static void changeSize(LivingEntity sender, float size) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        Multimap<Attribute, AttributeModifier> removeableAttributes = HashMultimap.create();
        Multimap<Attribute, AttributeModifier> removeableAttributes2 = HashMultimap.create();

        attributes.put(Attributes.ENTITY_HEIGHT.get(), new AttributeModifier(uuidHeight, "Player Height", MathHelper.clamp(size - 1, .01 - 1, ResizeConfig.GENERAL.MAX_SIZE.get()), AttributeModifier.Operation.MULTIPLY_TOTAL));
        attributes.put(Attributes.ENTITY_WIDTH.get(), new AttributeModifier(uuidWidth, "Player Width", MathHelper.clamp(size - 1, .01 - 1, ResizeConfig.GENERAL.MAX_SIZE.get()), AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (ResizeConfig.MODIFIER.SPEED_MODIFIER.get())
            attributes.put(net.minecraft.entity.ai.attributes.Attributes.MOVEMENT_SPEED, new AttributeModifier(uuidSpeed, "Player Speed", (size - 1) / 2, AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (ResizeConfig.MODIFIER.REACH_MODIFIER.get())
            removeableAttributes.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(uuidReach1, "Player Reach 1", size - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (ResizeConfig.MODIFIER.REACH_MODIFIER.get())
            removeableAttributes2.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(uuidReach2, "Player Reach 2", -MathHelper.clamp(size - 1, 0.33, Double.MAX_VALUE), AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (ResizeConfig.MODIFIER.STRENGTH_MODIFIER.get())
            attributes.put(net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE, new AttributeModifier(uuidStrength, "Player Strength", size - 1, AttributeModifier.Operation.ADDITION));
        if (ResizeConfig.MODIFIER.HEALTH_MODIFIER.get())
            attributes.put(net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH, new AttributeModifier(uuidHealth, "Player Health", (size - 1) * ResizeConfig.GENERAL.HEALTH_MULTIPLIER.get(), AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (size > 1) {
            reapplyPersistentModifiers(sender.getAttributeManager(), removeableAttributes);
        } else {
            sender.getAttributeManager().removeModifiers(removeableAttributes);
        }

        if (size < 1) {
            reapplyPersistentModifiers(sender.getAttributeManager(), removeableAttributes2);
        } else {
            sender.getAttributeManager().removeModifiers(removeableAttributes2);
        }

        reapplyPersistentModifiers(sender.getAttributeManager(), attributes);
        sender.setHealth(sender.getMaxHealth());
    }

    private static void copySizeElement(LivingEntity from, LivingEntity to, Attribute attribute, UUID uuid) {
        ModifiableAttributeInstance fromAttr = from.getAttribute(attribute);
        ModifiableAttributeInstance toAttr = to.getAttribute(attribute);
        if (fromAttr != null && toAttr != null) {
            AttributeModifier mod = fromAttr.getModifier(uuid);
            if (mod != null)
                toAttr.applyPersistentModifier(mod);
        }
    }

    public static void copySize(LivingEntity from, LivingEntity to) {
        from.getAttribute(Attributes.ENTITY_HEIGHT.get()).getModifier(uuidHeight);

        copySizeElement(from, to, Attributes.ENTITY_HEIGHT.get(), uuidHeight);
        copySizeElement(from, to, Attributes.ENTITY_WIDTH.get(), uuidWidth);
        if (ResizeConfig.MODIFIER.SPEED_MODIFIER.get())
            copySizeElement(from, to, net.minecraft.entity.ai.attributes.Attributes.MOVEMENT_SPEED, uuidSpeed);
        if (ResizeConfig.MODIFIER.REACH_MODIFIER.get()) {
            copySizeElement(from, to, ForgeMod.REACH_DISTANCE.get(), uuidReach1);
            copySizeElement(from, to, ForgeMod.REACH_DISTANCE.get(), uuidReach2);
        }
        if (ResizeConfig.MODIFIER.STRENGTH_MODIFIER.get())
            copySizeElement(from, to, net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE, uuidStrength);
        if (ResizeConfig.MODIFIER.HEALTH_MODIFIER.get())
            copySizeElement(from, to, net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH, uuidHealth);
    }

    private static void reapplyPersistentModifiers(AttributeModifierManager attrs, Multimap<Attribute, AttributeModifier> map) {
        map.forEach((attribute, modifiers) -> {
            ModifiableAttributeInstance modifiableattributeinstance = attrs.createInstanceIfAbsent(attribute);
            if (modifiableattributeinstance != null) {
                modifiableattributeinstance.removeModifier(modifiers);
                modifiableattributeinstance.applyPersistentModifier(modifiers);
            }
        });
    }
}
