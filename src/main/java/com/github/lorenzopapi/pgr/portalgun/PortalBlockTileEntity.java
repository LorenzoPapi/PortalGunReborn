package com.github.lorenzopapi.pgr.portalgun;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.VBOTracker;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;

public class PortalBlockTileEntity extends TileEntity {
	public PortalBlockTileEntity() {
		super(PGRRegistry.PORTAL_TILE_ENTITY.get());
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (world == null) return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		PortalStructure structure = PGRUtils.findPortalByPosition(world, pos);
		if (structure == null) return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		if  (!structure.positions.get(0).equals(pos)) return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		VoxelShape shape = getBlockState().getShape(world, pos);
		AxisAlignedBB boundingBox = shape.getBoundingBox().offset(pos);
		for (BlockPos position : structure.positions) {
			// TODO: do this fully
			boundingBox = new AxisAlignedBB(
					boundingBox.minX, Math.min(position.getY(), boundingBox.minY), boundingBox.minZ,
					boundingBox.maxX, Math.max(position.getY() + 1, boundingBox.maxY), boundingBox.maxZ
			);
		}
		return boundingBox;
	}
	
	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if (world.isRemote)
			VBOTracker.untrack(this);
	}
}
