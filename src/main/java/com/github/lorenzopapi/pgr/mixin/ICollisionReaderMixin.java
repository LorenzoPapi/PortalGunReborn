package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.PGRSavedData;
import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.world.ICollisionReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(ICollisionReader.class)
public interface ICollisionReaderMixin {

	/**
	 * @author LorenzoPapi
	 * @reason I cannot inject in interfaces because java bad
	 */
//	@Overwrite(remap = false)
//	default Stream<VoxelShape> getCollisionShapes(Entity entity, AxisAlignedBB aabb) {
//		if (entity != null && entity.world.isRemote) {
//			AtomicReference<HashSet<BlockPos>> behinds = new AtomicReference<>(new HashSet<>());
//			PGRSavedData data = Reference.serverEH.getWorldSaveData(entity.world);
//			StreamSupport.stream(new VoxelShapeSpliterator(entity.world, entity, aabb, (s, p) -> {
//				PortalStructure struct = PGRUtils.findPortalByPosition(entity.world, p.toImmutable());
//				if (struct != null && struct.hasPair()) {
//					behinds.get().addAll(struct.behinds);
//					data.behinds.putIfAbsent(struct, behinds.get());
//				}
//				return s.getBlock() != Blocks.AIR;
//			}), false).forEach(VoxelShape::getBoundingBox);
//			return StreamSupport.stream(new VoxelShapeSpliterator(entity.world, entity, aabb, (s, p) -> s.getBlock() != PGRRegistry.PORTAL_BLOCK && !behinds.get().contains(p.toImmutable())), false);
//		}
//		return Stream.empty();
//	}
}
