package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.ICollisionReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(ICollisionReader.class)
public interface ICollisionReaderMixin {

//	/**
//	 * @author LorenzoPapi
//	 */
//	@Overwrite(remap = false)
//	default Stream<VoxelShape> getCollisionShapes(Entity entity, AxisAlignedBB aabb) {
//		Reference.LOGGER.info("{}, {}", entity, aabb);
//		return Stream.<VoxelShape>builder().build();
//	}
//
//	/**
//	 * @author LorenzoPapi
//	 * @reason E
//	 */
//	@Overwrite(remap=false)
//	default Stream<VoxelShape> func_234867_d_(Entity entity, AxisAlignedBB aabb, Predicate<Entity> entityPredicate) {
//		Reference.LOGGER.info(entityPredicate.test(entity));
//		return Stream.<VoxelShape>builder().build();
//	}
}
