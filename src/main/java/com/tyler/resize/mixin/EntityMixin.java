package com.tyler.resize.mixin;

import com.tyler.resize.event.EntitySizeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getSize", at = @At("RETURN"), cancellable = true)
    private void getSize(Pose poseIn, CallbackInfoReturnable<EntitySize> cir) {
        EntitySizeEvent event = new EntitySizeEvent((Entity) (Object) this, poseIn, cir.getReturnValue());
        MinecraftForge.EVENT_BUS.post(event);
        cir.setReturnValue(event.getNewSize());
    }
}
