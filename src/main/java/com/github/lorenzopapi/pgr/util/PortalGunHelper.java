package com.github.lorenzopapi.pgr.util;

import com.github.lorenzopapi.pgr.entity.PortalProjectileEntity;
import com.github.lorenzopapi.pgr.handler.PGRConfig;
import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.handler.PGRSoundEvents;
import com.github.lorenzopapi.pgr.item.PortalGunItem;
import com.github.lorenzopapi.pgr.portal.ChannelInfo;
import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.portal.PortalsInWorldSavedData;
import com.github.lorenzopapi.pgr.util.EntityHelper;
import com.github.lorenzopapi.pgr.util.Reference;
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
			ChannelInfo channel = Reference.pgrEventHandler.lookupChannel(tag.getString("uuid"), tag.getString("channelName"));
			PortalStructure info = new PortalStructure().setIndicator(tag.getString("uuid"), tag.getString("channelName")).setType(isTypeA).setColour(isTypeA ? channel.colourA : channel.colourB);
			living.getEntityWorld().addEntity(new PortalProjectileEntity(living.getEntityWorld(), living, tag.getInt("width"), tag.getInt("height"), info, PGRConfig.COMMON.maxShootDistance.get()));
			EntityHelper.playSoundAtEntity(living, isTypeA ? PGRSoundEvents.pg_wpn_portal_gun_fire_blue : PGRSoundEvents.pg_wpn_portal_gun_fire_red, living.getSoundCategory(), 0.2F, 1.0F + (living.getRNG().nextFloat() - living.getRNG().nextFloat()) * 0.1F);
		}
	}

	public static PortalsInWorldSavedData getSaveDataForWorld(ServerWorld world) {
		String dataIdForDim = getDataIdForDim(world);
		PortalsInWorldSavedData savedData = world.getSavedData().get(() -> new PortalsInWorldSavedData(dataIdForDim), dataIdForDim);
		if (savedData == null) {
			savedData = new PortalsInWorldSavedData(dataIdForDim);
			world.getSavedData().set(savedData);
			if (world.getDimensionKey() == World.OVERWORLD) {
				savedData.addChannel("Global", new ChannelInfo("Global", "Chell").setColour(361215, 16756742));
				savedData.addChannel("Global", new ChannelInfo("Global", "Atlas").setColour(5482192, 4064209));
				savedData.addChannel("Global", new ChannelInfo("Global", "P-body").setColour(16373344, 8394260));

			}
		}
		savedData.initialize(world);
		savedData.markDirty();
		Reference.pgrEventHandler.portalInfoByDimension.put(world.getDimensionKey(), savedData);
		return savedData;
	}

	public static boolean spawnPortal(World world, BlockPos blockHitPos, Direction sideHit, Direction upDir, PortalStructure portal, int width, int height) {
		BlockPos[] positions = canPlacePortal(world, blockHitPos, sideHit, upDir, width, height);
		if (positions != null) {
			PortalsInWorldSavedData data = Reference.pgrEventHandler.getWorldSaveData(world.getDimensionKey());
			PortalStructure struct = data.findPortalOfSameType(portal);
			if (struct != null) {
				data.removePortal(struct);
			}
			portal.setPositions(positions);
			portal.initialize(world);
			PortalStructure possiblePair = data.findPair(portal);
			if (possiblePair != null) {
				portal.setPair(possiblePair);
				possiblePair.setPair(portal);
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
			validPos = world.getBlockState(offset).getCollisionShape(world, offset).isEmpty() && isDirectionSolid(world, offset, sideHit) && world.getBlockState(pos).getBlock() != PGRRegistry.PORTAL_BLOCK;
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
	public static int[] generateChannelColour(String uuid, String channelName) {
		String generator = uuid + "_" + channelName;
		Random rand = new Random();
		rand.setSeed(Math.abs(generator.hashCode() * uuid.hashCode()));
		int colourA = Math.round(1.6777215E7F * rand.nextFloat());
		float[] hsb = new float[3];
		Color.RGBtoHSB(colourA >> 16 & 0xFF, colourA >> 8 & 0xFF, colourA & 0xFF, hsb);
		hsb[2] = 0.65F + 0.25F * hsb[2];
		colourA = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		hsb[0] = hsb[0] + 0.5F;
		if (hsb[0] > 1.0F)
			hsb[0] = hsb[0] - 1.0F;
		int colourB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		return new int[] { colourA, colourB };
	}
}
