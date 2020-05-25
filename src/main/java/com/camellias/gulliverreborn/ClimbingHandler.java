package com.camellias.gulliverreborn;

import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ClimbingHandler {
    public static boolean canClimb(PlayerEntity player, Direction facing) {
        final World world = player.getEntityWorld();
        final BlockPos pos = new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ());
        final BlockState f = world.getBlockState(pos.add(0, 0, 0).offset(facing));
        final BlockState t = world.getBlockState(pos.add(0, 1, 0).offset(facing));
        final BlockState h = world.getBlockState(pos.add(0, 1, 0));
        final BlockState b = world.getBlockState(pos.add(0, 0, 0));
        final boolean fbpass = f.allowsMovement(world, pos.offset(facing), PathType.WATER);
        final boolean tbpass = t.allowsMovement(world, pos.add(0, 1, 0).offset(facing), PathType.WATER);
        final boolean hbpass = h.allowsMovement(world, pos.add(0, 1, 0), PathType.WATER);
        final boolean bbpass = b.allowsMovement(world, pos, PathType.WATER);

        if (bbpass) {
            if (!fbpass) {
                if ((!(tbpass || hbpass))) {
                    if ((t.getBlock() instanceof PaneBlock)) {

                    }
                    if ((h.getBlock() instanceof StairsBlock)) {
                        if (h.get(StairsBlock.FACING) == facing.getOpposite()) {
                            return true;
                        }
                    }
                    if ((h.getBlock() instanceof SlabBlock)) {
                        if (!h.isNormalCube(world, pos)) {
                            if (h.get(SlabBlock.TYPE) == SlabType.TOP) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
                return true;
            }
        }
        if ((b.getBlock() instanceof PaneBlock) && !(h.getBlock() instanceof PaneBlock)) {
            return true;
        }
        if (b.getBlock() instanceof StairsBlock) {
            if ((b.get(StairsBlock.FACING) == facing) && (b.get(StairsBlock.HALF) != Half.TOP)) {
                return true;
            }
        }
        if ((b.getBlock() instanceof SlabBlock)) {
            if (!h.isNormalCube(world, pos)) {
                if (b.get(SlabBlock.TYPE) == SlabType.BOTTOM) {
                    return true;
                }
            }
        }
        return false;
    }
}