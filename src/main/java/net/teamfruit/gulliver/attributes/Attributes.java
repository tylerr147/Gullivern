package net.teamfruit.gulliver.attributes;

import net.teamfruit.gulliver.Gulliver;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

public class Attributes {
    public static final IAttribute ENTITY_HEIGHT = new RangedAttribute((IAttribute) null, Gulliver.MODID + ".entityHeight",
            1.0F, Float.MIN_VALUE, Float.MAX_VALUE).setDescription("Entity Height").setShouldWatch(true);

    public static final IAttribute ENTITY_WIDTH = new RangedAttribute((IAttribute) null, Gulliver.MODID + ".entityWidth",
            1.0F, Float.MIN_VALUE, Float.MAX_VALUE).setDescription("Entity Width").setShouldWatch(true);
}
