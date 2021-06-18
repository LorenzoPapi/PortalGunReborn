package com.github.lorenzopapi.pgr.portalgun;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import net.minecraft.tileentity.TileEntity;

public class PortalBlockTileEntity extends TileEntity {
	public PortalBlockTileEntity() {
		super(PGRRegistry.PORTAL_TILE_ENTITY.get());
	}
}
