package com.github.lorenzopapi.pgr.network.message;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.util.PortalGunHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class KeyEventMessage implements IMessage<KeyEventMessage> {

	public int event;

	public KeyEventMessage() {}

	public KeyEventMessage(int e) {
		this.event = e;
	}

	@Override
	public void encode(KeyEventMessage message, PacketBuffer buffer) {
		buffer.writeInt(message.event);
	}

	@Override
	public KeyEventMessage decode(PacketBuffer buffer) {
		return new KeyEventMessage(buffer.readInt());
	}

	@Override
	public void handle(KeyEventMessage message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() -> {
			ServerPlayerEntity player = supplier.get().getSender();
			ItemStack is = player.getHeldItemMainhand();
			boolean isHoldingPortalGun = (is.getItem() == PGRRegistry.PORTAL_GUN.get());
			switch (message.event) {
				case 0:
				case 1:
				case 2:
//					if (isHoldingPortalGun) {
//						CompoundNBT tag = is.getTag();
//						if (tag == null) {
//							PortalGunItem.setRandomNBTTags(is, player);
//							tag = is.getTag();
//						}
//						if (tag != null) {
//							ArrayList<PortalInstance> infos = Reference.pgrEventHandler.getWorldSaveData(player.getServerWorld().getDimensionKey()).portalList;
//							ArrayList<PortalInstance> infoToRemove = new ArrayList<>();
//							for (PortalInstance info : infos) {
//								if (info.uuid.equals(tag.getString("uuid")) && info.channelName.equals(tag.getString("channelName")) && (message.event == 0 || (message.event == 1 && info.isTypeA) || (message.event == 2 && !info.isTypeA)))
//									infoToRemove.add(info);
//							}
//							for (PortalInstance info : infoToRemove) {
//								PortalPlacement placement = info.getPortalPlacement(player.world);
//								if (placement != null)
//									placement.remove(BlockPos.ZERO);
//							}
//							EntityHelper.playSoundAtEntity(player, PGRSoundEvents.pg_wpn_portal_fizzler_shimmy, player.getSoundCategory(), 0.2F, 1.0F);
//						}
//					}
					break;
				case 3:
				case 4:
					if (isHoldingPortalGun) {
//						if (GrabHandler.hasHandlerType((EntityLivingBase)player, Side.SERVER, PortalGunGrabHandler.class)) {
//							PortalGunHelper.tryGrab((EntityLivingBase)player);
//							break;
//						}
						PortalGunHelper.shootPortal(player, is, (message.event == 3));
					}
					break;
				case 5:
//					if (!PortalGunHelper.tryGrab((EntityLivingBase)player) && isHoldingPortalGun)
//						EntityHelper.playSoundAtEntity((Entity)player, SoundIndex.pg_object_use_failure, player.getSoundCategory(), 0.2F, 1.0F);
//					break;
			}
		});
		supplier.get().setPacketHandled(true);
	}
}
