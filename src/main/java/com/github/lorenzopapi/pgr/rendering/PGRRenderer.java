package com.github.lorenzopapi.pgr.rendering;

import com.github.lorenzopapi.pgr.portal.block.PortalBlockTileEntity;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.rendering.shader.PGRShaders;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import com.github.lorenzopapi.pgr.util.RendererUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.settings.CloudOption;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.IChunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.util.ArrayList;
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
	public void render(PortalBlockTileEntity portalTE, float partialTicks, MatrixStack stack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// TODO: check if the player is in a position where they could possibly see the portal
		
		PGRShaders.bindProgram();
		Matrix4f matrix4f = Minecraft.getInstance().gameRenderer.getProjectionMatrix(
				Minecraft.getInstance().gameRenderer.getActiveRenderInfo(),
				partialTicks,  true
		);
		PGRShaders.setMatrix(matrix4f);
		PGRShaders.unbind();
		
		PortalStructure portal = PGRUtils.findPortalByPosition(portalTE.getWorld(), portalTE.getPos());
		// I removed a null check here, which was always false, because the positions cannot be null since it's defined as an empty list from the beginning
		if (portal == null || portal.positions.isEmpty()) return;
		
		if (!portal.positions.get(0).equals(portalTE.getPos())) return;
		PortalStructure pair = portal.pair;
		if (pair == null) {
			stack.push();
			setupMatrix(stack, portal);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			for (int w = 0; w < portal.width; w++) {
				for (int h = 0; h < portal.height; h++) {
					RendererUtils.drawTexture(
							stack, new ResourceLocation("minecraft:null"),
							w, h, 1, 1, 0, portal.color
					);
				}
			}
			stack.pop();
			return;
		}
		BlockPos pairPos = pair.positions.get(0);

//		stack.push();
//		if (portal.positions.get(0).getY() == portal.positions.get(1).getY())
//			stack.rotate(new Quaternion(90, 0, 0, true));
//		else stack.translate(0, 0, 1);

//		setupMatrix(stack, portal);

//		RenderSystem.disableTexture();
//		RenderSystem.depthFunc(GL11.GL_ALWAYS);
//		RendererUtils.draw(stack, 0, 0, portal.width, portal.height, 0);
//		RenderSystem.enableTexture();
//		stack.pop();
		
		/* start active stencils */
		if (!Minecraft.getInstance().getFramebuffer().isStencilEnabled())
			Minecraft.getInstance().getFramebuffer().enableStencil();
		
		
		stack.push();
		setupMatrix(stack, portal);
		RenderSystem.enableDepthTest();
		RendererUtils.drawCircleColor(stack, 0, 0, portal.width, portal.height, -0.0005, (portal.color >> 16 & 255) / 255.0F, (portal.color >> 8 & 255) / 255.0F, (portal.color & 255) / 255.0F, 1);
		RendererUtils.drawCircleColor(stack, 0, 0, portal.width, portal.height, -0.0001, (portal.color >> 16 & 255) / 255.0F, (portal.color >> 8 & 255) / 255.0F, (portal.color & 255) / 255.0F, 1);
		stack.pop();
		setupStencil(stack, portal);
		/* end active stencils */

//		else if (portal.direction == Direction.EAST) stack.translate(-1, -portal.height, 0);

//		infos.sort((i1, i2) -> {
//			return Double.compare(
//					((WorldRendererAccessor.RenderInformationContainerAccessor) i2).getRenderChunk().getPosition().add(8, 8, 8).distanceSq(pairPos),
//					((WorldRendererAccessor.RenderInformationContainerAccessor) i1).getRenderChunk().getPosition().add(8, 8, 8).distanceSq(pairPos)
//			);
//		});
		
		/* start sky background */
		stack.push();
		// TODO: make this be one draw call instead of 5
		// this code is massive due to also needing to fill the depth buffer, due to which depth sorting method I'm using
		setupMatrix(stack, portal);
		stack.translate(-(portal.width * 2000) / 2f, -(portal.height * 2000) / 2f, 10);
		stack.scale(portal.width * 2000, portal.height * 2000, 1);
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
		RendererUtils.draw(stack, 0, 0, portal.width, portal.height, 0);
		RendererUtils.draw(stack, 0, 0, portal.width, portal.height, 20);
		RendererUtils.draw(stack, 0, 0, portal.width, portal.height, 90);
		RendererUtils.draw(stack, 0, 0, portal.width, portal.height, 190);
		RendererUtils.draw(stack, 0, 0, portal.width, portal.height, 250);
		RenderSystem.enableTexture();
		stack.pop();
		/* end sky background */
		
		/* start render sky */
		stack.push();
		{
			ActiveRenderInfo info = Minecraft.getInstance().getRenderManager().info;
			double d0 = info.getProjectedView().x;
			double d1 = info.getProjectedView().y;
			boolean flag1 = Minecraft.getInstance().world.func_239132_a_().func_230493_a_(MathHelper.floor(d0), MathHelper.floor(d1)) || Minecraft.getInstance().ingameGUI.getBossOverlay().shouldCreateFog();
			RenderSystem.color4f(1, 1, 1, 1);
			FogRenderer.setupFog(info, FogRenderer.FogType.FOG_SKY, Minecraft.getInstance().gameRenderer.getFarPlaneDistance() - 20, flag1, partialTicks);
			Minecraft.getInstance().worldRenderer.renderSky(stack, partialTicks);
		}
		stack.pop();
		/* end render sky */
		
		/* start fix matrix */
		stack.push();
		switch (portal.direction) {
			case NORTH:
				stack.rotate(new Quaternion(0, 90, 0, true));
				stack.translate(-1, 0, 0);
				break;
			case SOUTH:
				stack.rotate(new Quaternion(0, -90, 0, true));
				stack.translate(0, 0, -1);
				break;
			case WEST:
				stack.rotate(new Quaternion(0, 180, 0, true));
				stack.translate(-1, 0, -1);
				break;
			default:
				break;
		}
		/* end fix matrix */
		
		/* start setup matrix */
		stack.rotate(pair.direction.getRotation());
		stack.rotate(new Quaternion(-90, 90, 0, true));
		if (pair.direction == Direction.EAST) {
			stack.translate(1, 0, -1);
		} else if (pair.direction == Direction.NORTH) {
			stack.translate(0, 0, 1);
			stack.rotate(new Quaternion(0, 180, 0, true));
		} else if (pair.direction == Direction.SOUTH) {
			stack.translate(-1, 0, 0);
			stack.rotate(new Quaternion(0, 180, 0, true));
		}
		
		/* start adjusting for UP portals */
		if (pair.upDirection.equals(PortalStructure.UpDirection.UP)) {
			switch (pair.direction) {
				case WEST:
					stack.rotate(new Quaternion(0, 0, 90, true));
					stack.translate(1, 0, 0);
					break;
				case SOUTH:
					stack.rotate(new Quaternion(90, 0, 0, true));
					stack.translate(0, 0, -1);
					break;
				case NORTH:
					stack.rotate(new Quaternion(-90, 0, 0, true));
					stack.translate(0, -1, 0);
					break;
				case EAST:
					stack.rotate(new Quaternion(0, 0, -90, true));
					stack.translate(0, -1, 0);
					break;
			}
		}
		/* end adjusting for UP portals */
		/* start adjusting for DOWN portals */
		if (pair.upDirection.equals(PortalStructure.UpDirection.DOWN)) {
			switch (pair.direction) {
				case WEST:
					stack.rotate(new Quaternion(0, 180, 90, true));
					stack.translate(1, -1, -1);
					break;
				case SOUTH:
					stack.rotate(new Quaternion(90, 0, 180, true));
					stack.translate(1, -1, -1);
					break;
				case NORTH:
					stack.rotate(new Quaternion(-90, 0, 180, true));
					stack.translate(1, 0, 0);
					break;
				case EAST:
					stack.rotate(new Quaternion(0, 180, -90, true));
					stack.translate(0, 0, -1);
					break;
			}
		}
		/* end adjusting for DOWN portals */
		/* end setup matrix */
		
		/* start render clouds */
		if (Minecraft.getInstance().gameSettings.cloudOption != CloudOption.OFF) {
			RenderSystem.enableDepthTest();
			Minecraft.getInstance().worldRenderer.renderClouds(stack, partialTicks, pairPos.getX() + 0.5, pairPos.getY() + 0.5, pairPos.getZ() + 0.5);
			RenderSystem.depthFunc(GL20.GL_LEQUAL);
			Minecraft.getInstance().worldRenderer.renderClouds(stack, partialTicks, pairPos.getX() + 0.5, pairPos.getY() + 0.5, pairPos.getZ() + 0.5);
			Minecraft.getInstance().worldRenderer.renderClouds(stack, partialTicks, pairPos.getX() + 0.5, pairPos.getY() + 0.5, pairPos.getZ() + 0.5);
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
		int xOff = pair.direction.getXOffset();
		int zOff = -pair.direction.getZOffset();
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
					for (BlockPos position : pair.positions) {
						if (tileEntity.getPos().equals(position)) {
							IChunk chunk = tileEntity.getWorld().getChunk(render.getPosition());
							/* start render chunk */
							BlockPos relPos = render.getPosition().subtract(pairPos);
							stack.push();
							stack.translate(relPos.getX(), relPos.getY(), relPos.getZ());
							
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
											stack.push();
											stack.translate(x - 1, y, z);
											dispatcher.renderModel(
													state, pos, portalTE.getWorld(),
													stack, builder,
//													stack, bufferIn.getBuffer(RenderType.getSolid()),
													true, new Random(pos.toLong())
											);
											stack.pop();
										}
									}
								}
							}
							
