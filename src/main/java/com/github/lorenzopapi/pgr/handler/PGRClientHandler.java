package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.network.SKeyEventPacket;
import com.github.lorenzopapi.pgr.portal.structure.ChannelIndicator;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.Reference;
import com.github.lorenzopapi.pgr.util.RendererUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.event.TickEvent;

//TODO: change a few things to avoid getting destroyed by THE LAW because this is probably the most copy-pasted classes of the project
public class PGRClientHandler {
	public final ResourceLocation EMPTY_L = new ResourceLocation(Reference.MOD_ID, "textures/overlay/lempty.png");
	public final ResourceLocation EMPTY_R = new ResourceLocation(Reference.MOD_ID, "textures/overlay/rempty.png");
	public final ResourceLocation FULL_L = new ResourceLocation(Reference.MOD_ID, "textures/overlay/lfull.png");
	public final ResourceLocation FULL_R = new ResourceLocation(Reference.MOD_ID, "textures/overlay/rfull.png");
	public boolean zoomIn;
	public int zoomCounter = -1;
	public double zoomOriFov = -1.0F;
	public double zoomOriMouse = -1.0F;
	//public boolean handHack;
	public boolean holdingResetKey;
	public boolean hasResetPortal;

	public void onOverlayEvent(RenderBlockOverlayEvent e) {
		if (e.getOverlayType() == RenderBlockOverlayEvent.OverlayType.BLOCK) {
			for (PortalStructure s : Reference.serverEH.getPGRDataForWorld(e.getPlayer().world).portals) {
				if (s.behinds.contains(e.getBlockPos())) {
					e.setCanceled(true);
					break;
				}
			}
		}
	}

