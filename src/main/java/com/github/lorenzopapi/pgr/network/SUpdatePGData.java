package com.github.lorenzopapi.pgr.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SUpdatePGData implements IPacket<INetHandler> {
	public ItemStack oldStack;
	public int newWidth;
	public int newHeight;

	public SUpdatePGData(PacketBuffer buffer) {
		readPacketData(buffer);
	}

	public SUpdatePGData(ItemStack oldStack, int newWidth, int newHeight) {
		this.oldStack = oldStack;
		this.newWidth = newWidth;
		this.newHeight = newHeight;
	}

	@Override
	public void readPacketData(PacketBuffer buf) {
		oldStack = buf.readItemStack();
		newWidth = buf.readInt();
		newHeight = buf.readInt();
	}

	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeItemStack(oldStack);
		buf.writeInt(newWidth);
		buf.writeInt(newHeight);
	}

	@Override
	public void processPacket(INetHandler handler) {}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		oldStack.getTag().putInt("width", newWidth);
		oldStack.getTag().putInt("height", newHeight);
		ctx.get().getSender().openContainer.getSlot(0).putStack(oldStack);
	}
}