//							PGRShaders.bindProgram();
							vboTool.finish();
//							PGRShaders.unbind();
							
							stack.pop();
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
					for (BlockPos position : pair.positions) {
						if (tileEntity.getPos().equals(position)) {
//							IChunk chunk = tileEntity.getWorld().getChunk(render.getPosition());
//							/* start render chunk */
//							BlockPos relPos = render.getPosition().subtract(pairPos);
//							stack.push();
//							stack.translate(relPos.getX(), relPos.getY(), relPos.getZ());
//
//							BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//
//							IVertexBuilder builder = vboTool.getBuffer(blockRenderType);
//
//							for (int x = 0; x < 16; x++) {
//								for (int y = 0; y < 16; y++) {
//									for (int z = 0; z < 16; z++) {
//										if (xOff < 0) {
//											if (render.getPosition().getX() + (x + xOff) >= pairPos.getX()) continue;
//										} else if (xOff > 0) {
//											if (render.getPosition().getX() + (x + xOff) <= pairPos.getX()) continue;
//										} else if (zOff < 0) {
//											if (render.getPosition().getZ() + (z - zOff) <= pairPos.getZ()) continue;
//										} else if (zOff > 0) {
//											if (render.getPosition().getZ() + (z - zOff) >= pairPos.getZ()) continue;
//										}
//										BlockPos pos = render.getPosition().add(x, y, z);
//										BlockState state = chunk.getBlockState(pos);
//										if (RenderTypeLookup.canRenderInLayer(state, blockRenderType)) {
//											stack.push();
//											stack.translate(x - 1, y, z);
//											dispatcher.renderModel(
//													state, pos, portalTE.getWorld(),
//													stack, builder,
////													stack, bufferIn.getBuffer(RenderType.getSolid()),
//													true, new Random(pos.toLong())
//											);
//											stack.pop();
//										}
//									}
//								}
//							}
//
//
//							vboTool.finish();
//
//							stack.pop();
//							/* end render chunk */
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
				
				stack.push();
				stack.translate(relPos.getX(), relPos.getY(), relPos.getZ());
				
				/* start matrix corrections */
//				{
//					Vector3i vec = pair.direction.getDirectionVec();
//					int scl = portal.width - 1;
//					stack.translate(vec.getZ() * -scl, vec.getY() * -scl, vec.getX() * -scl);
//				}
				stack.translate(-1, 0, 0);
				/* end matrix corrections */
				
				/* start draw */
//				PGRShaders.bindProgram();
				buffer.bindBuffer();
				blockRenderType.setupRenderState();
				DefaultVertexFormats.BLOCK.setupBufferState(0L); // TODO: reference blockVertexFormat on WorldRenderer class for sake of better compatibility
//				RenderSystem.enableDepthTest();
//				RenderSystem.depthFunc(GL11.GL_LESS);
				buffer.draw(stack.getLast().getMatrix(), 7);
//				PGRShaders.unbind();
				stack.pop();
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
				stack.push();
				
				/* start matrix corrections */
//				{
//					Vector3i vec = pair.direction.getDirectionVec();
//					int scl = portal.width - 1;
//					stack.translate(vec.getZ() * -scl, vec.getY() * -scl, vec.getX() * -scl);
//				}
				stack.translate(-1, 0, 0);
				/* end matrix corrections */
				
//				for (TileEntity te : info.renderChunk.compiledChunk.get().getTileEntities()) {
				for (TileEntity te : info.compiledChunk.get().getTileEntities()) {
					if (te instanceof PortalBlockTileEntity) continue;
					if (te != null) {
						TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(te);
						if (renderer != null) {
							stack.push();
							stack.translate(te.getPos().getX() - pairPos.getX(), te.getPos().getY() - pairPos.getY(), te.getPos().getZ() - pairPos.getZ());
							renderer.render(te, partialTicks, stack, buffers.getBufferSource(),
									LightTexture.packLight(
											portalTE.getWorld().getLightFor(LightType.BLOCK, portalTE.getPos()),
											portalTE.getWorld().getLightFor(LightType.SKY, portalTE.getPos())
									), combinedOverlayIn
							);
							stack.pop();
						}
					}
				}
				
				stack.pop();
			}
			recursionDepth--;
		}
		/* end render tile entities */
		
		AxisAlignedBB renderBox = portalTE.getRenderBoundingBox();
		{
			List<Entity> entities = portalTE.getWorld().getEntitiesWithinAABBExcludingEntity(
					null, new AxisAlignedBB(portalTE.getPos().add(-30, -30, -30), pairPos.add(30, 30, 30))
			);
			for (Entity entity : entities) {
				if (entity.getRenderBoundingBox().intersects(renderBox)) {
					// TODO: render the entity through the portal on the other side
					if (entity == Minecraft.getInstance().getRenderViewEntity()) {
						if (!Minecraft.getInstance().getRenderManager().info.isThirdPerson()) continue;
					}
					EntityRenderer<Entity> renderer = (EntityRenderer<Entity>) Minecraft.getInstance().getRenderManager().getRenderer(entity);
					stack.push();
					stack.translate(
							MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getPosX()) - pairPos.getX() - 1f,
							MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getPosY()) - pairPos.getY(),
							MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getPosZ()) - pairPos.getZ()
					);
					stack.translate(xOff, 0, -zOff);
					Minecraft.getInstance().getRenderManager().renderEntityStatic(
							entity, 0, 0, 0,
							entity.getYaw(partialTicks), partialTicks, stack,
							buffers.getBufferSource(), renderer.getPackedLight(entity, partialTicks)
					);
					stack.pop();
				}
			}
		}
		
		List<Entity> entities = portalTE.getWorld().getEntitiesWithinAABBExcludingEntity(
				null, new AxisAlignedBB(pairPos.add(-30, -30, -30), pairPos.add(30, 30, 30))
		);
		
		ArrayList<Entity> toRenderOnTheOtherSide = new ArrayList<>();
		TileEntity pairTE = portalTE.getWorld().getTileEntity(pairPos);
		if (pairTE != null) renderBox = pairTE.getRenderBoundingBox();
		
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
			if (entity.getRenderBoundingBox().intersects(renderBox)) {
				toRenderOnTheOtherSide.add(entity);
			}
			EntityRenderer<Entity> renderer = (EntityRenderer<Entity>) Minecraft.getInstance().getRenderManager().getRenderer(entity);
			stack.push();
			stack.translate(
					MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getPosX()) - pairPos.getX() - 1f,
					MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getPosY()) - pairPos.getY(),
					MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getPosZ()) - pairPos.getZ()
			);
			Minecraft.getInstance().getRenderManager().renderEntityStatic(
					entity, 0, 0, 0,
					entity.getYaw(partialTicks), partialTicks, stack,
					buffers.getBufferSource(), renderer.getPackedLight(entity, partialTicks)
			);
			stack.pop();