	public void onClickEvent(InputEvent.ClickInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.currentScreen == null && !mc.isGamePaused()) {
			ItemStack is = mc.player.getHeldItemMainhand();
			if (is.getItem() == PGRRegistry.PORTAL_GUN.get()) {
				event.setSwingHand(false);
			}
		}
	}

	public void onKeyEvent(InputEvent.KeyInputEvent event) {
		commonKeyEvent(event.getAction(), event.getKey());
	}

	public void onMouseEvent(InputEvent.MouseInputEvent event) {
		commonKeyEvent(event.getAction(), event.getButton());
	}

	public void onRenderTick(TickEvent.RenderTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (event.phase == TickEvent.Phase.START) {
			if (this.zoomCounter > -1)
				if (this.zoomIn) {
					mc.gameSettings.fov = this.zoomOriFov * (0.1F + 0.9F * (1.0F - (float) Math.sin(Math.toRadians((90.0F * MathHelper.clamp((this.zoomCounter + event.renderTickTime) / 5.0F, 0.0F, 1.0F))))));
					mc.gameSettings.mouseSensitivity = this.zoomOriMouse * (0.1F + 0.9F * (1.0F - (float) Math.sin(Math.toRadians((90.0F * MathHelper.clamp((this.zoomCounter + event.renderTickTime) / 5.0F, 0.0F, 1.0F))))));
				} else {
					if (this.zoomCounter == 0) {
						mc.gameSettings.fov = this.zoomOriFov;
						mc.gameSettings.mouseSensitivity = this.zoomOriMouse;
					} else {
						mc.gameSettings.fov = this.zoomOriFov * (0.1F + 0.9F * (1.0F - (float) Math.sin(Math.toRadians((90.0F * MathHelper.clamp((this.zoomCounter - event.renderTickTime) / 5.0F, 0.0F, 1.0F))))));
						mc.gameSettings.mouseSensitivity = this.zoomOriMouse * (0.1F + 0.9F * (1.0F - (float) Math.sin(Math.toRadians((90.0F * MathHelper.clamp((this.zoomCounter - event.renderTickTime) / 5.0F, 0.0F, 1.0F))))));
					}
				}
		} else if (mc.player != null && mc.gameSettings.getPointOfView().func_243192_a() && mc.currentScreen == null && PGRConfig.CLIENT.portalgunIndicatorSize.get() > 0 && !mc.gameSettings.hideGUI) {
			ItemStack is = mc.player.getHeldItemMainhand();
			boolean holdingPG = (is.getItem() == PGRRegistry.PORTAL_GUN.get());
			if (holdingPG && is.getTag() != null) {
				CompoundNBT tag = is.getTag();
				ChannelIndicator indicator = Reference.serverEH.getPortalChannelIndicator(tag.getString("uuid"), tag.getString("channelName"), mc.player.getEntityWorld().getDimensionKey());
				if (indicator.info.colorA != -1) {
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(770, 771);
					GlStateManager.alphaFunc(516, 0.005F);
					MainWindow window = Minecraft.getInstance().getMainWindow();
					double width = window.getScaledWidth();
					double height = window.getScaledHeight();
					double size = Math.min(width, height) * PGRConfig.CLIENT.portalgunIndicatorSize.get() / 100.0D;
					double posX = (width + (PGRConfig.CLIENT.portalgunIndicatorSize.get() / 20.) - size) / 2.0D;
					double posY = (height + (PGRConfig.CLIENT.portalgunIndicatorSize.get() / 20.) - size) / 2.0D;
					RendererUtils.drawTexture(new MatrixStack(), indicator.portalAPlaced ? this.FULL_L : this.EMPTY_L, posX, posY, size, size, 0, indicator.info.colorA);
					RendererUtils.drawTexture(new MatrixStack(), indicator.portalBPlaced ? this.FULL_R : this.EMPTY_R, posX, posY, size, size, 0, indicator.info.colorB);
					RendererUtils.setColorFromInt(0xFFFFFF);
					GlStateManager.alphaFunc(516, 0.1F);
					GlStateManager.disableBlend();
				}
			}
		}
	}

	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null) {
				ItemStack is = mc.player.getHeldItemMainhand();
				boolean holdingPG = (is.getItem() == PGRRegistry.PORTAL_GUN.get());
				if (this.zoomIn) {
					if (this.zoomCounter < 5 && !mc.isGamePaused())
						this.zoomCounter++;
					if (!holdingPG) {
						this.zoomIn = false;
						this.zoomCounter--;
					}
				} else if (this.zoomCounter > -1) {
					this.zoomCounter--;
					if (this.zoomCounter == -1) {
						this.zoomOriFov = -1.0F;
						this.zoomOriMouse = -1.0F;
					}
				}
			}
		}
	}

	public void onClientDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		Minecraft.getInstance().enqueue(() -> {
			this.zoomIn = false;
			this.zoomCounter = -1;
			if (this.zoomOriFov > 0.0F) {
				Minecraft.getInstance().gameSettings.fov = this.zoomOriFov;
				Minecraft.getInstance().gameSettings.mouseSensitivity = this.zoomOriMouse;
			}
			this.zoomOriFov = -1.0F;
			this.zoomOriMouse = -1.0F;
			this.holdingResetKey = false;
			this.hasResetPortal = false;
		});
	}

	private void commonKeyEvent(int action, int key) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.currentScreen == null && !mc.isGamePaused()) {
			boolean holdingPG = mc.player.getHeldItemMainhand().getItem() == PGRRegistry.PORTAL_GUN.get();
			//Zoom code
			if (action == 1) {
				if (key == PGRConfig.KeyBinds.keyZoom.getKey().getKeyCode()) {
					if (holdingPG) {
						this.zoomIn = !this.zoomIn;
						if (this.zoomIn && this.zoomOriFov == -1.0F) {
							this.zoomOriFov = mc.gameSettings.fov;
							this.zoomOriMouse = mc.gameSettings.mouseSensitivity;
						}
					}
					// Reset check
				} else if (key == PGRConfig.KeyBinds.keyReset.getKey().getKeyCode()) {
					this.holdingResetKey = holdingPG;
					//Grab check
				} else if (key == PGRConfig.KeyBinds.keyGrab.getKey().getKeyCode()) {
					PGRNetworkHandler.HANDLER.sendToServer(new SKeyEventPacket(5));
				} else if (holdingPG) {
					//Portal shooting
					if (key == mc.gameSettings.keyBindAttack.getKey().getKeyCode()) {
						if (this.holdingResetKey) {
							this.hasResetPortal = true;
							PGRNetworkHandler.HANDLER.sendToServer(new SKeyEventPacket(1));
						} else {
							PGRNetworkHandler.HANDLER.sendToServer(new SKeyEventPacket(3));
						}
					} else if (key == mc.gameSettings.keyBindUseItem.getKey().getKeyCode()) {
						if (this.holdingResetKey) {
							this.hasResetPortal = true;
							PGRNetworkHandler.HANDLER.sendToServer(new SKeyEventPacket(2));
						} else {
							PGRNetworkHandler.HANDLER.sendToServer(new SKeyEventPacket(4));
						}
					}
				}
			} else if ((key == PGRConfig.KeyBinds.keyReset.getKey().getKeyCode()) && holdingPG) {
				if (this.holdingResetKey && !this.hasResetPortal) {
					PGRNetworkHandler.HANDLER.sendToServer(new SKeyEventPacket(0));
				}
				this.holdingResetKey = false;
				this.hasResetPortal = false;
			}
		}
	}

//	public void onHandRender(RenderHandEvent event) {
//		Minecraft mc = Minecraft.getInstance();
//		ItemStack is = mc.player.getHeldItemMainhand();
//		boolean holdingPG = (is.getItem() == PGRRegistry.PORTAL_GUN.get());
//		if (holdingPG && this.zoomCounter >= 0)
//			this.handHack = true;
//	}
//
//	public void onFovModifierEvent(EntityViewRenderEvent.FOVModifier event) {
//		if (this.handHack && event.getFOV() == 70.0F) {
//			this.handHack = false;
//			event.setFOV((Minecraft.getInstance()).gameSettings.fov);
//		}
//	}
}