package net.teamfruit.gulliver.attributes;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.teamfruit.gulliver.Gulliver;

public class Attributes {
    public static final Attribute ENTITY_HEIGHT = new RangedAttribute(Gulliver.MODID + ".entityHeight",
            1.0F, Float.MIN_VALUE, Float.MAX_VALUE).setShouldWatch(true);

    public static final Attribute ENTITY_WIDTH = new RangedAttribute(Gulliver.MODID + ".entityWidth",
            1.0F, Float.MIN_VALUE, Float.MAX_VALUE).setShouldWatch(true);
}
