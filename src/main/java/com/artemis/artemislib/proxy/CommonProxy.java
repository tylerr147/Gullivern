package com.artemis.artemislib.proxy;

import com.artemis.artemislib.compatibilities.Capabilities;
import com.artemis.artemislib.compatibilities.CapabilitiesHandler;
import com.artemis.artemislib.attributes.AttributesHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonProxy {

    public void preInit(FMLCommonSetupEvent event) {
        Capabilities.init();
        MinecraftForge.EVENT_BUS.register(new CapabilitiesHandler());
        MinecraftForge.EVENT_BUS.register(new AttributesHandler());
    }
}
