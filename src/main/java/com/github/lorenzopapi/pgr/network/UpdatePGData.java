package com.github.lorenzopapi.pgr.network;

import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;

public class UpdatePGData implements IPacket<INetHandler> {
	public CompoundNBT newData;

	@Override
	public void readPacketData(PacketBuffer buf) {

	}

	@Override
	public void writePacketData(PacketBuffer buf) {

	}

	@Override
	public void processPacket(INetHandler handler) {

	}
}
