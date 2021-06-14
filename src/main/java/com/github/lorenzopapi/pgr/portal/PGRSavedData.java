package com.github.lorenzopapi.pgr.portal;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class PGRSavedData extends WorldSavedData {
	public HashMap<String, ArrayList<ChannelInfo>> channelList;
	public List<PortalStructure> portals;
	public boolean initialized;

	public PGRSavedData(String name) {
		super(name);
		this.channelList = new HashMap<>();
		this.portals = new ArrayList<>();
	}

	public void initialize(World world) {
		if (this.initialized)
			return;
		this.initialized = true;
		Reference.LOGGER.info("Channels: " + channelList);
		Reference.LOGGER.info("Portals: " + portals.size());
		for (PortalStructure portal : portals) {
			portal.initialize(world);
			if (portal.pair == null) {
				PortalStructure possiblePair = findPair(portal);
				if (possiblePair != null) {
					portal.setPair(possiblePair);
					possiblePair.setPair(portal);
				}
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

	public PortalStructure findPortalByPosition(World world, BlockPos pos) {
		if (world.getBlockState(pos).getBlock() == PGRRegistry.PORTAL_BLOCK) {
			for (PortalStructure struct : portals) {
				List<BlockPos> positions = Arrays.asList(struct.positions);
				if (positions.contains(pos)) {
					return struct;
				}
			}
		}
		return null;
	}

	public PortalStructure findPortalOfSameType(PortalStructure toCheck) {
		for (PortalStructure struct : portals) {
			if (struct.isSameStruct(toCheck)) {
				return struct;
			}
		}
		return null;
	}

	public void removePortal(PortalStructure struct) {
		for (PortalStructure ps : portals) {
			if (ps.isSameStruct(struct)) {
				ChannelIndicator indicator = Reference.serverEH.getPortalChannelIndicator(ps.info.uuid, ps.info.channelName, ps.world.getDimensionKey());
				if (ps.isTypeA) {
					indicator.setPortalAPlaced(false);
				} else {
					indicator.setPortalBPlaced(false);
				}
				ps.removeStructure();
				if (ps.pair != null) {
					ps.pair.setPair(null);
					ps.setPair(null);
				}
				portals.remove(ps);
				break;
			}
		}
		markDirty();
	}

	public PortalStructure findPair(PortalStructure struct) {
		for (PortalStructure ps : portals) {
			if (ps.isPair(struct)) {
				return ps;
			}
		}
		return null;
	}

	@Override
	public void read(CompoundNBT nbt) {
		int portals = nbt.getInt("portals");
		for (int i = 0; i < portals; i++) {
			this.portals.add(new PortalStructure().readFromNBT(nbt.getCompound("portal_" + i), true));
		}
		int channels = nbt.getInt("channels");
		for (int i = 0; i < channels; i++) {
			ChannelInfo info = new ChannelInfo().readFromNBT(nbt.getCompound("channel_" + i));
			if (!info.uuid.equals("NULL")) {
				addChannel(info.uuid, info);
			}
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		int numPortals = 0;
		for (PortalStructure struct : portals) {
			nbt.put("portal_" + numPortals, struct.writeToNBT(new CompoundNBT()));
			numPortals++;
		}
		nbt.putInt("portals", numPortals);
		int channels = 0;
		for (Map.Entry<String, ArrayList<ChannelInfo>> e : this.channelList.entrySet()) {
			for (ChannelInfo channel : e.getValue()) {
				nbt.put("channel_" + channels, channel.writeToNBT(new CompoundNBT()));
				channels++;
			}
		}
		nbt.putInt("channels", channels);
		return nbt;
	}
}
