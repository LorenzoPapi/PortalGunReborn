package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portalgun.PortalBlock;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public static Vector3d collideBoundingBox(Vector3d vec, AxisAlignedBB collisionBox, ReuseableStream<VoxelShape> potentialHits) {
        throw new IllegalStateException("Something's wrong");
    }

    @Shadow
    public static Vector3d getAllowedMovement(Vector3d vec, AxisAlignedBB collisionBox, IWorldReader worldIn, ISelectionContext selectionContext, ReuseableStream<VoxelShape> potentialHits) {
        throw new IllegalStateException("Something's wrong");
    }

    @Shadow public World world;

//    @Inject(method = "pushOutOfBlocks", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/vector/Vector3d;scale(D)Lnet/minecraft/util/math/vector/Vector3d;"), cancellable = true)
//    public void pushOut(double x, double y, double z, CallbackInfo ci) {
//        BlockPos pos = new BlockPos(x, y, z);
//        for (Direction d : Direction.Plane.HORIZONTAL) {
//            BlockState offState = world.getBlockState(pos.offset(d));
//            if (offState.hasProperty(PortalBlock.HORIZONTAL_FACING) && offState.get(PortalBlock.HORIZONTAL_FACING) == d) {
//                Reference.LOGGER.info("CANCELLING!!");
//                ci.cancel();
//                return;
//            }
//        }
//    }

    /**
     * @author LorenzoPapi
     */
//    @Overwrite
//    public static Vector3d collideBoundingBoxHeuristically(@Nullable Entity entity, Vector3d vec, AxisAlignedBB collisionBox, World world, ISelectionContext context, ReuseableStream<VoxelShape> potentialHits) {
//        boolean flag = vec.x == 0.0D;
//        boolean flag1 = vec.y == 0.0D;
//        boolean flag2 = vec.z == 0.0D;
//        if ((flag && flag1) || (flag && flag2) || (flag1 && flag2)) {
//            return getAllowedMovement(vec, collisionBox, world, context, new ReuseableStream<>(Stream.empty()));
//        } else {
//            ReuseableStream<VoxelShape> stream = new ReuseableStream<>(Stream.concat(potentialHits.createStream(), world.func_241457_a_(entity, collisionBox.expand(vec), (state, pos) -> {
//                for (Direction d : Direction.Plane.HORIZONTAL) {
//                    BlockState offState = world.getBlockState(pos.offset(d));
//                    if (offState.getBlock() == PGRRegistry.PORTAL_BLOCK && offState.get(PortalBlock.HORIZONTAL_FACING) == d) {
//                        return false;
//                    }
//                }
//                return state.getBlock() != PGRRegistry.PORTAL_BLOCK;
//            })));
//            return collideBoundingBox(vec, collisionBox, stream);
//        }
//    }
}
