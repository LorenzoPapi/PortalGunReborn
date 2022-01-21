package com.github.lorenzopapi.pgr.network;

import com.github.lorenzopapi.pgr.portal.structure.ChannelInfo;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SUpdateChannelColor implements IPacket<INetHandler> {
	public ChannelInfo info;
	public int newColorA;
	public int newColorB;

	public SUpdateChannelColor(PacketBuffer buffer) {
		readPacketData(buffer);
	}

	public SUpdateChannelColor(ChannelInfo info, int newColorA, int newColorB) {
		this.info = info;
		this.newColorA = newColorA;
		this.newColorB = newColorB;
	}

	@Override
	public void readPacketData(PacketBuffer buf) {
		info = new ChannelInfo().readFromNBT(buf.readCompoundTag());
		newColorA = buf.readInt();
		newColorB = buf.readInt();
	}

	@Override
	public void writePacketData(PacketBuffer buf) {
		CompoundNBT nbt = new CompoundNBT();
		info.writeToNBT(nbt);
		buf.writeCompoundTag(nbt);
		buf.writeInt(newColorA);
		buf.writeInt(newColorB);
	}

	@Override
	public void processPacket(INetHandler handler) {}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		PortalStructure struct = PGRUtils.findPortalsByInfo(ctx.get().getSender().world, info);
		if (struct != null) {
			struct.info.colorA = newColorA;
			struct.info.colorB = newColorB;
			struct.setColor(struct.isTypeA ? newColorA : newColorB);
			if (struct.pair != null)
				struct.pair.setColor(struct.pair.isTypeA ? newColorA : newColorB);
		}
		Reference.serverEH.getPGRDataForWorld(ctx.get().getSender().world).markDirty();
	}
}
