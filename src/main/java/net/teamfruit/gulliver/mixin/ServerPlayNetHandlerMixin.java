package net.teamfruit.gulliver.mixin;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.teamfruit.gulliver.event.GulliverHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetHandler.class)
public class ServerPlayNetHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Redirect(method = "processPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;isInvulnerableDimensionChange()Z"))
    private boolean isInvulnerableDimensionChange(ServerPlayerEntity entity) {
        return GulliverHooks.fireMoveEvent(entity.isInvulnerableDimensionChange(), player);
    }
}
