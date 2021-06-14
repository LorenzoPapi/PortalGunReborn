package com.github.lorenzopapi.pgr.portal;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
import java.util.Arrays;

public class PortalStructure {

    public BlockPos[] positions = null;
    public PortalStructure pair;
    public ChannelInfo info;
    public int width = 1;
    public int height = 2;
    public World world;
    public boolean isTypeA = true;
    public int color = Color.WHITE.getRGB();
    public boolean initialized = false;

    public PortalStructure() {}

    public PortalStructure readFromNBT(CompoundNBT tag, boolean readPositions) {
        this.info = new ChannelInfo().readFromNBT(tag.getCompound("channelInfo"));
        this.isTypeA = tag.getBoolean("isTypeA");
        this.color = isTypeA ? info.colorA : info.colorB;
        if (readPositions) {
            this.positions = new BlockPos[tag.getInt("width") * tag.getInt("height")];
            for (int i = 0; i < tag.getInt("posLength"); i++) {
                this.positions[i] = new BlockPos(tag.getInt("x_" + i), tag.getInt("y_" + i), tag.getInt("z_" + i));
            }
        }
        return this;
    }

    public CompoundNBT writeToNBT(CompoundNBT tag) {
        tag.put("channelInfo", info.writeToNBT(new CompoundNBT()));
        tag.putBoolean("isTypeA", isTypeA);
        tag.putInt("height", height);
        tag.putInt("width", width);
        if (positions == null || Arrays.asList(positions).contains(null)) {
            positions = new BlockPos[0];
        }
        tag.putInt("posLength", positions.length);
        for (int i = 0; i < positions.length; i++) {
            BlockPos pos = positions[i];
            tag.putInt("x_" + i, pos.getX());
            tag.putInt("y_" + i, pos.getY());
            tag.putInt("z_" + i, pos.getZ());
        }
        return tag;
    }

    public void initialize(World world) {
        this.world = world;
        if (initialized)
            return;
        this.initialized = true;
        for (BlockPos pos : positions) {
            world.setBlockState(pos, PGRRegistry.PORTAL_BLOCK.getDefaultState());
        }
    }

    public PortalStructure setWidthAndHeight(int w, int h) {
        this.width = w;
        this.height = h;
        return this;
    }

    public PortalStructure setPositions(BlockPos[] newPos) {
        positions = new BlockPos[width*height];
        System.arraycopy(newPos, 0, positions, 0, positions.length);
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

    public void removeStructure() {
        for (BlockPos pos : positions) {
            world.removeTileEntity(pos);
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
