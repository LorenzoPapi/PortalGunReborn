package com.github.lorenzopapi.pgr.portal.block;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.VBOTracker;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class PortalBlockTileEntity extends TileEntity {
	public PortalBlockTileEntity() {
		super(PGRRegistry.PORTAL_TILE_ENTITY.get());
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (world == null) return AxisAlignedBB.fromVector(Vector3d.ZERO);
		PortalStructure portal = PGRUtils.findPortalByPosition(world, pos);
		if (portal == null || !portal.positions.get(0).equals(pos)) return AxisAlignedBB.fromVector(Vector3d.ZERO);
		Direction dir = getBlockState().get(PortalBlock.HORIZONTAL_FACING);
		//Don't ask why or how it works because the answer is: I don't even know
		return getBlockState().getShape(world, pos).getBoundingBox().offset(pos).expand((portal.width - 1) * Math.abs(dir.getZOffset()), portal.height - 1, (portal.width - 1) * Math.abs(dir.getXOffset()));
	}
	
	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if (world.isRemote)
			VBOTracker.untrack(this);
	}
}
