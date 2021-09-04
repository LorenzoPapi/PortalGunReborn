package com.github.lorenzopapi.pgr.util;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.structure.ChannelIndicator;
import com.github.lorenzopapi.pgr.portal.structure.ChannelInfo;
import com.github.lorenzopapi.pgr.portal.PGRSavedData;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.portal.gun.PortalGunItem;
import com.github.lorenzopapi.pgr.portal.gun.PortalProjectileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PGRUtils {
	public static void shootPortal(LivingEntity living, ItemStack is, boolean isTypeA) {
		CompoundNBT tag = is.getTag();
		if (tag == null && living instanceof PlayerEntity) {
			PortalGunItem.setRandomNBTTags(is, (PlayerEntity) living);
			tag = is.getTag();
		}
		if (tag != null) {
			ChannelInfo channel = Reference.serverEH.lookupChannel(tag.getString("uuid"), tag.getString("channelName"));
			if (channel.colorA == -1 || channel.colorB == -1) {
				int[] colors = generateChannelColor(channel.uuid, channel.channelName);
				channel.setColor(colors[0], colors[1]);
			}
			PortalStructure structure = new PortalStructure().setWorld(living.getEntityWorld()).setChannelInfo(channel).setType(isTypeA).setPortalColor(isTypeA ? channel.colorA : channel.colorB).setWidthAndHeight(tag.getInt("width"), tag.getInt("height"));
			living.getEntityWorld().addEntity(new PortalProjectileEntity(living.getEntityWorld(), living, structure));
			living.playSound(isTypeA ? PGRRegistry.PGRSounds.PORTAL_GUN_FIRE_BLUE : PGRRegistry.PGRSounds.PORTAL_GUN_FIRE_RED, 0.2F, 1.0F + (living.getRNG().nextFloat() - living.getRNG().nextFloat()) * 0.1F);
		}
	}

	public static PGRSavedData getSaveDataForWorld(ServerWorld world) {
		String dataId = "PGRPortalData_" + world.getDimensionKey().getLocation().getPath();
		PGRSavedData data = world.getSavedData().getOrCreate(() -> {
			PGRSavedData ret = new PGRSavedData(dataId);
			if (world.getDimensionKey() == World.OVERWORLD) {
				ret.addChannel("Global", new ChannelInfo("Global", "Chell").setColor(361215, 16756742));
				ret.addChannel("Global", new ChannelInfo("Global", "Atlas").setColor(5482192, 4064209));
				ret.addChannel("Global", new ChannelInfo("Global", "P-body").setColor(16373344, 8394260));
			}
			return ret;
		}, dataId);
		data.initialize(world);
		data.markDirty();
		Reference.serverEH.portalInfoByDimension.put(world.getDimensionKey(), data);
		return data;
	}

	public static boolean spawnPortal(World world, BlockPos blockHitPos, Direction sideHit, Direction upDir, PortalStructure portal, int width, int height) {
		List<BlockPos> positions = canPlacePortal(world, blockHitPos, sideHit, upDir, width, height);
		if (positions != null) {
			PGRSavedData data = Reference.serverEH.getWorldSaveData(world.getDimensionKey());

			// Check if portal is already placed: if true, delete it
			PortalStructure struct = findPortalOfSameType(world, portal);
			if (struct != null)
				data.removePortal(struct);

			// Searches pair and links it
			PortalStructure possiblePair = data.findPair(portal);
			if (possiblePair != null) {
				portal.setPair(possiblePair);
				possiblePair.setPair(portal);
			}

			portal.setPositionsAndDirection(positions, sideHit.getAxis().isHorizontal() ? sideHit : upDir, PortalStructure.UpDirection.fromDirection(sideHit)).initialize(world);

			// Updates channel indicator
			ChannelIndicator indicator = Reference.serverEH.getPortalChannelIndicator(portal.info.uuid, portal.info.channelName, world.getDimensionKey());
			if (portal.isTypeA)
				indicator.setPortalAPlaced(true);
			else
				indicator.setPortalBPlaced(true);

			// Adds channel to saved data in case it's not existing and then adds portal
			if (!data.channelList.get(portal.info.uuid).contains(portal.info))
				data.addChannel(portal.info.uuid, portal.info);

			data.portals.add(portal);
			data.markDirty();
			return true;
		}
		return false;
	}

	public static PortalStructure findPortalByPosition(World world, BlockPos pos) {
		for (PortalStructure struct : Reference.serverEH.getWorldSaveData(world).portals) {
			if (struct.positions.contains(pos)) {
				return struct;
			}
		}
		return null;
	}

	public static List<PortalStructure> findPortalsInAABB(World world, AxisAlignedBB box) {
		List<PortalStructure> structures = new ArrayList<>();
		loopStructs:
		for (PortalStructure struct : Reference.serverEH.getWorldSaveData(world).portals) {
			for (BlockPos position : struct.positions) {
				if (box.intersects(new AxisAlignedBB(position))) {
					structures.add(struct);
					continue loopStructs;
				}
			}
		}
		return structures;
	}

	public static PortalStructure findPortalOfSameType(World world, PortalStructure toCheck) {
		for (PortalStructure struct : Reference.serverEH.getWorldSaveData(world).portals) {
			if (struct.isSameStruct(toCheck)) {
				return struct;
			}
		}
		return null;
	}

	public static List<BlockPos> canPlacePortal(World world, BlockPos positionHit, Direction sideHit, Direction facing, int pWidth, int pHeight) {
		HashMap<BlockPos, Boolean> checkedPositions = new HashMap<>();
		// The total of the iterations is (2w-1)(2h-1)
		for (int i = -pWidth + 1; i < pWidth; i++) {
			for (int k = -pHeight + 1; k < pHeight; k++) {
				int offX = 0;
				int offY = 0;
				int offZ = 0;
				switch (sideHit) {
					case UP:
					case DOWN:
						switch (facing) {
							case NORTH:
							case SOUTH:
								offX = i;
								offZ = k;
								break;
							case WEST:
							case EAST:
								offX = k;
								offZ = i;
								break;
						}
						break;
					case NORTH:
					case SOUTH:
						offX = i;
						offY = k;
						break;
					case WEST:
					case EAST:
						offZ = i;
						offY = k;
						break;
				}
				BlockPos offsetPos = positionHit.add(offX, offY, offZ);
				checkedPositions.put(offsetPos, null);
			}
		}

		ArrayList<BlockPos> validPositions = new ArrayList<>();
		boolean isHitValid = isPositionValidForPortal(world, positionHit, sideHit);
		if (!isHitValid)
			return null;
		else
			validPositions.add(0, positionHit.offset(sideHit).toImmutable());

		//Validate positions
		for (BlockPos pos : checkedPositions.keySet()) {
			boolean validPos = isPositionValidForPortal(world, pos, sideHit);
			checkedPositions.put(pos, validPos);
			if (positionHit.equals(pos)) {
				continue;
			}
			if (validPos) {
				validPositions.add(pos.offset(sideHit).toImmutable());
			}
		}
		return validPositions.size() >= pWidth * pHeight ? validPositions : null;
	}

	public static boolean isPositionValidForPortal(World world, BlockPos pos, Direction sideHit) {
		boolean validPos = isDirectionSolid(world, pos, sideHit);
		if (validPos) {
			BlockPos offset = pos.offset(sideHit);
			validPos = world.getBlockState(offset).getCollisionShape(world, offset).isEmpty() && world.getBlockState(offset).getBlock() != PGRRegistry.PORTAL_BLOCK;
		}
		return validPos;
	}

	public static boolean isDirectionSolid(World world, BlockPos pos, Direction direction) {
		if (World.isOutsideBuildHeight(pos)) {
			return false;
		} else {
			IChunk ichunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
			return ichunk != null && Block.doesSideFillSquare(ichunk.getBlockState(pos).getCollisionShape(world, pos, ISelectionContext.dummy()), direction);
		}
	}

	//Might change it if I feel like it lol
	public static int[] generateChannelColor(String uuid, String channelName) {
		String generator = uuid + "_" + channelName;
		Random rand = new Random();
		rand.setSeed(Math.abs(generator.hashCode() * uuid.hashCode()));
		int colorA = Math.round(1.6777215E7F * rand.nextFloat());
		float[] hsb = new float[3];
		Color.RGBtoHSB(colorA >> 16 & 0xFF, colorA >> 8 & 0xFF, colorA & 0xFF, hsb);
		hsb[2] = 0.65F + 0.25F * hsb[2];
		colorA = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		hsb[0] = hsb[0] + 0.5F;
		if (hsb[0] > 1.0F)
			hsb[0] = hsb[0] - 1.0F;
		int colorB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		return new int[]{colorA, colorB};
	}
}
