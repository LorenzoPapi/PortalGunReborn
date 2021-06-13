package com.github.lorenzopapi.pgr.util;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.handler.PGRSounds;
import com.github.lorenzopapi.pgr.portal.ChannelIndicator;
import com.github.lorenzopapi.pgr.portal.ChannelInfo;
import com.github.lorenzopapi.pgr.portal.PGRSavedData;
import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.portalgun.PortalGunItem;
import com.github.lorenzopapi.pgr.portalgun.PortalProjectileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PortalGunHelper {
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
			PortalStructure structure = new PortalStructure().setWorld(living.getEntityWorld()).setChannelInfo(channel).setType(isTypeA).setColor(isTypeA ? channel.colorA : channel.colorB).setWidthAndHeight(tag.getInt("width"), tag.getInt("height"));
			living.getEntityWorld().addEntity(new PortalProjectileEntity(living.getEntityWorld(), living, structure));
			EntityHelper.playSoundAtEntity(living, isTypeA ? PGRSounds.pg_wpn_portal_gun_fire_blue : PGRSounds.pg_wpn_portal_gun_fire_red, living.getSoundCategory(), 0.2F, 1.0F + (living.getRNG().nextFloat() - living.getRNG().nextFloat()) * 0.1F);
		}
	}

	public static PGRSavedData getSaveDataForWorld(ServerWorld world) {
		String dataIdForDim = getDataIdForDim(world);
		PGRSavedData savedData = world.getSavedData().get(() -> new PGRSavedData(dataIdForDim), dataIdForDim);
		if (savedData == null) {
			savedData = new PGRSavedData(dataIdForDim);
			world.getSavedData().set(savedData);
			if (world.getDimensionKey() == World.OVERWORLD) {
				savedData.addChannel("Global", new ChannelInfo("Global", "Chell").setColor(361215, 16756742));
				savedData.addChannel("Global", new ChannelInfo("Global", "Atlas").setColor(5482192, 4064209));
				savedData.addChannel("Global", new ChannelInfo("Global", "P-body").setColor(16373344, 8394260));

			}
		}
		savedData.initialize(world);
		savedData.markDirty();
		Reference.serverEH.portalInfoByDimension.put(world.getDimensionKey(), savedData);
		return savedData;
	}

	public static boolean spawnPortal(World world, BlockPos blockHitPos, Direction sideHit, Direction upDir, PortalStructure portal, int width, int height) {
		BlockPos[] positions = canPlacePortal(world, blockHitPos, sideHit, upDir, width, height);
		if (positions != null) {
			PGRSavedData data = Reference.serverEH.getWorldSaveData(world.getDimensionKey());

			// Check is portal is already placed: if true, delete it. Then, initializes the new portal
			PortalStructure struct = data.findPortalOfSameType(portal);
			if (struct != null) {
				data.removePortal(struct);
			}
			portal.setPositions(positions).initialize(world);

			// Updates channel indicator
			ChannelIndicator indicator = Reference.serverEH.getPortalChannelIndicator(portal.info.uuid, portal.info.channelName, world.getDimensionKey());
			if (portal.isTypeA) {
				indicator.setPortalAPlaced(true);
			} else {
				indicator.setPortalBPlaced(true);
			}

			// Searches pair and links it
			PortalStructure possiblePair = data.findPair(portal);
			if (possiblePair != null) {
				portal.setPair(possiblePair);
				possiblePair.setPair(portal);
			}

			// Adds channel to saved data in case it's not existing and then adds portal
			if (!data.channelList.get(portal.info.uuid).contains(portal.info)) {
				data.addChannel(portal.info.uuid, portal.info);
			}
			data.portals.add(portal);
			return true;
		} else {
			return false;
		}
	}

	public static BlockPos[] canPlacePortal(World world, BlockPos positionHit, Direction sideHit, Direction facing, int pWidth, int pHeight) {
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
				validPositions.add(pos.offset(sideHit));
			}
		}
		if (validPositions.size() >= pWidth * pHeight) {
			BlockPos[] validPositionsArr = new BlockPos[validPositions.size()];
			validPositions.toArray(validPositionsArr);
			return validPositionsArr;
		} else {
			return null;
		}
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

	public static String getDataIdForDim(ServerWorld world) {
		return "PGRPortalData_" + world.getDimensionKey().getLocation().getPath();
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
		return new int[] { colorA, colorB };
	}
}
