package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portalgun.PortalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(ICollisionReader.class)
public interface ICollisionReaderMixin {

     default boolean isPortalNear(BlockPos pos, World world) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Direction d : new Direction[]{Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH}) {
            mutable.setAndMove(pos, d);
            BlockState state = world.getBlockState(mutable);
            if (state.getBlock() == PGRRegistry.PORTAL_BLOCK && state.get(PortalBlock.HORIZONTAL_FACING) == d) {
                System.out.println("Found!");
                return true;
            }
        }
        return false;
    }

    /**
     * @author LorenzoPapi
     * @reason I cannot inject in interfaces because java bad
     */
    @Overwrite(remap = false)
    default Stream<VoxelShape> getCollisionShapes(Entity entity, AxisAlignedBB aabb) {
        if (entity != null) {
            ReuseableStream<VoxelShape> stream = new ReuseableStream<>(StreamSupport.stream(new VoxelShapeSpliterator(entity.world, entity, aabb, (s, p) -> s.getBlock() != PGRRegistry.PORTAL_BLOCK && !isPortalNear(p, entity.world)), false));
            if (stream.createStream().count() != 0) {
                stream.createStream().forEach(System.out::print);
                System.out.println();
            }
            return stream.createStream();
        }
        return Stream.empty();
    }
}
