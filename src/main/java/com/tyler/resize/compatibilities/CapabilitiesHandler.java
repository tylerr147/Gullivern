package com.tyler.resize.compatibilities;

import com.tyler.resize.Resize;
import com.tyler.resize.compatibilities.sizeCap.ISizeCap;
import com.tyler.resize.compatibilities.sizeCap.SizeCapPro;
import com.tyler.resize.compatibilities.sizeCap.SizeDefaultCap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilitiesHandler {

    @SubscribeEvent
    public void onAddCapabilites(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity && !event.getObject().getCapability(SizeCapPro.sizeCapability).isPresent()) {
            final LivingEntity entity = (LivingEntity) event.getObject();
            final boolean transformed = false;
            final float defaultWidth = entity.getWidth();
            final float defaultHeight = entity.getHeight();
            final ISizeCap cap = new SizeDefaultCap(transformed, defaultWidth, defaultHeight);
            event.addCapability(new ResourceLocation(Resize.MODID, "capability"), new SizeCapPro(cap));
        }
    }
}
