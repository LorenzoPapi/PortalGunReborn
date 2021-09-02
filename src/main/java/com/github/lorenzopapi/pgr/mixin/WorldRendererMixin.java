package com.github.lorenzopapi.pgr.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.math.vector.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Set;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Shadow private Set<ChunkRenderDispatcher.ChunkRender> chunksToUpdate;
	
	@Shadow @Final private ObjectList<WorldRenderer.LocalRenderInformationContainer> renderInfos;
	
	@Inject(at = @At("HEAD"), method = "updateCameraAndRender")
	public void preRender(MatrixStack matrixStackIn, float partialTicks, long finishTimeNano, boolean drawBlockOutline, ActiveRenderInfo activeRenderInfoIn, GameRenderer gameRendererIn, LightTexture lightmapIn, Matrix4f projectionIn, CallbackInfo ci) {
//		for (WorldRenderer.LocalRenderInformationContainer renderInfo : renderInfos) {
//		}
	}
}