//			renderer.render(
//					entity, entity.getYaw(partialTicks), partialTicks, stack,
//					buffers.getBufferSource(), renderer.getPackedLight(entity, partialTicks)
//			);
		}
		
		buffers.getBufferSource().finish();
		
		/* start cleanup */
		RenderSystem.enableDepthTest();
		
		stack.pop();
		
		clearStencil(stack, portal);
		/* end cleanup */
		
		/* start fix matrix */
		stack.push();
		switch (portal.direction) {
			case NORTH:
				stack.rotate(new Quaternion(0, 90, 0, true));
				stack.translate(-1, 0, 0);
				break;
			case SOUTH:
				stack.rotate(new Quaternion(0, -90, 0, true));
				stack.translate(0, 0, -1);
				break;
			case WEST:
				stack.rotate(new Quaternion(0, 180, 0, true));
				stack.translate(-1, 0, -1);
				break;
			default:
				break;
		}
		/* end fix matrix */
		
		/* start setup matrix */
		stack.rotate(pair.direction.getRotation());
		stack.rotate(new Quaternion(-90, 90, 0, true));
		if (pair.direction == Direction.EAST) {
			stack.translate(1, 0, -1);
		} else if (pair.direction == Direction.NORTH) {
			stack.translate(0, 0, 1);
			stack.rotate(new Quaternion(0, 180, 0, true));
		} else if (pair.direction == Direction.SOUTH) {
			stack.translate(-1, 0, 0);
			stack.rotate(new Quaternion(0, 180, 0, true));
		}
		
		/* start adjusting for UP portals */
		if (pair.upDirection.equals(PortalStructure.UpDirection.UP)) {
			switch (pair.direction) {
				case WEST:
					stack.rotate(new Quaternion(0, 0, 90, true));
					stack.translate(1, 0, 0);
					break;
				case SOUTH:
					stack.rotate(new Quaternion(90, 0, 0, true));
					stack.translate(0, 0, -1);
					break;
				case NORTH:
					stack.rotate(new Quaternion(-90, 0, 0, true));
					stack.translate(0, -1, 0);
					break;
				case EAST:
					stack.rotate(new Quaternion(0, 0, -90, true));
					stack.translate(0, -1, 0);
					break;
			}
		}
		/* end adjusting for UP portals */
		/* start adjusting for DOWN portals */
		if (pair.upDirection.equals(PortalStructure.UpDirection.DOWN)) {
			switch (pair.direction) {
				case WEST:
					stack.rotate(new Quaternion(0, 180, 90, true));
					stack.translate(1, -1, -1);
					break;
				case SOUTH:
					stack.rotate(new Quaternion(90, 0, 180, true));
					stack.translate(1, -1, -1);
					break;
				case NORTH:
					stack.rotate(new Quaternion(-90, 0, 180, true));
					stack.translate(1, 0, 0);
					break;
				case EAST:
					stack.rotate(new Quaternion(0, 180, -90, true));
					stack.translate(0, 0, -1);
					break;
			}
		}
		/* end adjusting for DOWN portals */
		/* end setup matrix */
		for (Entity entity : toRenderOnTheOtherSide) {
			EntityRenderer<Entity> renderer = (EntityRenderer<Entity>) Minecraft.getInstance().getRenderManager().getRenderer(entity);
			stack.push();
			stack.translate(
					MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getPosX()),
					MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getPosY()),
					MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getPosZ())
			);
			stack.translate(
					-pairPos.getX() + ((xOff <= 0) ? -1 : -2),
					-pairPos.getY(),
					-pairPos.getZ()
			);
