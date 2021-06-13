package com.github.lorenzopapi.pgr.portalgun;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class PortalBlockTileEntity extends TileEntity {
	public PortalBlockTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}
	
	public PortalBlockTileEntity() {
		super(PGRRegistry.PORTAL_TILE_ENTITY.get());
	}
}
