package com.github.lorenzopapi.pgr.network;

import com.github.lorenzopapi.pgr.network.message.IMessage;
import com.github.lorenzopapi.pgr.network.message.KeyEventMessage;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PGRMessageHandler {
	private static final String PROTOCOL_VERSION = Integer.toString(1);
	private static int counter = 0;
	public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
			                                            .named(new ResourceLocation(Reference.MODID, "main_channel"))
			                                            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
			                                            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
			                                            .networkProtocolVersion(() -> PROTOCOL_VERSION)
			                                            .simpleChannel();

	public static void register() {
		registerMessage(KeyEventMessage.class, new KeyEventMessage());
	}

	public static <T> void registerMessage(Class<T> clazz, IMessage<T> message) {
		HANDLER.registerMessage(counter, clazz, message::encode, message::decode, message::handle);
		counter++;
	}
}
