package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.item.PortalGunItem;
import com.github.lorenzopapi.pgr.util.PortalGunHelper;
import com.github.lorenzopapi.pgr.portal.PortalsInWorldSavedData;
import com.github.lorenzopapi.pgr.portal.ChannelIndicator;
import com.github.lorenzopapi.pgr.portal.ChannelInfo;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;

public class PGREventHandler {
	public HashMap<String, ArrayList<ChannelIndicator>> portalChannelListeners = new HashMap<>();
	public HashMap<RegistryKey<World>, PortalsInWorldSavedData> portalInfoByDimension = new HashMap<>();

	
	public void onBlockBreak(BlockEvent.BreakEvent e) {
		ItemStack is = e.getPlayer().getHeldItem(Hand.MAIN_HAND);
		if (is.getItem() == PGRRegistry.PORTAL_GUN)
			e.setCanceled(true);
	}

	
	public void onClickBlock(PlayerInteractEvent.LeftClickBlock e) {
		ItemStack is = e.getPlayer().getHeldItem(Hand.MAIN_HAND);
		if (is.getItem() == PGRRegistry.PORTAL_GUN)
			e.setCanceled(true);
	}

	
	public void onItemCrafted(PlayerEvent.ItemCraftedEvent e) {
		ItemStack is = e.getCrafting();
		if (is.isItemEqual(new ItemStack(PGRRegistry.PORTAL_GUN))) {
			PortalGunItem.setRandomNBTTags(is, e.getPlayer());
			PortalsInWorldSavedData data = getWorldSaveData(e.getPlayer().world.getDimensionKey());
			String uuid = is.getTag().getString("uuid");
			String name = is.getTag().getString("channelName");
			int[] colors = PortalGunHelper.generateChannelColour(uuid, name);
			ChannelInfo newInfo = new ChannelInfo(uuid, name).setColour(colors[0], colors[1]);
			data.addChannel(uuid, newInfo);
			data.markDirty();
		}
	}

	public void onWorldLoad(WorldEvent.Load e) {
		//I hate my life
		if (e.getWorld() instanceof World) {
			getWorldSaveData(((World) e.getWorld()).getDimensionKey());
		}
	}
	
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent e) {
		if (!(e.getEntityLiving().getEntityWorld()).isRemote && e.getEntityLiving() instanceof ZombieEntity) {
			ZombieEntity zombie = (ZombieEntity)e.getEntityLiving();
			if (zombie.getHeldItemMainhand().getItem() == PGRRegistry.PORTAL_GUN && zombie.getRNG().nextFloat() < 0.008F)
				PortalGunHelper.shootPortal(zombie, zombie.getHeldItemMainhand(), zombie.getRNG().nextBoolean());
		}
	}

	public PortalsInWorldSavedData getWorldSaveData(RegistryKey<World> dimension) {
		PortalsInWorldSavedData data = this.portalInfoByDimension.get(dimension);
		if (data == null) {
			ServerWorld world = ServerLifecycleHooks.getCurrentServer().getWorld(dimension);
			data = (world != null) ? PortalGunHelper.getSaveDataForWorld(world) : new PortalsInWorldSavedData("PortalGunPortalData_somethingBroke");
		}
		return data;
	}

	public ChannelInfo lookupChannel(String uuid, String channelName) {
		PortalsInWorldSavedData data = getWorldSaveData(World.OVERWORLD);
		return data.getChannel(uuid, channelName);
	}

	
	public void onWorldUnload(WorldEvent.Unload e) {
		if (!e.getWorld().isRemote()) {
			RegistryKey<World> dimension = null;
			for (ServerWorld world : ServerLifecycleHooks.getCurrentServer().getWorlds()) {
				if (world.equals(e.getWorld())) {
					dimension = world.getDimensionKey();
					break;
				}
			}
			this.portalInfoByDimension.remove(dimension);
		}
	}

	
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent e) {
		this.portalChannelListeners.remove(UUIDTypeAdapter.fromUUID(e.getPlayer().getGameProfile().getId()));
	}

	
	public void onEntityEnterChunk(EntityEvent.EnteringChunk e) {
		if (PGRConfig.COMMON.canPortalProjectilesChunkload.get())
			PGRChunkHandler.checkAndCreateTicket(e);
	}

	
	public void onServerStopping(FMLServerStoppingEvent e) {
		this.portalChannelListeners.clear();
		this.portalInfoByDimension.clear();
		PGRChunkHandler.entitiesLoadingChunks.clear();
	}
}
