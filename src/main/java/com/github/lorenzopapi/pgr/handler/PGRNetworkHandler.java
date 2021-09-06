package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.network.IMessage;
import com.github.lorenzopapi.pgr.network.KeyEventMessage;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PGRNetworkHandler {
	private static final String PROTOCOL_VERSION = Integer.toString(1);
	public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
			                                            .named(new ResourceLocation(Reference.MOD_ID, "main_channel"))
			                                            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
			                                            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
			                                            .networkProtocolVersion(() -> PROTOCOL_VERSION)
			                                            .simpleChannel();
	private static int id = 0;

	public static void register() {
		registerMessage(KeyEventMessage.class, new KeyEventMessage());
	}

	public static <T> void registerMessage(Class<T> clazz, IMessage<T> message) {
		HANDLER.registerMessage(id, clazz, message::encode, message::decode, message::handle);
		id++;
	}
}
