package net.teamfruit.gulliver.attributes;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.teamfruit.gulliver.Gulliver;

public class Attributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Attribute.class, Gulliver.MODID);

    public static final RegistryObject<Attribute> ENTITY_HEIGHT = ATTRIBUTES.register("entity_height",
            () -> new RangedAttribute(Gulliver.MODID + ".entityHeight",
                    1.0F, Float.MIN_VALUE, Float.MAX_VALUE).setShouldWatch(true));

    public static final RegistryObject<Attribute> ENTITY_WIDTH = ATTRIBUTES.register("entity_width",
            () -> new RangedAttribute(Gulliver.MODID + ".entityWidth",
                    1.0F, Float.MIN_VALUE, Float.MAX_VALUE).setShouldWatch(true));
}
