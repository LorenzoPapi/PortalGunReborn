package com.github.lorenzopapi.pgr.portalgun;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.PortalStructure;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class PortalBlockTileEntity extends TileEntity {
	public PortalBlockTileEntity() {
		super(PGRRegistry.PORTAL_TILE_ENTITY.get());
	}
}
