package com.github.lorenzopapi.pgr.rendering;

import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.portal.PortalsInWorldSavedData;
import com.github.lorenzopapi.pgr.util.Reference;
import com.github.lorenzopapi.pgr.util.RendererHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Shader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.*;

import java.util.HashMap;
import java.util.Random;

public class PGRRenderer extends TileEntityRenderer {
	public PGRRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	private static final RenderTypeBuffers buffersBlocks = new RenderTypeBuffers();
	
	private static PGRFrameBuffer buffer;
	private static PGRFrameBuffer clipping;
	private static DynamicTexture texture;
	private static ResourceLocation textureLocation = null;
	
	private static int recursionDepth = 0;
	
	@Override
	public void render(TileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		if (recursionDepth > 1) return;
		
		PortalsInWorldSavedData data = Reference.pgrEventHandler.getWorldSaveData(tileEntityIn.getWorld().getDimensionKey());
		PortalStructure struct = data.findPortalByPosition(tileEntityIn.getWorld(), tileEntityIn.getPos());
		if (struct == null || struct.positions == null || struct.positions.length < 1) return;
		if (textureLocation == null) {
			buffer = new PGRFrameBuffer(256, 256 * 2, true, Minecraft.IS_RUNNING_ON_MAC);
			clipping = new PGRFrameBuffer(256, 256 * 2, true, Minecraft.IS_RUNNING_ON_MAC);
			texture = new DynamicTexture(1, 1, true);
			textureLocation = Minecraft.getInstance().getTextureManager().getDynamicTextureLocation("prg/portal_texture", texture);
			texture.glTextureId = buffer.getTextureID();
		}
//		MatrixStack stack = new MatrixStack();
		if (!struct.positions[0].equals(tileEntityIn.getPos())) return;
		PortalStructure pairStruct = struct.pair;
		if (pairStruct == null) return;
		
		recursionDepth++;
		int sizeX = struct.width;
//		int sizeY = struct.height;
		int sizeY = 1;
//		buffer.resize(Minecraft.getInstance().getMainWindow().getFramebufferWidth(), Minecraft.getInstance().getMainWindow().getFramebufferHeight(), true);
		buffer.resize(256, 256, true);
//		clipping.resize(Minecraft.getInstance().getMainWindow().getFramebufferWidth(), Minecraft.getInstance().getMainWindow().getFramebufferHeight(), true);
		
		matrixStackIn.push();
		if (struct.positions[0].getY() == struct.positions[1].getY()) matrixStackIn.rotate(new Quaternion(90, 0, 0, true));
		else matrixStackIn.translate(0, 0, 1);
		matrixStackIn.translate(0, 0, -0.01f);
		Minecraft.getInstance().getFramebuffer().unbindFramebuffer();
		buffer.bindFramebuffer(true);
		MatrixStack stack = new MatrixStack();
		stack.push();
		stack.translate(0, 0, -1.2f);
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos pairPos = pairStruct.positions[pairStruct.width / 2];
//		if (pairStruct.positions[0].getY() == pairStruct.positions[pairStruct.width].getY()) {
//			stack.rotate(new Quaternion(90, 0, 0, true));
//			stack.rotate(new Quaternion(180, 0, 0, true));
//		} else {
//			stack.translate(0, -0.1f, 0);
//		}
		stack.translate(-0.5f, -0.25f,0);
		for (int xOff = -16; xOff <= 16; xOff++) {
			for (int yOff = -16; yOff <= 16; yOff++) {
				for (int zOff = -16; zOff <= 16; zOff++) {
					stack.push();
					stack.translate(xOff, yOff, zOff);
					BlockPos renderPos = new BlockPos(
							pairPos.add(xOff, yOff, zOff)
					);
					BlockState block = tileEntityIn.getWorld().getBlockState(renderPos);
					RenderType type = RenderType.getSolid();
					for (RenderType blockRenderType : RenderType.getBlockRenderTypes())
						if (RenderTypeLookup.canRenderInLayer(block, blockRenderType))
							type = blockRenderType;
//					IVertexBuilder builder = bufferIn.getBuffer(type);
					IVertexBuilder builder = buffersBlocks.getBufferSource().getBuffer(type);
					dispatcher.renderModel(block, renderPos, tileEntityIn.getWorld(), stack, builder, true, new Random(renderPos.toLong()));
					stack.pop();
				}
			}
		}
		stack.pop();
		
		for (RenderType blockRenderType : RenderType.getBlockRenderTypes()) {
			BufferBuilder bufferBuilder = (BufferBuilder) buffersBlocks.getBufferSource().getBuffer(blockRenderType);
			bufferBuilder.sortVertexData(tileEntityIn.getPos().getX(), tileEntityIn.getPos().getY(), tileEntityIn.getPos().getZ());
			bufferBuilder.finishDrawing();
			Minecraft.getInstance().getFramebuffer().unbindFramebuffer();
			buffer.setFramebufferColor(0, 0, 0, 1);
			buffer.bindFramebuffer(false);
//			RenderSystem.pushMatrix();
			RenderSystem.pushMatrix();
			blockRenderType.setupRenderState();
			WorldVertexBufferUploader.draw(bufferBuilder);
			RenderSystem.popMatrix();
//			Vector4f vector = new Vector4f(0, 0, 0, 0);
//			vector.transform(matrixStackIn.getLast().getMatrix());
//			blockRenderType.clearRenderState();
//			RenderSystem.popMatrix();
			buffer.unbindFramebuffer();
			Minecraft.getInstance().getFramebuffer().bindFramebuffer(true);
			texture.updateDynamicTexture();
		}
		buffer.unbindFramebuffer();
		Minecraft.getInstance().getFramebuffer().bindFramebuffer(true);
//		Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
//		Vector4f corner1 = new Vector4f(-sizeX / 2f + 0.5f, 0, 0, 1);
//		Vector4f corner2 = new Vector4f(-sizeX / 2f + 0.5f, sizeY, 0, 1);
//		Vector4f corner3 = new Vector4f(sizeX, sizeY, 0, 1);
//		Vector4f corner4 = new Vector4f(sizeX, 0, 0, 1);
//		corner1.transform(matrix4f);
//		corner2.transform(matrix4f);
//		corner3.transform(matrix4f);
//		corner4.transform(matrix4f);
//		IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntitySolid(textureLocation));
//		RendererHelper.drawSquare(corner1, corner2, corner3, corner4, new Vector3f(1, 0, 0), builder, combinedLightIn, 0, 1, 0, 1);
		RendererHelper.drawTexture(matrixStackIn, textureLocation, -sizeX / 2f + 0.5f, 0, sizeX, sizeY, 0);
		matrixStackIn.pop();
		
		recursionDepth--;
	}
	
