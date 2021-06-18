package com.github.lorenzopapi.pgr;

import com.github.lorenzopapi.pgr.handler.PGRClientHandler;
import com.github.lorenzopapi.pgr.handler.PGRConfig;
import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.handler.PGRServerHandler;
import com.github.lorenzopapi.pgr.network.PGRMessageHandler;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import static com.github.lorenzopapi.pgr.util.Reference.LOGGER;
import static com.github.lorenzopapi.pgr.util.Reference.MOD_ID;

@Mod(MOD_ID)
public class PortalGunReborn {

	/**
	 TODO:
	    tags (OreDictionary)
	    portal shooting (duh)
	*/

	public PortalGunReborn() {
		LOGGER.info("Hello from PGR!");
		Reference.serverEH = new PGRServerHandler();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		// config
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PGRConfig.COMMON_SPEC);
		
		// client events
		if (FMLEnvironment.dist.isClient()) {
			Reference.clientEH = new PGRClientHandler();

			forgeBus.addListener(Reference.clientEH::onClientTick);
			forgeBus.addListener(Reference.clientEH::onClientDisconnect);
			forgeBus.addListener(Reference.clientEH::onRenderTick);
			forgeBus.addListener(Reference.clientEH::onKeyEvent);
			forgeBus.addListener(Reference.clientEH::onClickEvent);
			//forgeBus.addListener(Reference.clientEH::onHandRender);
			//forgeBus.addListener(Reference.clientEH::onFovModifierEvent);
			forgeBus.addListener(Reference.clientEH::onMouseEvent);
		}
		// networking (I'd assume)
		// Your assumption is correct, 4 points for you!
		PGRMessageHandler.register();
		// setup handling
		modBus.addListener(this::onClientSetup);
		// registries
		PGRRegistry.register();

		// left and right click handling
		forgeBus.addListener(Reference.serverEH::onBlockBreak);
		forgeBus.addListener(Reference.serverEH::onLeftClickBlock);
		forgeBus.addListener(Reference.serverEH::onRightClickItem);
		forgeBus.addListener(Reference.serverEH::onRightClickBlock);
		// item event handling
		forgeBus.addListener(Reference.serverEH::onItemCrafted);
		// entity event handling
		forgeBus.addListener(Reference.serverEH::onLivingUpdate);
		// world event handling
		forgeBus.addListener(Reference.serverEH::onWorldLoad);
		forgeBus.addListener(Reference.serverEH::onWorldUnload);
		// server shutdown handling
		forgeBus.addListener(Reference.serverEH::onServerStopping);
	}

	private void onClientSetup(FMLClientSetupEvent e) {
		PGRConfig.KeyBinds.registerKeyBindings();
		//TileEntityRendererDispatcher.instance.setSpecialRendererInternal(PGRRegistry.PORTAL_TILE_ENTITY.get(), new PGRRenderer(TileEntityRendererDispatcher.instance));
	}
}
