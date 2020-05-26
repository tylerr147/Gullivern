package net.teamfruit.gulliver.compatibilities.sizeCap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class SizeCapStorage implements IStorage<ISizeCap> {

    public static final SizeCapStorage storage = new SizeCapStorage();

    @Override
    public INBT writeNBT(Capability<ISizeCap> capability, ISizeCap instance, Direction side) {
        final CompoundNBT tag = new CompoundNBT();
        tag.putBoolean("transformed", instance.getTrans());
        return tag;
    }

    @Override
    public void readNBT(Capability<ISizeCap> capability, ISizeCap instance, Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            final CompoundNBT tag = (CompoundNBT) nbt;
            if (tag.contains("transformed")) {
                instance.setTrans(tag.getBoolean("transformed"));
            }
        }
    }

}
