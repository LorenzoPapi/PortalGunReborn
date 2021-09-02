package com.github.lorenzopapi.pgr.handler;

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

import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CollisionReaderMixinHandler {
	public static Stream<VoxelShape> getShapes(Entity entity, AxisAlignedBB aabb, ICollisionReader iCollisionReader) {
		if (entity != null && entity.world.isRemote) {
//			AtomicReference<HashSet<BlockPos>> behinds = new AtomicReference<>(new HashSet<>());
			PGRSavedData data = Reference.serverEH.getWorldSaveData(entity.world);
			final HashSet<BlockPos> behind = new HashSet<>();
			StreamSupport.stream(new VoxelShapeSpliterator(entity.world, entity, aabb, (s, p) -> {
				PortalStructure struct = PGRUtils.findPortalByPosition(entity.world, p.toImmutable());
				if (struct != null && struct.hasPair()) {
					behind.addAll(struct.behinds);
					data.behinds.putIfAbsent(struct, behind);
				}
				return s.getBlock() != Blocks.AIR;
			}), false).forEach(VoxelShape::getBoundingBox);
			return StreamSupport.stream(new VoxelShapeSpliterator(entity.world, entity, aabb, (s, p) -> s.getBlock() != PGRRegistry.PORTAL_BLOCK && !behind.contains(p.toImmutable())), false);
		}
		return Stream.empty();
	}
}
