package com.github.lorenzopapi.pgr.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VBOTracker {
	private static final Map<TileEntity, VertexBuffer> bufferMap = new HashMap<>();
	private static final ArrayList<TileEntity> toRemove = new ArrayList<>();
	
	public static void untrack(TileEntity te) {
		if (RenderSystem.isOnRenderThread())
			bufferMap.remove(te);
		else toRemove.add(te);
	}
	
	public static void set(TileEntity te, VertexBuffer buffer) {
		for (TileEntity tileEntity : toRemove) bufferMap.remove(tileEntity);
		bufferMap.put(te, buffer);
	}
	
	public static VertexBuffer get(TileEntity te) {
		return bufferMap.get(te);
	}
}
