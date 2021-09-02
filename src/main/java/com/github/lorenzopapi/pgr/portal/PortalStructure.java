package com.github.lorenzopapi.pgr.portal;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portalgun.PortalBlock;
import com.github.lorenzopapi.pgr.portalgun.UpDirection;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PortalStructure {

	public List<BlockPos> positions = new ArrayList<>();
	public List<BlockPos> behinds = new ArrayList<>();
	public PortalStructure pair;
	public ChannelInfo info;
	public int width = 1;
	public int height = 2;
	public World world;
	public boolean isTypeA = true;
	public int portalColor = 0; // Black
	public boolean initialized = false;
	public Direction direction = Direction.NORTH;
	public UpDirection upDirection = UpDirection.WALL;
	public boolean beingRemoved = false;

	public PortalStructure() {}

	public PortalStructure readFromNBT(CompoundNBT tag) {
		this.info = new ChannelInfo().readFromNBT(tag.getCompound("channelInfo"));
		this.isTypeA = tag.getBoolean("isTypeA");
		this.portalColor = isTypeA ? info.colorA : info.colorB;
		if (tag.contains("direction")) {
			this.upDirection = UpDirection.valueOf(tag.getString("upDirection").toUpperCase());
			this.direction = Direction.byHorizontalIndex(tag.getInt("direction"));
			for (int i = 0; i < tag.getInt("posSize"); i++) {
				ListNBT list = tag.getList("pos_" + i, 6);
				this.positions.add(new BlockPos(list.getDouble(0), list.getDouble(1), list.getDouble(2)).toImmutable());
			}
		}
		return this;
	}

	@Override
	public String toString() {
		return "PortalStructure{" +
				       "positions=" + positions +
				       ", hasPair?=" + hasPair() +
				       ", info=" + info +
				       ", width=" + width +
				       ", height=" + height +
				       ", isTypeA=" + isTypeA +
				       ", direction=" + direction +
				       '}';
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
		this.world = world;
		if (initialized)
			return;
		this.initialized = true;
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
	}

	public PortalStructure setWidthAndHeight(int w, int h) {
		this.width = w;
		this.height = h;
		return this;
	}

	public PortalStructure setPositionsAndDirection(List<BlockPos> newPos, Direction dir, UpDirection upDir) {
		positions = new ArrayList<>(width * height);
		for (int i = 0; i < width * height; i++)
			positions.add(newPos.get(i));
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

	public PortalStructure setPortalColor(int portalColor) {
		this.portalColor = portalColor;
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
	}

	public boolean isSameStruct(PortalStructure other) {
		return other.isTypeA == this.isTypeA && other.info.uuid.equals(this.info.uuid) && other.info.channelName.equals(this.info.channelName);
	}

	public boolean isPair(PortalStructure other) {
		return other.isTypeA != this.isTypeA && other.info.uuid.equals(this.info.uuid) && other.info.channelName.equals(this.info.channelName);
	}
}
