package com.tyler.resize.compatibilities;

import java.util.concurrent.Callable;

import com.tyler.resize.compatibilities.sizeCap.ISizeCap;
import com.tyler.resize.compatibilities.sizeCap.SizeCapStorage;
import com.tyler.resize.compatibilities.sizeCap.SizeDefaultCap;

import net.minecraftforge.common.capabilities.CapabilityManager;

public class Capabilities {
	public static void init()
	{
		CapabilityManager.INSTANCE.register(ISizeCap.class, new SizeCapStorage(), new CababilityFactory());
	}

	private static class CababilityFactory implements Callable<ISizeCap>
	{
		@Override
		public ISizeCap call() throws Exception
		{
			return new SizeDefaultCap();
		}
	}
}
