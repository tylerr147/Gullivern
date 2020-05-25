package com.artemis.artemislib.compatibilities.sizeCap;

import net.minecraft.nbt.CompoundNBT;

public interface ISizeCap {

	boolean getTrans();

	void setTrans(boolean transformed);

	float getDefaultWidth();

	void setDefaultWidth(float width);

	float getDefaultHeight();

	void setDefaultHeight(float height);

	CompoundNBT saveNBT();

	void loadNBT(CompoundNBT compound);

}