//			stack.translate(
////					-pairPos.getX(),
////					0,
//					-(portalTE.getPos().getX() - pairPos.getX()),
//					-(portalTE.getPos().getY() - pairPos.getY() + 1),
//					-((pairPos.getZ() + 1) - portalTE.getPos().getZ())
////					-pairPos.getZ()
//			);
//			stack.translate(
////					pairPos.getX() - portalTE.getPos().getX(),
//					0,
//					1,
//					0
////					pairPos.getZ() - portalTE.getPos().getZ()
//			);
			Minecraft.getInstance().getRenderManager().renderEntityStatic(
					entity, 0, 0, 0,
					entity.getYaw(partialTicks), partialTicks, stack,
					buffers.getBufferSource(), renderer.getPackedLight(entity, partialTicks)
			);
			stack.pop();
		}
		stack.pop();
		buffers.getBufferSource().finish();
		
//		stack.push();
//		stack.translate(-portalTE.getPos().getX(), -portalTE.getPos().getY(), -portalTE.getPos().getZ());
//		WorldRenderer.drawBoundingBox(
//				stack, bufferIn.getBuffer(RenderType.getLines()),
//				renderBox, (portal.color >> 16 & 255) / 255.0F, (portal.color >> 8 & 255) / 255.0F, (portal.color & 255) / 255.0F, 1
//		);
//		stack.pop();
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
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		// TODO: color this as the fog color of the portal's pair
		RenderSystem.color4f(0, 0, 0, 1);
		RendererUtils.drawCircle(stack, 0.1f, 0.1f, structure.width - 0.2f, structure.height - 0.2f, -0.001f);
		RenderSystem.enableTexture();
		stack.pop();
		
		// Only pass stencil test if equal to 1(So only if rendered before)
		GL11.glStencilMask(0x00);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
	}
	
	public void setupBigStencil(MatrixStack stack, PortalStructure structure) {
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		
		// Always write to stencil buffer
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		
		RenderSystem.enableDepthTest();
		stack.push();
		RenderSystem.disableTexture();
		RenderSystem.disableFog();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		// TODO: color this as the fog color of the portal's pair
		RenderSystem.color4f(0, 0, 0, 1);
		RendererUtils.draw(stack, -10, -10, structure.width + 10, structure.height + 10, -0.005f);
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
		RendererUtils.drawCircle(stack, 0.1f, 0.1f, structure.width - 0.2f, structure.height - 0.2f, -0.005f);
		stack.pop();
		
		//Set things back
		GL11.glColorMask(true, true, true, true);
	}
	
	public void clearBigStencil(MatrixStack stack, PortalStructure structure) {
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		
		GL11.glColorMask(false, false, false, false);
		stack.push();
		setupMatrix(stack, structure);
		RendererUtils.draw(stack, -10, -10, structure.width + 10, structure.height + 10, -0.001f);
		stack.pop();
		
		//Set things back
		GL11.glColorMask(true, true, true, true);
	}
}
