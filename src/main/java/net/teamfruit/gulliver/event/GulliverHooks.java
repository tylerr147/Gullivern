package net.teamfruit.gulliver.event; 
 
import net.minecraft.entity.Entity; 
import net.minecraft.entity.EntitySize; 
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraftforge.common.MinecraftForge;
 
public class GulliverHooks { 
    public static boolean fireMoveEvent(boolean isInvulnerableDimensionChange, ServerPlayNetHandler handler, ServerPlayerEntity player) {
        PlayNetMoveEvent event = new PlayNetMoveEvent(handler, player);
        boolean canceled = MinecraftForge.EVENT_BUS.post(event);
        return isInvulnerableDimensionChange || canceled;
    } 
}
