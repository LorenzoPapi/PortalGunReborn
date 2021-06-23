package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.entity.Entity;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.world.ICollisionReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(ICollisionReader.class)
public interface ICollisionReaderMixin {

	/**
	 * @author LorenzoPapi
	 * @reason I cannot inject in interfaces because java bad
	 */
	@Overwrite(remap = false)
	default Stream<VoxelShape> getCollisionShapes(Entity entity, AxisAlignedBB aabb) {
		if (entity != null) {
			ReuseableStream<VoxelShape> stream = new ReuseableStream<>(StreamSupport.stream(new VoxelShapeSpliterator(entity.world, entity, aabb, (s, p) -> {
				// TODO: pair thingy, collision related to the direction (egh), save behinds in saved data
				return s.getBlock() != PGRRegistry.PORTAL_BLOCK && !Reference.serverEH.getWorldSaveData(entity.world.getDimensionKey()).behinds.contains(p);
			}), false));
			return stream.createStream();
		}
		return Stream.empty();
	}
}
