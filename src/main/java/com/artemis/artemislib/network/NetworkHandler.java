package com.artemis.artemislib.network;

import com.camellias.gulliverreborn.GulliverReborn;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class NetworkHandler {

	public final static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(GulliverReborn.MODID.toLowerCase());

	private static int ID = 0;

	private static int nextId()
	{
		return ID++;
	}

	public static void init()
	{

	}

}
