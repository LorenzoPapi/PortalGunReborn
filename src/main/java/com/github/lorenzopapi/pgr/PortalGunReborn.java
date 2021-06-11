package com.github.lorenzopapi.pgr;

import com.github.lorenzopapi.pgr.handler.PGRChunkHandler;
import com.github.lorenzopapi.pgr.handler.PGRClientEventHandler;
import com.github.lorenzopapi.pgr.handler.PGRConfig;
import com.github.lorenzopapi.pgr.handler.PGREventHandler;
import com.github.lorenzopapi.pgr.network.PGRMessageHandler;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import static com.github.lorenzopapi.pgr.util.Reference.LOGGER;
import static com.github.lorenzopapi.pgr.util.Reference.MODID;

@Mod(MODID)
public class PortalGunReborn {

	/**
	 TODO:
	    tags (OreDictionary)
	    portal shooting (duh)
	*/

	public PortalGunReborn() {
		LOGGER.info("Hello from PGR!");
		Reference.pgrEventHandler = new PGREventHandler();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		if (FMLEnvironment.dist.isClient()) {
			Reference.pgrClientEventHandler = new PGRClientEventHandler();

			forgeBus.addListener(Reference.pgrClientEventHandler::onClientTick);
			forgeBus.addListener(Reference.pgrClientEventHandler::onClientDisconnect);
			forgeBus.addListener(Reference.pgrClientEventHandler::onRenderTick);
			forgeBus.addListener(Reference.pgrClientEventHandler::onKeyEvent);
			forgeBus.addListener(Reference.pgrClientEventHandler::onHandRender);
			forgeBus.addListener(Reference.pgrClientEventHandler::onFovModifierEvent);
			forgeBus.addListener(Reference.pgrClientEventHandler::onMouseEvent);
		}
		PGRMessageHandler.register();
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PGRConfig.COMMON_SPEC);

		forgeBus.addListener(Reference.pgrEventHandler::onBlockBreak);
		forgeBus.addListener(Reference.pgrEventHandler::onClickBlock);
		forgeBus.addListener(Reference.pgrEventHandler::onItemCrafted);
		forgeBus.addListener(Reference.pgrEventHandler::onLivingUpdate);
		forgeBus.addListener(Reference.pgrEventHandler::onWorldLoad);
		forgeBus.addListener(Reference.pgrEventHandler::onWorldUnload);
		forgeBus.addListener(Reference.pgrEventHandler::onPlayerLogout);
		forgeBus.addListener(Reference.pgrEventHandler::onEntityEnterChunk);
		forgeBus.addListener(Reference.pgrEventHandler::onServerStopping);
	}

	private void onCommonSetup(FMLCommonSetupEvent e) {
		if (PGRConfig.COMMON.canPortalProjectilesChunkload.get())
			ForgeChunkManager.setForcedChunkLoadingCallback(MODID, new PGRChunkHandler());
	}

	private void onClientSetup(FMLClientSetupEvent e) {
		PGRConfig.KeyBinds.registerKeyBindings();
	}
}
