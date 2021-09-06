package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.portal.PGRSavedData;
import com.github.lorenzopapi.pgr.portal.gun.PortalGunItem;
import com.github.lorenzopapi.pgr.portal.structure.ChannelIndicator;
import com.github.lorenzopapi.pgr.portal.structure.ChannelInfo;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
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
	public final HashMap<RegistryKey<World>, PGRSavedData> portalInfoByDimension = new HashMap<>();
	public final ArrayList<ChannelIndicator> indicators = new ArrayList<>();

	public void onLivingEquip(LivingEquipmentChangeEvent e) {
		LivingEntity ent = e.getEntityLiving();
		if (ent instanceof PlayerEntity && e.getTo().isItemEqual(PGRRegistry.PORTAL_GUN.get().getDefaultInstance()))
			ent.world.playSound(null, ent.getPosition(), PGRRegistry.PGRSounds.PORTAL_GUN_EQUIP, SoundCategory.PLAYERS, 0.2F, 1.0F);
	}

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
			PGRSavedData data = getWorldSaveData(e.getPlayer().world);
			String uuid = is.getTag().getString("uuid");
			String name = is.getTag().getString("channelName");
			int[] colors = PGRUtils.generateChannelColor(uuid, name);
			ChannelInfo newInfo = new ChannelInfo(uuid, name).setColor(colors[0], colors[1]);
			data.addChannel(uuid, newInfo);
			data.markDirty();
		}
	}

	public void onHurtEvent(LivingHurtEvent e) {
		if ("inWall".equals(e.getSource().damageType))
			e.setCanceled(true);
	}

	public void onWorldLoad(WorldEvent.Load e) {
		//I hate my life
		if (e.getWorld() instanceof World)
			getWorldSaveData((World) e.getWorld());
	}

	public void onLivingUpdate(LivingEvent.LivingUpdateEvent e) {
		if (!(e.getEntityLiving().getEntityWorld()).isRemote && e.getEntityLiving() instanceof ZombieEntity) {
			ZombieEntity zombie = (ZombieEntity) e.getEntityLiving();
			if (zombie.getHeldItemMainhand().getItem() == PGRRegistry.PORTAL_GUN.get() && zombie.getRNG().nextFloat() < 0.008F)
				PGRUtils.shootPortal(zombie, zombie.getHeldItemMainhand(), zombie.getRNG().nextBoolean());
		}
	}

	public PGRSavedData getWorldSaveData(World world) {
		return getWorldSaveData(world.getDimensionKey());
	}

	public PGRSavedData getWorldSaveData(RegistryKey<World> dimension) {
		PGRSavedData data = this.portalInfoByDimension.get(dimension);
		if (data == null) {
			ServerWorld world = ServerLifecycleHooks.getCurrentServer().getWorld(dimension);
			data = (world != null) ? PGRUtils.getSaveDataForWorld(world) : new PGRSavedData("PGRPortalData_broken");
		}
		return data;
	}

	public ChannelInfo lookupChannel(String uuid, String channelName) {
		return getWorldSaveData(World.OVERWORLD).getChannel(uuid, channelName);
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
		for (PGRSavedData data : portalInfoByDimension.values()) {
			data.reset();
		}
		this.portalInfoByDimension.clear();
		this.indicators.clear();
	}
}
