package com.artemis.artemislib.util.attributes;

import com.camellias.gulliverreborn.GulliverReborn;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

public class ArtemisLibAttributes {
    public static final IAttribute ENTITY_HEIGHT = new RangedAttribute((IAttribute) null, GulliverReborn.MODID + ".entityHeight",
            1.0F, Float.MIN_VALUE, Float.MAX_VALUE).setDescription("Entity Height").setShouldWatch(true);

    public static final IAttribute ENTITY_WIDTH = new RangedAttribute((IAttribute) null, GulliverReborn.MODID + ".entityWidth",
            1.0F, Float.MIN_VALUE, Float.MAX_VALUE).setDescription("Entity Width").setShouldWatch(true);
}
