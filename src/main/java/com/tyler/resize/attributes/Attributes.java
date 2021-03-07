package com.tyler.resize.attributes;

import com.tyler.resize.Resize;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class Attributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Attribute.class, Resize.MODID);

    public static final RegistryObject<Attribute> ENTITY_HEIGHT = ATTRIBUTES.register("entity_height",
            () -> new RangedAttribute(Resize.MODID + ".entityHeight",
                    1.0F, Float.MIN_VALUE, Float.MAX_VALUE).setShouldWatch(true));

    public static final RegistryObject<Attribute> ENTITY_WIDTH = ATTRIBUTES.register("entity_width",
            () -> new RangedAttribute(Resize.MODID + ".entityWidth",
                    1.0F, Float.MIN_VALUE, Float.MAX_VALUE).setShouldWatch(true));
}
