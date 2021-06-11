package com.github.lorenzopapi.pgr.portal;

import net.minecraft.nbt.CompoundNBT;

public class ChannelInfo {
	public String uuid;
	public String channelName;
	public int colourA;
	public int colourB;

	public ChannelInfo() {
		this("NULL", "NULL");
	}

	public ChannelInfo(String uuid, String channelName) {
		this.uuid = uuid;
		this.channelName = channelName;
		this.colourA = -1;
		this.colourB = -1;
	}

	public ChannelInfo setColour(int a, int b) {
		this.colourA = a;
		this.colourB = b;
		return this;
	}

	public boolean equals(Object o) {
		return (o instanceof ChannelInfo && ((ChannelInfo)o).uuid.equals(this.uuid) && ((ChannelInfo)o).channelName.equals(this.channelName));
	}

	public ChannelInfo readFromNBT(CompoundNBT tag) {
		this.uuid = tag.getString("uuid");
		this.channelName = tag.getString("channelName");
		this.colourA = tag.getInt("colourA");
		this.colourB = tag.getInt("colourB");
		return this;
	}

	public CompoundNBT writeToNBT(CompoundNBT tag) {
		tag.putString("uuid", this.uuid);
		tag.putString("channelName", this.channelName);
		tag.putInt("colourA", this.colourA);
		tag.putInt("colourB", this.colourB);
		return tag;
	}
}
