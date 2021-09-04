package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.portal.PGRSavedData;
import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.portalgun.PortalBlockTileEntity;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CollisionReaderMixinHandler {
	public static Stream<VoxelShape> getShapes(Entity entity, AxisAlignedBB aabb, ICollisionReader iCollisionReader) {
		return StreamSupport.stream(new VoxelShapeSpliterator(iCollisionReader, entity, aabb), false);
//		if (entity != null && entity.world.isRemote) {
////			AtomicReference<HashSet<BlockPos>> behinds = new AtomicReference<>(new HashSet<>());
//			PGRSavedData data = Reference.serverEH.getWorldSaveData(entity.world);
//			final HashSet<BlockPos> behind = new HashSet<>();
//			StreamSupport.stream(new VoxelShapeSpliterator(entity.world, entity, aabb, (s, p) -> {
//				PortalStructure struct = PGRUtils.findPortalByPosition(entity.world, p.toImmutable());
//				if (struct != null && struct.hasPair()) {
//					behind.addAll(struct.behinds);
//					data.behinds.putIfAbsent(struct, behind);
//				}
//				return s.getBlock() != Blocks.AIR;
//			}), false).forEach(VoxelShape::getBoundingBox);
//			return StreamSupport.stream(new VoxelShapeSpliterator(entity.world, entity, aabb, (s, p) -> s.getBlock() != PGRRegistry.PORTAL_BLOCK && !behind.contains(p.toImmutable())), false);
//		}
//		return Stream.empty();
	}
	
	public static Stream<VoxelShape> filterShapes(Stream<VoxelShape> shapeStream, Entity entity, AxisAlignedBB box) {
		if (entity == null) return shapeStream;
		List<PortalStructure> intersecting = PGRUtils.findPortalsInAABB(entity.getEntityWorld(), box);
		HashSet<VoxelShape> shapes = new HashSet<>();
		shapeStream.forEach((shape)->{
			loopPortals:
			for (PortalStructure structure : intersecting) {
				TileEntity te = entity.getEntityWorld().getTileEntity(structure.positions.get(0));
				if (te instanceof PortalBlockTileEntity) {
					AxisAlignedBB boundingBox = te.getRenderBoundingBox();
//					boundingBox = boundingBox.offset(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
//					boundingBox = boundingBox.offset(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
					boundingBox = boundingBox.expand(-structure.direction.getXOffset() * 2, -structure.direction.getYOffset() * 2, -structure.direction.getZOffset() * 2);
					boundingBox = boundingBox.expand(structure.direction.getXOffset() * 0.5, structure.direction.getYOffset() * 0.5, structure.direction.getZOffset() * 0.5);
					for (AxisAlignedBB axisAlignedBB : shape.toBoundingBoxList()) {
						if (boundingBox.intersects(axisAlignedBB) || !entity.getEntityWorld().isRemote) {
							shape = VoxelShapes.combine(
									shape,
									VoxelShapes.create(boundingBox),
									IBooleanFunction.ONLY_FIRST
							);
							continue loopPortals;
						}
					}
				}
			}
			if (shape.isEmpty()) return;
			shapes.add(shape);
		});
		return Stream.of(shapes.toArray(new VoxelShape[0]));
	}
}
