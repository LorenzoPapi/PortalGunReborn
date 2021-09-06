package com.github.lorenzopapi.pgr.rendering;

import com.github.lorenzopapi.pgr.portal.block.PortalBlockTileEntity;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.RendererUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.settings.CloudOption;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.IChunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class PGRRenderer extends TileEntityRenderer<PortalBlockTileEntity> {
	private static final RenderTypeBuffers buffersBlocks = new RenderTypeBuffers();
	private static PGRFrameBuffer buffer;
	private static PGRFrameBuffer clipping;
	private static DynamicTexture texture;
	private static final ResourceLocation textureLocation = null;
	private static int recursionDepth = 0;
	
	public PGRRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	private static final RenderTypeBuffers buffers = new RenderTypeBuffers();
	
	private static final HoldingRenderTypeBuffers vboTool = new HoldingRenderTypeBuffers(new BufferBuilder(16));
	
	@Override
	public void render(PortalBlockTileEntity portalTE, float partialTicks, MatrixStack stacks, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// TODO: check if the player is in a position where they could possibly see the portal
		
		PortalStructure portal = PGRUtils.findPortalByPosition(portalTE.getWorld(), portalTE.getPos());
		// I removed a null check here, which was always false, because the positions cannot be null since it's defined as an empty list from the beginning
		if (portal == null || portal.positions.isEmpty()) return;
		
		if (!portal.positions.get(0).equals(portalTE.getPos())) return;
		PortalStructure pairStruct = portal.pair;
		if (pairStruct == null) {
			stacks.push();
			setupMatrix(stacks, portal);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			for (int w = 0; w < portal.width; w++) {
				for (int h = 0; h < portal.height; h++) {
					RenderSystem.color4f(
							((portal.color >> 16) & 0xFF) / 255f,
							((portal.color >> 8) & 0xFF) / 255f,
							(portal.color & 0xFF) / 255f,
							1
					);
					RendererUtils.drawTexture(
							stacks, new ResourceLocation("minecraft:null"),
							w, h, 1, 1, 0
					);
				}
			}
			stacks.pop();
			return;
		}
		BlockPos pairPos = pairStruct.positions.get(0);

//		stacks.push();
//		if (portal.positions.get(0).getY() == portal.positions.get(1).getY())
//			stacks.rotate(new Quaternion(90, 0, 0, true));
//		else stacks.translate(0, 0, 1);

//		setupMatrix(stacks, portal);

//		RenderSystem.disableTexture();
//		RenderSystem.depthFunc(GL11.GL_ALWAYS);
//		RendererUtils.draw(stacks, 0, 0, portal.width, portal.height, 0);
//		RenderSystem.enableTexture();
//		stacks.pop();
		
		/* start active stencils */
		if (!Minecraft.getInstance().getFramebuffer().isStencilEnabled())
			Minecraft.getInstance().getFramebuffer().enableStencil();
		setupStencil(stacks, portal);
		/* end active stencils */

//		else if (portal.direction == Direction.EAST) stacks.translate(-1, -portal.height, 0);

//		infos.sort((i1, i2) -> {
//			return Double.compare(
//					((WorldRendererAccessor.RenderInformationContainerAccessor) i2).getRenderChunk().getPosition().add(8, 8, 8).distanceSq(pairPos),
//					((WorldRendererAccessor.RenderInformationContainerAccessor) i1).getRenderChunk().getPosition().add(8, 8, 8).distanceSq(pairPos)
//			);
//		});
		
		/* start sky background */
		stacks.push();
		// TODO: make this be one draw call instead of 5
		// this code is massive due to also needing to fill the depth buffer, due to which depth sorting method I'm using
		setupMatrix(stacks, portal);
		stacks.translate(-(portal.width * 2000) / 2f, -(portal.height * 2000) / 2f, 10);
		stacks.scale(portal.width * 2000, portal.height * 2000, 1);
		RenderSystem.depthFunc(GL11.GL_ALWAYS);
		int skyColor = portalTE.getWorld().getBiome(portalTE.getPos()).getFogColor();
		RenderSystem.disableTexture();
		{
			int ci = (skyColor >> 16) & 0xFF;
			int cb = (skyColor >> 8) & 0xFF;
			int cg = (skyColor) & 0xFF;
			RenderSystem.enableFog();
			RenderSystem.color4f(ci / 255f, cb / 255f, cg / 255f, 1);
		}
		RendererUtils.draw(stacks, 0, 0, portal.width, portal.height, 0);
		RendererUtils.draw(stacks, 0, 0, portal.width, portal.height, 20);
		RendererUtils.draw(stacks, 0, 0, portal.width, portal.height, 90);
		RendererUtils.draw(stacks, 0, 0, portal.width, portal.height, 190);
		RendererUtils.draw(stacks, 0, 0, portal.width, portal.height, 250);
		RenderSystem.enableTexture();
		stacks.pop();
		/* end sky background */
		
		/* start render sky */
		stacks.push();
		{
			ActiveRenderInfo info = Minecraft.getInstance().getRenderManager().info;
			double d0 = info.getProjectedView().x;
			double d1 = info.getProjectedView().y;
			boolean flag1 = Minecraft.getInstance().world.func_239132_a_().func_230493_a_(MathHelper.floor(d0), MathHelper.floor(d1)) || Minecraft.getInstance().ingameGUI.getBossOverlay().shouldCreateFog();
			RenderSystem.color4f(1, 1, 1, 1);
			FogRenderer.setupFog(info, FogRenderer.FogType.FOG_SKY, Minecraft.getInstance().gameRenderer.getFarPlaneDistance() - 20, flag1, partialTicks);
			Minecraft.getInstance().worldRenderer.renderSky(stacks, partialTicks);
		}
		stacks.pop();
		/* end render sky */
		
		/* start fix matrix */
		stacks.push();
		switch (portal.direction) {
			case NORTH:
				stacks.rotate(new Quaternion(0, 90, 0, true));
				stacks.translate(-1, 0, 0);
				break;
			case SOUTH:
				stacks.rotate(new Quaternion(0, -90, 0, true));
				stacks.translate(0, 0, -1);
				break;
			case WEST:
				stacks.rotate(new Quaternion(0, 180, 0, true));
				stacks.translate(-1, 0, -1);
				break;
			default:
				break;
		}
		/* end fix matrix */
		
		/* start setup matrix */
		stacks.rotate(pairStruct.direction.getRotation());
		stacks.rotate(new Quaternion(-90, 90, 0, true));
		if (pairStruct.direction == Direction.EAST) {
			stacks.translate(1, 0, -1);
		} else if (pairStruct.direction == Direction.NORTH) {
			stacks.translate(0, 0, 1);
			stacks.rotate(new Quaternion(0, 180, 0, true));
		} else if (pairStruct.direction == Direction.SOUTH) {
			stacks.translate(-1, 0, 0);
			stacks.rotate(new Quaternion(0, 180, 0, true));
		}
		
		/* start adjusting for UP portals */
		if (pairStruct.upDirection.equals(PortalStructure.UpDirection.UP)) {
			switch (pairStruct.direction) {
				case WEST:
					stacks.rotate(new Quaternion(0, 0, 90, true));
					stacks.translate(1, 0, 0);
					break;
				case SOUTH:
					stacks.rotate(new Quaternion(90, 0, 0, true));
					stacks.translate(0, 0, -1);
					break;
				case NORTH:
					stacks.rotate(new Quaternion(-90, 0, 0, true));
					stacks.translate(0, -1, 0);
					break;
				case EAST:
					stacks.rotate(new Quaternion(0, 0, -90, true));
					stacks.translate(0, -1, 0);
					break;
			}
		}
		/* end adjusting for UP portals */
		/* start adjusting for DOWN portals */
		if (pairStruct.upDirection.equals(PortalStructure.UpDirection.DOWN)) {
			switch (pairStruct.direction) {
				case WEST:
					stacks.rotate(new Quaternion(0, 180, 90, true));
					stacks.translate(1, -1, -1);
					break;
				case SOUTH:
					stacks.rotate(new Quaternion(90, 0, 180, true));
					stacks.translate(1, -1, -1);
					break;
				case NORTH:
					stacks.rotate(new Quaternion(-90, 0, 180, true));
					stacks.translate(1, 0, 0);
					break;
				case EAST:
					stacks.rotate(new Quaternion(0, 180, -90, true));
					stacks.translate(0, 0, -1);
					break;
			}
		}
		/* end adjusting for DOWN portals */
		/* end setup matrix */
		
		/* start render clouds */
		if (Minecraft.getInstance().gameSettings.cloudOption != CloudOption.OFF) {
			RenderSystem.enableDepthTest();
			Minecraft.getInstance().worldRenderer.renderClouds(stacks, partialTicks, pairPos.getX() + 0.5, pairPos.getY() + 0.5, pairPos.getZ() + 0.5);
			RenderSystem.depthFunc(GL20.GL_LEQUAL);
			Minecraft.getInstance().worldRenderer.renderClouds(stacks, partialTicks, pairPos.getX() + 0.5, pairPos.getY() + 0.5, pairPos.getZ() + 0.5);
			Minecraft.getInstance().worldRenderer.renderClouds(stacks, partialTicks, pairPos.getX() + 0.5, pairPos.getY() + 0.5, pairPos.getZ() + 0.5);
		}
		/* end render clouds */
		
		/* start render blocks */
		List<ChunkRenderDispatcher.ChunkRender> infos = new ObjectArrayList<>();
		{
			List<WorldRenderer.LocalRenderInformationContainer> winfos = Minecraft.getInstance().worldRenderer.renderInfos;
			for (WorldRenderer.LocalRenderInformationContainer winfo : winfos) {
				infos.add(winfo.renderChunk);
			}
		}
		
//		{
//			ActiveRenderInfo info = Minecraft.getInstance().getRenderManager().info;
//			Vector3d vector3d = info.getProjectedView();
//			int renderDistanceChunks = Minecraft.getInstance().worldRenderer.renderDistanceChunks;
//			ViewFrustum viewFrustum = Minecraft.getInstance().worldRenderer.viewFrustum;
//			int j = info.getBlockPos().getY() > 0 ? 248 : 8;
//			int k = MathHelper.floor(vector3d.x / 16.0D) * 16;
//			int l = MathHelper.floor(vector3d.z / 16.0D) * 16;
//			for (int i1 = -renderDistanceChunks; i1 <= renderDistanceChunks; ++i1) {
//				loopZ:
//				for (int j1 = -renderDistanceChunks; j1 <= renderDistanceChunks; ++j1) {
//					ChunkRenderDispatcher.ChunkRender chunkrenderdispatcher$chunkrender1 = viewFrustum.getRenderChunk(new BlockPos(k + (i1 << 4) + 8, j, l + (j1 << 4) + 8));
//					if (chunkrenderdispatcher$chunkrender1 == null) continue;
//					for (ChunkRenderDispatcher.ChunkRender render : infos)
//						if (render.getPosition().equals(chunkrenderdispatcher$chunkrender1.getPosition()))
//							continue loopZ;
//					infos.add(chunkrenderdispatcher$chunkrender1);
//				}
//			}
//		}
		
		/* start check portal direction */
		int xOff = 0;
		int zOff = 0;
		if (pairStruct.direction == Direction.WEST) xOff = -1;
		else if (pairStruct.direction == Direction.EAST) xOff = 1;
		else if (pairStruct.direction == Direction.NORTH) zOff = 1;
		else if (pairStruct.direction == Direction.SOUTH) zOff = -1;
		/* end check portal direction */
		
		{
			ActiveRenderInfo info = Minecraft.getInstance().getRenderManager().info;
			double d0 = info.getProjectedView().x;
			double d1 = info.getProjectedView().y;
			boolean flag1 = Minecraft.getInstance().world.func_239132_a_().func_230493_a_(MathHelper.floor(d0), MathHelper.floor(d1)) || Minecraft.getInstance().ingameGUI.getBossOverlay().shouldCreateFog();
			RenderSystem.color4f(1, 1, 1, 1);
			FogRenderer.setupFog(info, FogRenderer.FogType.FOG_TERRAIN, Math.max(Minecraft.getInstance().gameRenderer.getFarPlaneDistance() - 16.0F, 32.0F), flag1, partialTicks);
		}
		
		// neat thing, this does actually return the block render types in the order they should be rendered in
		for (RenderType blockRenderType : RenderType.getBlockRenderTypes()) {
//			blockRenderType.setupRenderState();
			
			loopInfos:
			for (ChunkRenderDispatcher.ChunkRender render : infos) {
				if (render.compiledChunk.get().isLayerEmpty(blockRenderType)) continue;
				for (TileEntity tileEntity : render.compiledChunk.get().getTileEntities()) {
					for (BlockPos position : pairStruct.positions) {
						if (tileEntity.getPos().equals(position)) {
							IChunk chunk = tileEntity.getWorld().getChunk(render.getPosition());
							/* start render chunk */
							BlockPos relPos = render.getPosition().subtract(pairPos);
							stacks.push();
							stacks.translate(relPos.getX(), relPos.getY(), relPos.getZ());
							
							BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
							
							IVertexBuilder builder = vboTool.getBuffer(blockRenderType);
							
							for (int x = 0; x < 16; x++) {
								for (int y = 0; y < 16; y++) {
									for (int z = 0; z < 16; z++) {
										if (xOff < 0) {
											if (render.getPosition().getX() + (x + xOff) >= pairPos.getX()) continue;
										} else if (xOff > 0) {
											if (render.getPosition().getX() + (x + xOff) <= pairPos.getX()) continue;
										} else if (zOff < 0) {
											if (render.getPosition().getZ() + (z - zOff) <= pairPos.getZ()) continue;
										} else if (zOff > 0) {
											if (render.getPosition().getZ() + (z - zOff) >= pairPos.getZ()) continue;
										}
										BlockPos pos = render.getPosition().add(x, y, z);
										BlockState state = chunk.getBlockState(pos);
										if (RenderTypeLookup.canRenderInLayer(state, blockRenderType)) {
											stacks.push();
											stacks.translate(x - 1, y, z);
											dispatcher.renderModel(
													state, pos, portalTE.getWorld(),
													stacks, builder,
//													stacks, bufferIn.getBuffer(RenderType.getSolid()),
													true, new Random(pos.toLong())
											);
											stacks.pop();
										}
									}
								}
							}
							
							
							vboTool.finish();
							
							stacks.pop();
							/* end render chunk */
							
							break loopInfos;
						}
					}
				}
			}
			
			loopInfos:
			for (ChunkRenderDispatcher.ChunkRender info : infos) {
//			for (WorldRenderer.LocalRenderInformationContainer info : infos) {
//				ChunkRenderDispatcher.ChunkRender render = info.renderChunk;
				ChunkRenderDispatcher.ChunkRender render = info;
				if (render.compiledChunk.get().isLayerEmpty(blockRenderType)) continue;
				// can't render the chunks the portal is in normally, cuz I can't have nice things I guess
				for (TileEntity tileEntity : render.compiledChunk.get().getTileEntities()) {
					for (BlockPos position : pairStruct.positions) {
						if (tileEntity.getPos().equals(position)) {
							IChunk chunk = tileEntity.getWorld().getChunk(render.getPosition());
							/* start render chunk */
							BlockPos relPos = render.getPosition().subtract(pairPos);
							stacks.push();
							stacks.translate(relPos.getX(), relPos.getY(), relPos.getZ());

							BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

							IVertexBuilder builder = vboTool.getBuffer(blockRenderType);

							for (int x = 0; x < 16; x++) {
								for (int y = 0; y < 16; y++) {
									for (int z = 0; z < 16; z++) {
										if (xOff < 0) {
											if (render.getPosition().getX() + (x + xOff) >= pairPos.getX()) continue;
										} else if (xOff > 0) {
											if (render.getPosition().getX() + (x + xOff) <= pairPos.getX()) continue;
										} else if (zOff < 0) {
											if (render.getPosition().getZ() + (z - zOff) <= pairPos.getZ()) continue;
										} else if (zOff > 0) {
											if (render.getPosition().getZ() + (z - zOff) >= pairPos.getZ()) continue;
										}
										BlockPos pos = render.getPosition().add(x, y, z);
										BlockState state = chunk.getBlockState(pos);
										if (RenderTypeLookup.canRenderInLayer(state, blockRenderType)) {
											stacks.push();
											stacks.translate(x - 1, y, z);
											dispatcher.renderModel(
													state, pos, portalTE.getWorld(),
													stacks, builder,
//													stacks, bufferIn.getBuffer(RenderType.getSolid()),
													true, new Random(pos.toLong())
											);
											stacks.pop();
										}
									}
								}
							}


							vboTool.finish();

							stacks.pop();
							/* end render chunk */
//
							continue loopInfos;
						}
					}
				}
				if (xOff < 0) {
					if (render.boundingBox.minX > pairPos.getX()) continue;
				} else if (xOff > 0) {
					if (render.boundingBox.maxX < pairPos.getX()) continue;
				} else if (zOff > 0) {
					if (render.boundingBox.minZ > pairPos.getZ()) continue;
				} else if (zOff < 0) {
					if (render.boundingBox.maxZ < pairPos.getZ()) continue;
				}
				// TODO: transparency sort
				VertexBuffer buffer = render.getVertexBuffer(blockRenderType);
				BlockPos relPos = render.getPosition().subtract(pairPos);
				
				stacks.push();
				stacks.translate(relPos.getX(), relPos.getY(), relPos.getZ());
				
				/* start matrix corrections */
				{
					Vector3i vec = pairStruct.direction.getDirectionVec();
					int scl = portal.width - 1;
					stacks.translate(vec.getZ() * -scl, vec.getY() * -scl, vec.getX() * -scl);
				}
				stacks.translate(-1, 0, 0);
				/* end matrix corrections */
				
				/* start draw */
				buffer.bindBuffer();
				blockRenderType.setupRenderState();
				DefaultVertexFormats.BLOCK.setupBufferState(0L); // TODO: reference blockVertexFormat on WorldRenderer class for sake of better compatibility
//				RenderSystem.enableDepthTest();
//				RenderSystem.depthFunc(GL11.GL_LESS);
				buffer.draw(stacks.getLast().getMatrix(), 7);
				stacks.pop();
				/* end draw */
			}
			
			/* start cleanup */
			VertexBuffer.unbindBuffer();
			RenderSystem.clearCurrentColor();
			DefaultVertexFormats.BLOCK.clearBufferState();
			blockRenderType.clearRenderState();
			/* end cleanup */
		}
		/* end render blocks */
		
		/* start render tile entities */
		if (recursionDepth <= 3) {
			recursionDepth++;
			for (ChunkRenderDispatcher.ChunkRender info : infos) {
//			for (WorldRenderer.LocalRenderInformationContainer info : infos) {
				ChunkRenderDispatcher.ChunkRender render = info;
				if (xOff < 0) {
					if (render.boundingBox.maxX < pairPos.getX() - xOff) continue;
				} else if (xOff > 0) {
					if (render.boundingBox.minX > pairPos.getX() - xOff) continue;
				} else if (zOff > 0) {
					if (render.boundingBox.maxZ > pairPos.getZ() - zOff) continue;
				} else if (zOff < 0) {
					if (render.boundingBox.minZ < pairPos.getZ() - zOff) continue;
				}
				stacks.push();
				
				/* start matrix corrections */
				{
					Vector3i vec = pairStruct.direction.getDirectionVec();
					int scl = portal.width - 1;
					stacks.translate(vec.getZ() * -scl, vec.getY() * -scl, vec.getX() * -scl);
				}
				stacks.translate(-1, 0, 0);
				/* end matrix corrections */
				
//				for (TileEntity te : info.renderChunk.compiledChunk.get().getTileEntities()) {
				for (TileEntity te : info.compiledChunk.get().getTileEntities()) {
					if (te instanceof PortalBlockTileEntity) continue;
					if (te != null) {
						TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(te);
						if (renderer != null) {
							stacks.push();
							stacks.translate(te.getPos().getX() - pairPos.getX(), te.getPos().getY() - pairPos.getY(), te.getPos().getZ() - pairPos.getZ());
							renderer.render(te, partialTicks, stacks, buffers.getBufferSource(),
									LightTexture.packLight(
											portalTE.getWorld().getLightFor(LightType.BLOCK, portalTE.getPos()),
											portalTE.getWorld().getLightFor(LightType.SKY, portalTE.getPos())
									), combinedOverlayIn
							);
							stacks.pop();
						}
					}
				}
				
				stacks.pop();
			}
			recursionDepth--;
		}
		/* end render tile entities */
		
		List<Entity> entities = portalTE.getWorld().getEntitiesWithinAABBExcludingEntity(
				null, new AxisAlignedBB(pairPos.add(-30, -30, -30), pairPos.add(30, 30, 30))
		);
		
		AxisAlignedBB renderBox = portalTE.getRenderBoundingBox();
		
		for (Entity entity : entities) {
			AxisAlignedBB boundingBox = entity.getRenderBoundingBox();
			if (xOff < 0) {
				if (boundingBox.maxX > pairPos.getX() + 1.5) continue;
			} else if (xOff > 0) {
				if (boundingBox.minX < pairPos.getX() - 0.5) continue;
			} else if (zOff > 0) {
				if (boundingBox.maxZ > pairPos.getZ() + 1.5) continue;
			} else if (zOff < 0) {
				if (boundingBox.minZ < pairPos.getZ() - 0.5) continue;
			}
			EntityRenderer<Entity> renderer = (EntityRenderer<Entity>) Minecraft.getInstance().getRenderManager().getRenderer(entity);
			if (entity.getRenderBoundingBox().intersects(renderBox)) {
				// TODO: render the entity through the portal on the other side
			}
			stacks.push();
			stacks.translate(
					MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getPosX()) - pairPos.getX() - 1f,
					MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getPosY()) - pairPos.getY(),
					MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getPosZ()) - pairPos.getZ()
			);
			Minecraft.getInstance().getRenderManager().renderEntityStatic(
					entity, 0, 0, 0,
					entity.getYaw(partialTicks), partialTicks, stacks,
					buffers.getBufferSource(), renderer.getPackedLight(entity, partialTicks)
			);
			stacks.pop();
