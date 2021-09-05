package com.github.lorenzopapi.pgr.portal;

import com.github.lorenzopapi.pgr.portal.structure.ChannelIndicator;
import com.github.lorenzopapi.pgr.portal.structure.ChannelInfo;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PGRSavedData extends WorldSavedData {
	public final HashMap<String, ArrayList<ChannelInfo>> channelList;
	public final List<PortalStructure> portals;
	public boolean initialized;

	public PGRSavedData(String name) {
		super(name);
		this.channelList = new HashMap<>();
		this.portals = new ArrayList<>();
	}

	public void reset() {
		this.channelList.clear();
		this.portals.clear();
	}

	public void initialize(World world) {
		if (this.initialized)
			return;
		this.initialized = true;
		Reference.LOGGER.info("Channels: " + channelList);
		Reference.LOGGER.info("Portals: " + portals.size());
		for (PortalStructure portal : portals) {
			portal.initialize(world);
			if (!portal.hasPair()) {
				PortalStructure possiblePair = findPair(portal);
				if (possiblePair != null) {
					portal.setPair(possiblePair);
					possiblePair.setPair(portal);
				}
			}
			ChannelIndicator indicator = Reference.serverEH.getPortalChannelIndicator(portal.info.uuid, portal.info.channelName, world.getDimensionKey());
			if (portal.isTypeA) {
				indicator.setPortalAPlaced(true);
			} else {
				indicator.setPortalBPlaced(true);
			}
		}
	}

	//Adds channel and maps it to uuid
	public void addChannel(String uuid, ChannelInfo info) {
		ArrayList<ChannelInfo> channels = this.channelList.computeIfAbsent(uuid, k -> new ArrayList<>());
		if (!channels.contains(info))
			channels.add(info);
	}

	//Gets or creates channel based on UUID and Name
	public ChannelInfo getChannel(String uuid, String channelName) {
		ArrayList<ChannelInfo> channels = this.channelList.computeIfAbsent(uuid, k -> new ArrayList<>());
		for (ChannelInfo channel : channels) {
			if (channel.channelName.equals(channelName)) {
				return channel;
			}
		}
		return new ChannelInfo(uuid, channelName);
	}

	public void removePortal(PortalStructure struct) {
		ChannelIndicator indicator = Reference.serverEH.getPortalChannelIndicator(struct.info.uuid, struct.info.channelName, struct.world.getDimensionKey());
		if (struct.isTypeA) {
			indicator.setPortalAPlaced(false);
		} else {
			indicator.setPortalBPlaced(false);
		}
		struct.removeStructure();
		portals.remove(struct);
		markDirty();
	}

	public PortalStructure findPair(PortalStructure struct) {
		for (PortalStructure pair : portals) {
			if (pair.isPair(struct)) {
				return pair;
			}
		}
		return null;
	}

	@Override
	public void read(CompoundNBT nbt) {
		ListNBT portalsToRead = nbt.getList("portals", 10);
		for (int i = 0; i < portalsToRead.size(); i++) {
			this.portals.add(new PortalStructure().readFromNBT(portalsToRead.getCompound(i)));
		}
		ListNBT channelsToRead = nbt.getList("channels", 10);
		for (int i = 0; i < channelsToRead.size(); i++) {
			ChannelInfo info = new ChannelInfo().readFromNBT(channelsToRead.getCompound(i));
			if (!info.uuid.equals("NULL"))
				addChannel(info.uuid, info);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		ListNBT portalsToSave = new ListNBT();
		for (PortalStructure struct : portals) {
			portalsToSave.add(struct.writeToNBT(new CompoundNBT()));
		}
		nbt.put("portals", portalsToSave);
		ListNBT channelsToSave = new ListNBT();
		for (Map.Entry<String, ArrayList<ChannelInfo>> e : this.channelList.entrySet()) {
			for (ChannelInfo channel : e.getValue()) {
				channelsToSave.add(channel.writeToNBT(new CompoundNBT()));
			}
		}
		nbt.put("channels", channelsToSave);
		return nbt;
	}
}
