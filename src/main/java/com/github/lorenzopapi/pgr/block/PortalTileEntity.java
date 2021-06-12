package com.github.lorenzopapi.pgr.block;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class PortalTileEntity extends TileEntity {
	public PortalTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}
	
	public PortalTileEntity() {
		super(PGRRegistry.PORTAL_TILE_ENTITY.get());
	}
}
