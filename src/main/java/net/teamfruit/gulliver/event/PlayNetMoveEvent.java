package net.teamfruit.gulliver.event; 
 
import net.minecraft.entity.Entity; 
import net.minecraft.entity.EntitySize; 
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PlayNetMoveEvent extends Event {
    private final ServerPlayNetHandler handler;
    private final ServerPlayerEntity player;

    public PlayNetMoveEvent(ServerPlayNetHandler handler, ServerPlayerEntity player) {
        this.handler = handler;
        this.player = player;
    } 
 
    public ServerPlayNetHandler getHandler() {
        return handler;
    } 
 
    public ServerPlayerEntity getPlayer() {
        return player;
    } 
}