	/*
	BlockPos pairPos = struct.pair.positions[struct.pair.width / 2];
//		{
//			Matrix4f matrix4f = matrixStackIn.getLast().getMatrix().copy();
//			matrix4f.setTranslation(0,0,0);
//			stack.getLast().getMatrix().mul(matrix4f);
//		}
		{
			Matrix4f matrix4f = matrixStackIn.getLast().getMatrix().copy();
			matrix4f.setTranslation(0,0,0);
			stack.getLast().getMatrix().mul(matrix4f);
//			stack.rotate(new Quaternion(0, 180, 0, true));
		}
		if (pairStruct.positions[0].getY() == pairStruct.positions[pairStruct.width].getY()) {
			stack.rotate(new Quaternion(90, 0, 0, true));
			stack.rotate(new Quaternion(180, 0, 0, true));
		} else {
			stack.translate(0, -0.1f, 0);
		}
		stack.translate(-0.5f, 0, 0);
		for (int xOff = -16; xOff <= 16; xOff++) {
			for (int yOff = -16; yOff <= 16; yOff++) {
				for (int zOff = -16; zOff <= 16; zOff++) {
					stack.push();
					stack.translate(xOff, yOff, zOff);
					BlockPos renderPos = new BlockPos(
							pairPos.add(xOff, yOff, zOff)
					);
					BlockState block = tileEntityIn.getWorld().getBlockState(renderPos);
					RenderType type = RenderType.getSolid();
					for (RenderType blockRenderType : RenderType.getBlockRenderTypes())
						if (RenderTypeLookup.canRenderInLayer(block, blockRenderType))
							type = blockRenderType;
					IVertexBuilder builder = buffersBlocks.getBufferSource().getBuffer(type);
					dispatcher.renderModel(block, renderPos, tileEntityIn.getWorld(), stack, builder, true, new Random(renderPos.toLong()));
					stack.pop();
				}
			}
		}
		int sizeX = pairStruct.width;
		int sizeY = pairStruct.height;
		buffer.resize(
				(int) ((sizeX) * 256),
				(int) ((sizeY) * 256),
				Minecraft.IS_RUNNING_ON_MAC
		);
		for (RenderType blockRenderType : RenderType.getBlockRenderTypes()) {
			BufferBuilder bufferBuilder = (BufferBuilder) buffersBlocks.getBufferSource().getBuffer(blockRenderType);
			bufferBuilder.sortVertexData(tileEntityIn.getPos().getX(), tileEntityIn.getPos().getY(), tileEntityIn.getPos().getZ());
			bufferBuilder.finishDrawing();
			Minecraft.getInstance().getFramebuffer().unbindFramebuffer();
			buffer.setFramebufferColor(0, 0, 0, 1);
			buffer.bindFramebuffer(true);
//			RenderSystem.pushMatrix();
			RenderSystem.pushMatrix();
			blockRenderType.setupRenderState();
			WorldVertexBufferUploader.draw(bufferBuilder);
			RenderSystem.popMatrix();
//			Vector4f vector = new Vector4f(0, 0, 0, 0);
//			vector.transform(matrixStackIn.getLast().getMatrix());
//			blockRenderType.clearRenderState();
//			RenderSystem.popMatrix();
			buffer.unbindFramebuffer();
			Minecraft.getInstance().getFramebuffer().bindFramebuffer(true);
			texture.updateDynamicTexture();
		}
	 */
}
