package com.github.lorenzopapi.pgr.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.world.ICollisionReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(ICollisionReader.class)
public interface ICollisionReaderMixin {
	/**
	 * @author LorenzoPapi
	 * @reason E
	 */
	@Overwrite(remap = false)
	default boolean placedBlockCollides(BlockState state, BlockPos pos, ISelectionContext context) {
		return false;
	}

	/**
	 * @author LorenzoPapi
	 * @reason E
	 */
	@Overwrite(remap = false)
	default boolean hasNoCollisions(Entity entity, AxisAlignedBB aabb, Predicate<Entity> entityPredicate) {
		return false;
	}

	/**
	 * @author LorenzoPapi
	 * @reason E
	 */
	@Overwrite(remap = false)
	default Stream<VoxelShape> func_241457_a_(Entity entity, AxisAlignedBB aabb, BiPredicate<BlockState, BlockPos> statePosPredicate) {
		return Stream.<VoxelShape>builder().build();
	}

	/**
	 * @author LorenzoPapi
	 */
	@Overwrite(remap = false)
	default Stream<VoxelShape> getCollisionShapes(Entity entity, AxisAlignedBB aabb) {
		return Stream.<VoxelShape>builder().build();
	}

	/**
	 * @author LorenzoPapi
	 * @reason E
	 */
	@Overwrite(remap=false)
	default Stream<VoxelShape> func_234867_d_(Entity entity, AxisAlignedBB aabb, Predicate<Entity> entityPredicate) {
		return Stream.<VoxelShape>builder().build();
	}
}
