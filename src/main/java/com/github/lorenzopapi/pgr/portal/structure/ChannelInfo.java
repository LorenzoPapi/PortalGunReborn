package com.github.lorenzopapi.pgr.portal.structure;

import net.minecraft.nbt.CompoundNBT;

public class ChannelInfo {
	public String uuid;
	public String channelName;
	public int colorA;
	public int colorB;

	public ChannelInfo() {
		this("NULL", "NULL");
	}

	public ChannelInfo(String uuid, String channelName) {
		this.uuid = uuid;
		this.channelName = channelName;
		this.colorA = -1;
		this.colorB = -1;
	}

	public ChannelInfo setColor(int a, int b) {
		this.colorA = a;
		this.colorB = b;
		return this;
	}

	public boolean equals(Object o) {
		return (o instanceof ChannelInfo && ((ChannelInfo) o).uuid.equals(this.uuid) && ((ChannelInfo) o).channelName.equals(this.channelName));
	}

	public ChannelInfo readFromNBT(CompoundNBT tag) {
		this.uuid = tag.getString("uuid");
		this.channelName = tag.getString("channelName");
		this.colorA = tag.getInt("colorA");
		this.colorB = tag.getInt("colorB");
		return this;
	}

	public CompoundNBT writeToNBT(CompoundNBT tag) {
		tag.putString("uuid", this.uuid);
		tag.putString("channelName", this.channelName);
		tag.putInt("colorA", this.colorA);
		tag.putInt("colorB", this.colorB);
		return tag;
	}

	@Override
	public String toString() {
		return "ChannelInfo{" +
				       "uuid='" + uuid + '\'' +
				       ", channelName='" + channelName + '\'' +
				       ", colorA=" + colorA +
				       ", colorB=" + colorB +
				       '}';
	}


}
