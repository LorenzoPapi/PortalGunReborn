package com.github.lorenzopapi.pgr.portal.structure;

import com.github.lorenzopapi.pgr.client.audio.PortalAmbienceSound;
import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.block.PortalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PortalStructure {

	public final List<BlockPos> positions = new ArrayList<>();
	public final List<BlockPos> behinds = new ArrayList<>();
	public PortalStructure pair;
	public ChannelInfo info;
	public int width = 1;
	public int height = 2;
	public World world;
	public boolean isTypeA = true;
	public int color = 0; // Black
	public boolean initialized = false;
	public Direction direction = Direction.NORTH;
	public UpDirection upDirection = UpDirection.WALL;
	public boolean beingRemoved = false;

	public PortalStructure() {}

	public PortalStructure readFromNBT(CompoundNBT tag) {
		this.info = new ChannelInfo().readFromNBT(tag.getCompound("channelInfo"));
		this.isTypeA = tag.getBoolean("isTypeA");
		this.color = isTypeA ? info.colorA : info.colorB;
		this.width = tag.getInt("width");
		this.height = tag.getInt("height");
		if (tag.contains("direction")) {
			this.upDirection = UpDirection.valueOf(tag.getString("upDirection").toUpperCase());
			this.direction = Direction.byHorizontalIndex(tag.getInt("direction"));
			for (int i = 0; i < tag.getInt("posSize"); i++) {
				ListNBT list = tag.getList("pos_" + i, 6);
				this.positions.add(new BlockPos(list.getDouble(0), list.getDouble(1), list.getDouble(2)).toImmutable());
			}
			this.positions.sort(Vector3i::compareTo);
		}
		return this;
	}

	public CompoundNBT writeToNBT(CompoundNBT tag) {
		tag.put("channelInfo", info.writeToNBT(new CompoundNBT()));
		tag.putBoolean("isTypeA", isTypeA);
		tag.putInt("height", height);
		tag.putInt("width", width);
		if (positions.size() > 0) {
			tag.putString("upDirection", upDirection.toString());
			tag.putInt("direction", direction.getHorizontalIndex());
			tag.putInt("posSize", positions.size());
			//TODO: why isn't it an array of positions.....?
			//Like what the actual fuck old me what was on your stupid mind?????
			for (int i = 0; i < positions.size(); i++) {
				BlockPos pos = positions.get(i).toImmutable();
				tag.put("pos_" + i, newDoubleNBTList(pos.getX(), pos.getY(), pos.getZ()));
			}
		}
		return tag;
	}

	private ListNBT newDoubleNBTList(double... numbers) {
		ListNBT list = new ListNBT();
		for (double d0 : numbers) {
			list.add(DoubleNBT.valueOf(d0));
		}
		return list;
	}

	public void initialize(World world) {
		if (initialized)
			return;
		this.initialized = true;
		this.world = world;
		if (!world.isRemote) {
			for (BlockPos pos : positions) {
				BlockState state = PGRRegistry.PORTAL_BLOCK.getDefaultState().with(PortalBlock.HORIZONTAL_FACING, direction).with(PortalBlock.UP_FACING, upDirection);
				world.setBlockState(pos, state);
				if (upDirection != UpDirection.WALL)
					this.behinds.add(pos.offset(upDirection.toDirection().getOpposite()));
				else
					this.behinds.add(pos.offset(direction.getOpposite()));
			}
		}
		//TODO: ok so world will ALWAYS be a server world, and I'm calling a client class
		//It'll probably crash a dedicated server
		//But hey at least the client works!

		//Ok younger me what the fuck was wrong with you (like I know but still WTH)
		Minecraft.getInstance().getSoundHandler().play(new PortalAmbienceSound(this));
	}

	public PortalStructure setWidthAndHeight(int w, int h) {
		this.width = w;
		this.height = h;
		return this;
	}

	public PortalStructure setPositionsAndDirection(List<BlockPos> newPos, Direction dir, UpDirection upDir) {
		this.positions.clear();
		this.positions.addAll(newPos);
		this.positions.sort(Vector3i::compareTo);
		this.direction = dir;
		this.upDirection = upDir;
		return this;
	}

	public PortalStructure setWorld(World world) {
		this.world = world;
		return this;
	}

	public PortalStructure setChannelInfo(ChannelInfo info) {
		this.info = info;
		return this;
	}

	public PortalStructure setColor(int color) {
		this.color = color;
		return this;
	}

	public PortalStructure setType(boolean isTypeA) {
		this.isTypeA = isTypeA;
		return this;
	}

	public PortalStructure setPair(PortalStructure pair) {
		this.pair = pair;
		return this;
	}

	public boolean hasPair() {
		return this.pair != null;
	}

	public void removeStructure() {
		if (beingRemoved)
			return;
		this.beingRemoved = true;
		if (this.hasPair()) {
			this.pair.setPair(null);
			this.setPair(null);
		}
		for (BlockPos pos : positions) {
			if (upDirection != UpDirection.WALL)
				this.behinds.remove(pos.offset(upDirection.toDirection().getOpposite()));
			else
				this.behinds.remove(pos.offset(direction.getOpposite()));
			world.removeBlock(pos, false);
		}
		this.world.playSound(null, this.positions.get(0), PGRRegistry.PGRSounds.PORTAL_CLOSE, SoundCategory.BLOCKS, 0.4F, 1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.1F);
	}

	public boolean isSameStruct(PortalStructure other) {
		return other.isTypeA == this.isTypeA && other.info.uuid.equals(this.info.uuid) && other.info.channelName.equals(this.info.channelName);
	}

	public boolean isPair(PortalStructure other) {
		return other.isTypeA != this.isTypeA && other.info.uuid.equals(this.info.uuid) && other.info.channelName.equals(this.info.channelName);
	}

	@Override
	public String toString() {
		return "PortalStructure{" +
				       "positions=" + positions +
					   ", paired=" + hasPair() +
				       ", info=" + info +
				       ", width=" + width +
				       ", height=" + height +
				       ", isTypeA=" + isTypeA +
				       ", direction=" + direction +
				       ", upDirection=" + upDirection +
				       '}';
	}

	public enum UpDirection implements IStringSerializable {
		UP("up"),
		DOWN("down"),
		WALL("wall");

		private final String name;

		UpDirection(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}

		@Override
		public String getString() {
			return this.name;
		}

		public Direction toDirection() {
			if (this == UP) return Direction.UP;
			else if (this == DOWN) return Direction.DOWN;
			else throw new RuntimeException("Error, this shouldn't be happening what the actual fuck");
		}

		public static UpDirection fromDirection(Direction direction) {
			if (direction == Direction.UP) return UP;
			else if (direction == Direction.DOWN) return DOWN;
			else return WALL;
		}

	}
}