//			renderer.render(
//					entity, entity.getYaw(partialTicks), partialTicks, stacks,
//					buffers.getBufferSource(), renderer.getPackedLight(entity, partialTicks)
//			);
		}
		
		buffers.getBufferSource().finish();
		
		/* start cleanup */
		RenderSystem.enableDepthTest();
		
		stacks.pop();
		
		clearStencil(stacks, portal);
		/* end cleanup */
		
		stacks.push();
		stacks.translate(-portalTE.getPos().getX(), -portalTE.getPos().getY(), -portalTE.getPos().getZ());
		//Made by me (Lorenzo)
		//since I have to yet even THINK about how to do the overlay, I decided to use the render box of the portal
		//and color it
		//I now am an epic renderer boy
		float[] f = new Color(portal.color).getRGBColorComponents(null);
		WorldRenderer.drawBoundingBox(
				stacks, bufferIn.getBuffer(RenderType.getLines()),
				renderBox, f[0], f[1], f[2], 1
		);
		stacks.pop();
	}
	
	public void setupMatrix(MatrixStack stack, PortalStructure struct) {
		stack.rotate(struct.direction.getRotation());
		stack.rotate(new Quaternion(90, 0, 0, true));
		if (struct.direction == Direction.WEST) stack.translate(0, -struct.height, 1);
		else if (struct.direction == Direction.EAST) stack.translate(-struct.width, -struct.height, 0);
		else if (struct.direction == Direction.NORTH) stack.translate(-struct.width, -struct.height, 1);
		else if (struct.direction == Direction.SOUTH) stack.translate(0, -struct.height, 0);
		
		if (struct.upDirection.equals(PortalStructure.UpDirection.UP)) {
			stack.rotate(new Quaternion(-90, 0, 0, true));
			stack.translate(0, 0, 1);
		}
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
		RenderSystem.disableFog();
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
