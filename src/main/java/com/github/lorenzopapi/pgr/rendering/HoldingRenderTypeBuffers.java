package com.github.lorenzopapi.pgr.rendering;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Util;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class HoldingRenderTypeBuffers extends IRenderTypeBuffer.Impl {
	private RegionRenderCacheBuilder fixedBuilder;
	private final Map<RenderType, BufferBuilder> fixedBuffers;
	
	public HoldingRenderTypeBuffers(BufferBuilder bufferIn) {
		this(bufferIn, new RegionRenderCacheBuilder());
	}
	
	public HoldingRenderTypeBuffers(BufferBuilder bufferIn, RegionRenderCacheBuilder fixedBuilder) {
		this(bufferIn, Util.make(new Object2ObjectLinkedOpenHashMap<>(), (p_228485_1_) -> {
			for (RenderType blockRenderType : RenderType.getBlockRenderTypes()) {
				p_228485_1_.put(blockRenderType, fixedBuilder.getBuilder(blockRenderType));
			}
		}));
		this.fixedBuilder = fixedBuilder;
	}
	
	public HoldingRenderTypeBuffers(BufferBuilder bufferIn, Map<RenderType, BufferBuilder> fixedBuffersIn) {
		super(bufferIn, fixedBuffersIn);
		fixedBuffers = fixedBuffersIn;
	}
	
	@Override
	public IVertexBuilder getBuffer(RenderType p_getBuffer_1_) {
		BufferBuilder builder = fixedBuffers.getOrDefault(p_getBuffer_1_, this.buffer);
		if (!this.startedBuffers.contains(builder)) {
			builder.begin(p_getBuffer_1_.getDrawMode(), p_getBuffer_1_.getVertexFormat());
			startedBuffers.add(builder);
		}
		return builder;
	}
	
	@Override
	public void finish(RenderType renderTypeIn) {
//		BufferBuilder bufferbuilder = (BufferBuilder) this.getBuffer(renderTypeIn);
//		boolean flag = Objects.equals(this.lastRenderType, renderTypeIn.getRenderType());
//		if (flag || bufferbuilder != this.buffer) {
//			if (this.startedBuffers.remove(bufferbuilder)) {
//				renderTypeIn.finish(bufferbuilder, 0, 0, 0);
//				if (flag) {
//					this.lastRenderType = Optional.empty();
//				}
//
//			}
//		}
		this.lastRenderType = Optional.empty();
	}
	
	@Override
	public void finish() {
//		for (BufferBuilder startedBuffer : startedBuffers) {
//			startedBuffer.finishDrawing();
//		}
		for (RenderType type : fixedBuffers.keySet()) {
			BufferBuilder builder = fixedBuffers.get(type);
			if (builder.isDrawing())
				type.finish(builder, 0, 0, 0);
//			buffer.finishDrawing();
		}
		if (buffer.isDrawing()) buffer.finishDrawing();
		startedBuffers.clear();
	}
	
	public Set<BufferBuilder> getBuilders() {
		return startedBuffers;
	}
}
