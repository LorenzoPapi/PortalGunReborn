package com.github.lorenzopapi.pgr.portal;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
/**
 * Class that defines a ChannelIndicator.
 * It's a structure that contains a ChannelInfo, the dimension it's in and the portals available
 * So for every dimension there is a ChannelIndicator (?)
 * */
public class ChannelIndicator implements Comparable<ChannelIndicator> {
	public final ChannelInfo info;

	public final RegistryKey<World> dimension;

	public boolean portalAAvailable;

	public boolean portalBAvailable;

	public ChannelIndicator(ChannelInfo info, RegistryKey<World> dimension) {
		this.info = info;
		this.dimension = dimension;
	}

	public ChannelIndicator setPortalAStatus(boolean available) {
		this.portalAAvailable = available;
		return this;
	}

	public ChannelIndicator setPortalBStatus(boolean available) {
		this.portalBAvailable = available;
		return this;
	}

	public boolean equals(Object o) {
		return (o instanceof ChannelIndicator && ((ChannelIndicator)o).info.equals(this.info) && ((ChannelIndicator)o).dimension == this.dimension);
	}

	public int compareTo(ChannelIndicator o) {
		if (this.info.uuid.equals(o.info.uuid))
			return this.info.channelName.compareTo(o.info.channelName);
		return 0;
	}
}

