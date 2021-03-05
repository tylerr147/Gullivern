package net.teamfruit.gulliver.mixin;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.teamfruit.gulliver.event.GulliverHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "getSize", at = @At("RETURN"), cancellable = true)
    private void getSize(Pose poseIn, CallbackInfoReturnable<EntitySize> cir) {
        cir.setReturnValue(GulliverHooks.firePlayerGetSizeEvent(cir.getReturnValue(), (PlayerEntity) (Object) this, poseIn));
    }
}
