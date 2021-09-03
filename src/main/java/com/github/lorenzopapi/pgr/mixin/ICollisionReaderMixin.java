package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.handler.CollisionReaderMixinHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.ICollisionReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.stream.Stream;

@Mixin(ICollisionReader.class)
public interface ICollisionReaderMixin {

	/**
	 * @author LorenzoPapi
	 * @reason I cannot inject in interfaces because java bad
	 */
	@Overwrite(remap = false)
//	@Overwrite
	default Stream<VoxelShape> getCollisionShapes(Entity entity, AxisAlignedBB aabb) {
		return CollisionReaderMixinHandler.getShapes(entity, aabb, (ICollisionReader)(Object)this);
	}
}
