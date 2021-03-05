package net.teamfruit.gulliver.event;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PlayNetMoveEvent extends Event {
    private final ServerPlayerEntity player;

    public PlayNetMoveEvent(ServerPlayerEntity player) {
        this.player = player;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }
}
