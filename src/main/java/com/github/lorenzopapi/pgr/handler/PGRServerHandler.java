package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.portal.ChannelIndicator;
import com.github.lorenzopapi.pgr.portal.ChannelInfo;
import com.github.lorenzopapi.pgr.portal.PGRSavedData;
import com.github.lorenzopapi.pgr.portalgun.PortalGunItem;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;

public class PGRServerHandler {
	public HashMap<RegistryKey<World>, PGRSavedData> portalInfoByDimension = new HashMap<>();
	public ArrayList<ChannelIndicator> indicators = new ArrayList<>();

	public void onRightClickItem(PlayerInteractEvent.RightClickItem e) {
		ItemStack is = e.getPlayer().getHeldItem(Hand.MAIN_HAND);
		if (is.getItem() == PGRRegistry.PORTAL_GUN.get())
			e.setCanceled(true);
	}

	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
		ItemStack is = e.getPlayer().getHeldItem(Hand.MAIN_HAND);
		if (is.getItem() == PGRRegistry.PORTAL_GUN.get())
			e.setCanceled(true);
	}
	
	public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock e) {
		ItemStack is = e.getPlayer().getHeldItem(Hand.MAIN_HAND);
		if (is.getItem() == PGRRegistry.PORTAL_GUN.get())
			e.setCanceled(true);
	}

	public void onBlockBreak(PlayerEvent.BreakSpeed e) {
		ItemStack is = e.getPlayer().getHeldItem(Hand.MAIN_HAND);
		if (is.getItem() == PGRRegistry.PORTAL_GUN.get())
			e.setCanceled(true);
	}

	public void onItemCrafted(PlayerEvent.ItemCraftedEvent e) {
		ItemStack is = e.getCrafting();
		if (is.isItemEqual(PGRRegistry.PORTAL_GUN.get().getDefaultInstance())) {
			PortalGunItem.setRandomNBTTags(is, e.getPlayer());
			PGRSavedData data = getWorldSaveData(e.getPlayer().world.getDimensionKey());
			String uuid = is.getTag().getString("uuid");
			String name = is.getTag().getString("channelName");
			int[] colors = PGRUtils.generateChannelColor(uuid, name);
			ChannelInfo newInfo = new ChannelInfo(uuid, name).setColor(colors[0], colors[1]);
			data.addChannel(uuid, newInfo);
			data.markDirty();
		}
	}

	public void onHurtEvent(LivingHurtEvent e) {
		if (e.getSource().damageType.equals("inWall")) {
			e.setCanceled(true);
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
			if (zombie.getHeldItemMainhand().getItem() == PGRRegistry.PORTAL_GUN.get() && zombie.getRNG().nextFloat() < 0.008F)
				PGRUtils.shootPortal(zombie, zombie.getHeldItemMainhand(), zombie.getRNG().nextBoolean());
		}
	}

	public PGRSavedData getWorldSaveData(RegistryKey<World> dimension) {
		PGRSavedData data = this.portalInfoByDimension.get(dimension);
		if (data == null) {
			ServerWorld world = ServerLifecycleHooks.getCurrentServer().getWorld(dimension);
			data = (world != null) ? PGRUtils.getSaveDataForWorld(world) : new PGRSavedData("PortalGunPortalData_somethingBroke");
		}
		return data;
	}

	public ChannelInfo lookupChannel(String uuid, String channelName) {
		PGRSavedData data = getWorldSaveData(World.OVERWORLD);
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

	public ChannelIndicator getPortalChannelIndicator(String uuid, String channel, RegistryKey<World> dimension) {
		if (!indicators.isEmpty()) {
			for (ChannelIndicator indicator : indicators) {
				if (indicator.dimension == dimension && indicator.info.uuid.equals(uuid) && indicator.info.channelName.equals(channel)) {
					return indicator;
				}
			}
		}
		PGRSavedData data = Reference.serverEH.getWorldSaveData(dimension);
		for (ChannelInfo info : data.channelList.getOrDefault(uuid, new ArrayList<>())) {
			if (info.uuid.equals(uuid) && info.channelName.equals(channel)) {
				ChannelIndicator indicator = new ChannelIndicator(info, dimension);
				this.indicators.add(indicator);
				return indicator;
			}
		}
		return new ChannelIndicator(new ChannelInfo(), dimension);
	}

	public void onServerStopping(FMLServerStoppingEvent e) {
		this.portalInfoByDimension.clear();
		this.indicators.clear();
	}
}
