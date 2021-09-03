package com.github.lorenzopapi.pgr.mixin;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
	@Accessor("renderInfos")
	ObjectList<WorldRenderer.LocalRenderInformationContainer> getRenderInfos();
	
	@Accessor("renderDistanceChunks")
	int getRenderDistanceChunks();
	
	@Accessor("viewFrustum")
	ViewFrustum getViewFrustum();
	
	@Mixin(WorldRenderer.LocalRenderInformationContainer.class)
	interface RenderInformationContainerAccessor {
		@Accessor("renderChunk")
		ChunkRenderDispatcher.ChunkRender getRenderChunk();
	}
}
