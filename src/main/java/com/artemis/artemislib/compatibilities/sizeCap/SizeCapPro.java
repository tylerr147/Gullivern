package com.artemis.artemislib.compatibilities.sizeCap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SizeCapPro implements ICapabilitySerializable<CompoundNBT> {

    private ISizeCap capabilitySize = null;

    public SizeCapPro() {
        this.capabilitySize = new SizeDefaultCap();
    }

    public SizeCapPro(ISizeCap capability) {
        this.capabilitySize = capability;
    }

    @CapabilityInject(ISizeCap.class)
    public static final Capability<ISizeCap> sizeCapability = null;

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (sizeCapability != null && capability == sizeCapability) {
            return LazyOptional.of(() -> (T) this.capabilitySize);
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return this.capabilitySize.saveNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.capabilitySize.loadNBT(nbt);
    }
}