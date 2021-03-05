package net.teamfruit.gulliver.event; 
 
import net.minecraft.entity.Entity; 
import net.minecraft.entity.EntitySize; 
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraftforge.common.MinecraftForge;
 
public class GulliverHooks { 
    public static boolean fireMoveEvent(boolean isInvulnerableDimensionChange, ServerPlayerEntity player) {
        PlayNetMoveEvent event = new PlayNetMoveEvent(player);
        boolean canceled = MinecraftForge.EVENT_BUS.post(event);
        return isInvulnerableDimensionChange || canceled;
    }

    public static EntitySize fireEntityGetSizeEvent(EntitySize size, Entity entity, Pose pose) {
        EntitySizeEvent event = new EntitySizeEvent(entity, pose, size);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getNewSize();
    }

    public static EntitySize firePlayerGetSizeEvent(EntitySize size, Entity entity, Pose pose) {
        EntitySizeEvent event = new EntitySizeEvent(entity, pose, size);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getNewSize();
    }
}
