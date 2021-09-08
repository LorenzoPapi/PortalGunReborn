package com.github.lorenzopapi.pgr.network;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.PGRSavedData;
import com.github.lorenzopapi.pgr.portal.gun.PGItem;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SKeyEventPacket implements IPacket<INetHandler> {

	public int event;

	public SKeyEventPacket(PacketBuffer buffer) {
		readPacketData(buffer);
	}

	public SKeyEventPacket(int e) {
		this.event = e;
	}

	@Override
	public void readPacketData(PacketBuffer buf) {
		event = buf.readInt();
	}

	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeInt(event);
	}

	@Override
	public void processPacket(INetHandler handler) {}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ItemStack is = player.getHeldItemMainhand();
		boolean isHoldingPortalGun = (is.getItem() == PGRRegistry.PORTAL_GUN.get());
		switch (event) {
			case 0:
			case 1:
			case 2:
				if (isHoldingPortalGun) {
					CompoundNBT tag = is.getTag();
					if (tag == null) {
						PGItem.setRandomNBTTags(is, player);
						tag = is.getTag();
					}
					if (tag != null) {
						PGRSavedData data = Reference.serverEH.getPGRDataForDimension(player.getServerWorld().getDimensionKey());
						List<PortalStructure> toRemove = new ArrayList<>();
						for (PortalStructure struct : data.portals) {
							if (struct.info.uuid.equals(tag.getString("uuid")) && struct.info.channelName.equals(tag.getString("channelName")) && (event == 0 || (event == 1 && struct.isTypeA) || (event == 2 && !struct.isTypeA)))
								toRemove.add(struct);
						}
						for (PortalStructure struct : toRemove) {
							data.removePortal(struct);
						}
						toRemove.clear();
						player.playSound(PGRRegistry.PGRSounds.PORTAL_GUN_RESET_PORTALS, player.getSoundCategory(), 0.2F, 1.0F);
					}
				}
				break;
			case 3:
			case 4:
				if (isHoldingPortalGun) {
					PGRUtils.shootPortal(player, is, (event == 3));
				}
				break;
		}
	}

}
