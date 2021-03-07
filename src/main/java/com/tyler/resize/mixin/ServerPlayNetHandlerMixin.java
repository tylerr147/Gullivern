package com.tyler.resize.mixin;

import com.tyler.resize.event.PlayNetMoveEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraftforge.common.MinecraftForge;
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
        PlayNetMoveEvent event = new PlayNetMoveEvent(player);
        boolean canceled = MinecraftForge.EVENT_BUS.post(event);
        return entity.isInvulnerableDimensionChange() || canceled;
    }
}
