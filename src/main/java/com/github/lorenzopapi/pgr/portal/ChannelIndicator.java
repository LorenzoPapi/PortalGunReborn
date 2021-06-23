package com.github.lorenzopapi.pgr.portal;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

public class ChannelIndicator {
	public final ChannelInfo info;

	public final RegistryKey<World> dimension;

	public boolean portalAPlaced;

	public boolean portalBPlaced;

	public ChannelIndicator(ChannelInfo info, RegistryKey<World> dimension) {
		this.info = info;
		this.dimension = dimension;
	}

	public ChannelIndicator setPortalAPlaced(boolean available) {
		this.portalAPlaced = available;
		return this;
	}

	public ChannelIndicator setPortalBPlaced(boolean available) {
		this.portalBPlaced = available;
		return this;
	}

	public boolean equals(Object o) {
		return (o instanceof ChannelIndicator && ((ChannelIndicator) o).info.equals(this.info) && ((ChannelIndicator) o).dimension == this.dimension);
	}
}

