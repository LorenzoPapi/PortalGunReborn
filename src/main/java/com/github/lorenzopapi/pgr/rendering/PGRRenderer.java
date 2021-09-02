package com.github.lorenzopapi.pgr.rendering;

import com.github.lorenzopapi.pgr.assets.Shaders;
import com.github.lorenzopapi.pgr.mixin.WorldRendererAccessor;
import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.portalgun.PortalBlockTileEntity;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.RendererUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class PGRRenderer extends TileEntityRenderer<PortalBlockTileEntity> {
	private static final RenderTypeBuffers buffersBlocks = new RenderTypeBuffers();
	private static PGRFrameBuffer buffer;
	private static PGRFrameBuffer clipping;
	private static DynamicTexture texture;
	private static ResourceLocation textureLocation = null;
	private static int recursionDepth = 0;

	public PGRRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	private static final RenderTypeBuffers buffers = new RenderTypeBuffers();
	
	@Override
	public void render(PortalBlockTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// TODO: check if the player is in a position where they could possibly see the portal
		
		PortalStructure struct = PGRUtils.findPortalByPosition(tileEntityIn.getWorld(), tileEntityIn.getPos());
		if (struct == null || struct.positions == null || struct.positions.size() < 1) return;
		
		if (!struct.positions.get(0).equals(tileEntityIn.getPos())) return;
		PortalStructure pairStruct = struct.pair;
		if (pairStruct == null) return;
		BlockPos pairPos = pairStruct.positions.get(0);
		
//		matrixStackIn.push();
//		if (struct.positions.get(0).getY() == struct.positions.get(1).getY())
//			matrixStackIn.rotate(new Quaternion(90, 0, 0, true));
//		else matrixStackIn.translate(0, 0, 1);
		
//		setupMatrix(matrixStackIn, struct);
		
//		RenderSystem.disableTexture();
//		RenderSystem.depthFunc(GL11.GL_ALWAYS);
//		RendererUtils.draw(matrixStackIn, 0, 0, struct.width, struct.height, 0);
//		RenderSystem.enableTexture();
//		matrixStackIn.pop();
		
		if (!Minecraft.getInstance().getFramebuffer().isStencilEnabled()) Minecraft.getInstance().getFramebuffer().enableStencil();
		setupStencil(matrixStackIn, struct);
		List<WorldRenderer.LocalRenderInformationContainer> infos = ((WorldRendererAccessor)Minecraft.getInstance().worldRenderer).getRenderInfos();
		
//		else if (struct.direction == Direction.EAST) matrixStackIn.translate(-1, -struct.height, 0);
		
//		infos.sort((i1, i2) -> {
//			return Double.compare(
//					((WorldRendererAccessor.RenderInformationContainerAccessor) i2).getRenderChunk().getPosition().add(8, 8, 8).distanceSq(pairPos),
//					((WorldRendererAccessor.RenderInformationContainerAccessor) i1).getRenderChunk().getPosition().add(8, 8, 8).distanceSq(pairPos)
//			);
//		});
		
		matrixStackIn.push();
		// TODO: make this be one draw call instead of 3
		setupMatrix(matrixStackIn, struct);
		matrixStackIn.translate(-(struct.width * 2000) / 2f, -(struct.height * 2000) / 2f, 10);
		matrixStackIn.scale(struct.width * 2000, struct.height * 2000, 1);
		RenderSystem.depthFunc(GL11.GL_ALWAYS);
		int skyColor = tileEntityIn.getWorld().getBiome(tileEntityIn.getPos()).getFogColor();
		RenderSystem.disableTexture();
		{
			int ci = (skyColor >> 16) & 0xFF;
			int cb = (skyColor >> 8) & 0xFF;
			int cg = (skyColor) & 0xFF;
			RenderSystem.enableFog();
			RenderSystem.color4f(ci / 255f, cb / 255f, cg / 255f, 1);
		}
		RendererUtils.draw(matrixStackIn, 0, 0, struct.width, struct.height, 0);
		matrixStackIn.translate(0, 0, 90);
		RendererUtils.draw(matrixStackIn, 0, 0, struct.width, struct.height, 0);
		matrixStackIn.translate(0, 0, 100);
		RendererUtils.draw(matrixStackIn, 0, 0, struct.width, struct.height, 0);
		matrixStackIn.translate(0, 0, 250);
		RendererUtils.draw(matrixStackIn, 0, 0, struct.width, struct.height, 0);
		RenderSystem.enableTexture();
		matrixStackIn.pop();
		
		/* start render sky */
		matrixStackIn.push();
		{
			ActiveRenderInfo info = Minecraft.getInstance().getRenderManager().info;
			double d0 = info.getProjectedView().x;
			double d1 = info.getProjectedView().y;
			boolean flag1 = Minecraft.getInstance().world.func_239132_a_().func_230493_a_(MathHelper.floor(d0), MathHelper.floor(d1)) || Minecraft.getInstance().ingameGUI.getBossOverlay().shouldCreateFog();
			FogRenderer.setupFog(info, FogRenderer.FogType.FOG_SKY, Minecraft.getInstance().gameRenderer.getFarPlaneDistance() - 20, flag1, partialTicks);
			Minecraft.getInstance().worldRenderer.renderSky(matrixStackIn, partialTicks);
		}
		matrixStackIn.pop();
		/* end render sky */
		
		/* start fix matrix */
		matrixStackIn.push();
		switch (struct.direction) {
			case NORTH:
				matrixStackIn.rotate(new Quaternion(0,90, 0, true));
				matrixStackIn.translate(-1, 0, 0);
				break;
			case SOUTH:
				matrixStackIn.rotate(new Quaternion(0,-90, 0, true));
				matrixStackIn.translate(0, 0, -1);
				break;
			case WEST:
				matrixStackIn.rotate(new Quaternion(0,180, 0, true));
				matrixStackIn.translate(-1, 0, -1);
				break;
			default:break;
		}
		/* end fix matrix */
		
		/* start setup matrix */
		matrixStackIn.rotate(pairStruct.direction.getRotation());
		matrixStackIn.rotate(new Quaternion(-90, 90, 0, true));
		if (pairStruct.direction == Direction.EAST) {
			matrixStackIn.translate(1, 0, -1);
		} else if (pairStruct.direction == Direction.NORTH) {
			matrixStackIn.translate(0, 0, 1);
			matrixStackIn.rotate(new Quaternion(0, 180, 0, true));
		} else if (pairStruct.direction == Direction.SOUTH) {
			matrixStackIn.translate(-1, 0, 0);
			matrixStackIn.rotate(new Quaternion(0, 180, 0, true));
		}
		/* end setup matrix */
		
		/* start render clouds */
		RenderSystem.enableDepthTest();
		Minecraft.getInstance().worldRenderer.renderClouds(matrixStackIn, partialTicks, pairPos.getX() + 0.5, pairPos.getY() + 0.5, pairPos.getZ() + 0.5);
		RenderSystem.depthFunc(GL20.GL_LEQUAL);
		Minecraft.getInstance().worldRenderer.renderClouds(matrixStackIn, partialTicks, pairPos.getX() + 0.5, pairPos.getY() + 0.5, pairPos.getZ() + 0.5);
		Minecraft.getInstance().worldRenderer.renderClouds(matrixStackIn, partialTicks, pairPos.getX() + 0.5, pairPos.getY() + 0.5, pairPos.getZ() + 0.5);
		/* end render clouds */
		
		/* start render blocks */
		// neat thing, this does actually return the block render types in the order they should be rendered in
		for (RenderType blockRenderType : RenderType.getBlockRenderTypes()) {
			blockRenderType.setupRenderState();
			loopInfos:
			for (WorldRenderer.LocalRenderInformationContainer info : infos) {
				ChunkRenderDispatcher.ChunkRender render = ((WorldRendererAccessor.RenderInformationContainerAccessor)info).getRenderChunk();
				if (render.compiledChunk.get().isLayerEmpty(blockRenderType)) continue;
				for (TileEntity tileEntity : render.compiledChunk.get().getTileEntities()) {
					for (BlockPos position : pairStruct.positions) {
						if (tileEntity.getPos().equals(position)) {
							continue loopInfos;
						}
					}
				}
				// TODO: transparency sort
				VertexBuffer buffer = render.getVertexBuffer(blockRenderType);
				BlockPos relPos = render.getPosition().subtract(pairPos);

				matrixStackIn.push();
				matrixStackIn.translate(relPos.getX(), relPos.getY(), relPos.getZ());

				{
					Vector3i vec = pairStruct.direction.getDirectionVec();
					int scl = struct.width - 1;
					matrixStackIn.translate(vec.getZ() * -scl,vec.getY() * -scl,vec.getX() * -scl);
				}
				matrixStackIn.translate(-1, 0, 0);

				buffer.bindBuffer();
				DefaultVertexFormats.BLOCK.setupBufferState(0L); // TODO: reference blockVertexFormat on WorldRenderer class for sake of better compatibility
				RenderSystem.enableDepthTest();
				RenderSystem.depthFunc(GL11.GL_LESS);
				buffer.draw(matrixStackIn.getLast().getMatrix(), 7);
				matrixStackIn.pop();
			}

			VertexBuffer.unbindBuffer();
			RenderSystem.clearCurrentColor();
			DefaultVertexFormats.BLOCK.clearBufferState();
			blockRenderType.clearRenderState();
		}
		/* end render blocks */
		
		/* start render tile entities */
		for (WorldRenderer.LocalRenderInformationContainer info : infos) {
			for (TileEntity te : ((WorldRendererAccessor.RenderInformationContainerAccessor)info).getRenderChunk().compiledChunk.get().getTileEntities()) {
				if (te instanceof PortalBlockTileEntity) continue;
				if (te != null) {
					TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(te);
					if (renderer != null) {
						matrixStackIn.push();
						matrixStackIn.translate(te.getPos().getX() - pairPos.getX(), te.getPos().getY() - pairPos.getY(), te.getPos().getZ() - pairPos.getZ());
						renderer.render(te, partialTicks, matrixStackIn, buffers.getBufferSource(),
								LightTexture.packLight(
										tileEntityIn.getWorld().getLightFor(LightType.BLOCK, tileEntityIn.getPos()),
										tileEntityIn.getWorld().getLightFor(LightType.SKY, tileEntityIn.getPos())
								), combinedOverlayIn
						);
						matrixStackIn.pop();
					}
				}
				
			}
		}
		buffers.getBufferSource().finish();
		/* end render tile entities */
		
		/* cleanup */
		RenderType.getSolid().setupRenderState();
		
		matrixStackIn.pop();
		
		clearStencil(matrixStackIn, struct);
	}
	
	public void setupMatrix(MatrixStack stack, PortalStructure struct) {
		stack.rotate(struct.direction.getRotation());
		stack.rotate(new Quaternion(90, 0, 0, true));
		if (struct.direction == Direction.WEST) stack.translate(0, -struct.height, 1);
		else if (struct.direction == Direction.EAST) stack.translate(-1, -struct.height, 0);
		else if (struct.direction == Direction.NORTH) stack.translate(-1, -struct.height, 1);
		else if (struct.direction == Direction.SOUTH) stack.translate(0, -struct.height, 0);
	}
	
	// https://gitlab.com/Spectre0987/TardisMod-1-14/-/blob/1.16/src/main/java/net/tardis/mod/client/renderers/boti/BOTIRenderer.java
	public void setupStencil(MatrixStack stack, PortalStructure structure) {
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		
		// Always write to stencil buffer
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		
		RenderSystem.enableDepthTest();
		stack.push();
		setupMatrix(stack, structure);
		RenderSystem.disableTexture();
		// TODO: color this as the fog color of the portal's pair
		RenderSystem.color4f(0, 0, 0, 1);
		RendererUtils.draw(stack, 0, 0, structure.width, structure.height, 0);
		RenderSystem.enableTexture();
		stack.pop();
		
		// Only pass stencil test if equal to 1(So only if rendered before)
		GL11.glStencilMask(0x00);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
	}
	
	// https://gitlab.com/Spectre0987/TardisMod-1-14/-/blob/1.16/src/main/java/net/tardis/mod/client/renderers/boti/BOTIRenderer.java
	public void clearStencil(MatrixStack stack, PortalStructure structure) {
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		
		GL11.glColorMask(false, false, false, false);
		stack.push();
		setupMatrix(stack, structure);
		RendererUtils.draw(stack, 0, 0, structure.width, structure.height, 0);
		stack.pop();
		
		//Set things back
		GL11.glColorMask(true, true, true, true);
	}
}
