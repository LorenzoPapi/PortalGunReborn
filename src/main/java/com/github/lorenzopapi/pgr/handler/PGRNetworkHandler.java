package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.network.SKeyEventPacket;
import com.github.lorenzopapi.pgr.network.SUpdatePGData;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PGRNetworkHandler {
	public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(Reference.MOD_ID, "main"), () -> "1", "1"::equals, "1"::equals);

	public static void register() {
		HANDLER.registerMessage(0, SKeyEventPacket.class,
				SKeyEventPacket::writePacketData,
				SKeyEventPacket::new,
				(packet, ctx) -> {
					ctx.get().setPacketHandled(true);
					packet.handle(ctx);
				}
		);
		HANDLER.registerMessage(1, SUpdatePGData.class,
				SUpdatePGData::writePacketData,
				SUpdatePGData::new,
				(packet, ctx) -> {
					ctx.get().setPacketHandled(true);
					packet.handle(ctx);
				}
		);
	}
}
