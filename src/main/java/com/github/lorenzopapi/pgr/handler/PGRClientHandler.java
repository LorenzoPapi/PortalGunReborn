package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.network.PGRMessageHandler;
import com.github.lorenzopapi.pgr.network.message.KeyEventMessage;
import com.github.lorenzopapi.pgr.portal.ChannelIndicator;
import com.github.lorenzopapi.pgr.util.Reference;
import com.github.lorenzopapi.pgr.util.RendererHelper;
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
import net.minecraftforge.event.TickEvent;

public class PGRClientHandler {
	public final ResourceLocation texEmptyL = new ResourceLocation(Reference.MODID, "textures/overlay/lempty.png");
	public final ResourceLocation texEmptyR = new ResourceLocation(Reference.MODID, "textures/overlay/rempty.png");
	public final ResourceLocation texFullL = new ResourceLocation(Reference.MODID, "textures/overlay/lfull.png");
	public final ResourceLocation texFullR = new ResourceLocation(Reference.MODID, "textures/overlay/rfull.png");
	public boolean zoom;
	public int zoomCounter = -1;
	public double zoomOriFov = -1.0F;
	public double zoomOriMouse = -1.0F;
	//public boolean handHack;
	public boolean holdingResetKey;
	public boolean hasResetPortal;

	private void commonKeyEvent(int action, int key, boolean isHoldingPortalGun, Minecraft mc) {
		//Zoom code
		if (action == 1) {
			if (key == PGRConfig.KeyBinds.keyZoom.getKey().getKeyCode()) {
				if (isHoldingPortalGun) {
					this.zoom = !this.zoom;
					if (this.zoom && this.zoomOriFov == -1.0F) {
						this.zoomOriFov = mc.gameSettings.fov;
						this.zoomOriMouse = mc.gameSettings.mouseSensitivity;
					}
				}
				// Reset check
			} else if (key == PGRConfig.KeyBinds.keyReset.getKey().getKeyCode()) {
				this.holdingResetKey = isHoldingPortalGun;
				//Grab check
			} else if (key == PGRConfig.KeyBinds.keyGrab.getKey().getKeyCode()) {
				PGRMessageHandler.HANDLER.sendToServer(new KeyEventMessage(5));
			} else if (isHoldingPortalGun) {
				//Portal shooting
				if (key == mc.gameSettings.keyBindAttack.getKey().getKeyCode()) {
					if (this.holdingResetKey) {
						this.hasResetPortal = true;
						PGRMessageHandler.HANDLER.sendToServer(new KeyEventMessage(1));
					} else {
						PGRMessageHandler.HANDLER.sendToServer(new KeyEventMessage(3));
					}
				} else if (key == mc.gameSettings.keyBindUseItem.getKey().getKeyCode()) {
					if (this.holdingResetKey) {
						this.hasResetPortal = true;
						PGRMessageHandler.HANDLER.sendToServer(new KeyEventMessage(2));
					} else {
						PGRMessageHandler.HANDLER.sendToServer(new KeyEventMessage(4));
					}
				}
			}
		} else if ((key == PGRConfig.KeyBinds.keyReset.getKey().getKeyCode()) && isHoldingPortalGun) {
			if (this.holdingResetKey && !this.hasResetPortal) {
				PGRMessageHandler.HANDLER.sendToServer(new KeyEventMessage(0));
			}
			this.holdingResetKey = false;
			this.hasResetPortal = false;
		}
	}

	public void onKeyEvent(InputEvent.KeyInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.currentScreen == null && !mc.isGamePaused()) {
			ItemStack is = mc.player.getHeldItemMainhand();
			boolean isHoldingPortalGun = (is.getItem() == PGRRegistry.PORTAL_GUN.get());
			int key = event.getKey();
			commonKeyEvent(event.getAction(), key, isHoldingPortalGun, mc);
		}
	}

	public void onMouseEvent(InputEvent.MouseInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.currentScreen == null && !mc.isGamePaused()) {
			ItemStack is = mc.player.getHeldItemMainhand();
			boolean isHoldingPortalGun = (is.getItem() == PGRRegistry.PORTAL_GUN.get());
			int button = event.getButton();
			commonKeyEvent(event.getAction(), button, isHoldingPortalGun, mc);
		}
	}

	public void onRenderTick(TickEvent.RenderTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (event.phase == TickEvent.Phase.START) {
			if (this.zoomCounter > -1)
				if (this.zoom) {
					mc.gameSettings.fov = this.zoomOriFov * (0.1F + 0.9F * (1.0F - (float)Math.sin(Math.toRadians((90.0F * MathHelper.clamp((this.zoomCounter + event.renderTickTime) / 5.0F, 0.0F, 1.0F))))));
					mc.gameSettings.mouseSensitivity = this.zoomOriMouse * (0.1F + 0.9F * (1.0F - (float)Math.sin(Math.toRadians((90.0F * MathHelper.clamp((this.zoomCounter + event.renderTickTime) / 5.0F, 0.0F, 1.0F))))));
				} else {
					if (this.zoomCounter == 0) {
						mc.gameSettings.fov = this.zoomOriFov;
						mc.gameSettings.mouseSensitivity = this.zoomOriMouse;
					} else {
						mc.gameSettings.fov = this.zoomOriFov * (0.1F + 0.9F * (1.0F - (float)Math.sin(Math.toRadians((90.0F * MathHelper.clamp((this.zoomCounter - event.renderTickTime) / 5.0F, 0.0F, 1.0F))))));
						mc.gameSettings.mouseSensitivity = this.zoomOriMouse * (0.1F + 0.9F * (1.0F - (float)Math.sin(Math.toRadians((90.0F * MathHelper.clamp((this.zoomCounter - event.renderTickTime) / 5.0F, 0.0F, 1.0F))))));
					}
				}
		} else if (mc.player != null && mc.currentScreen == null && PGRConfig.CLIENT.portalgunIndicatorSize.get() > 0 && !mc.gameSettings.hideGUI) {
			ItemStack is = mc.player.getHeldItemMainhand();
			boolean isHoldingPortalGun = (is.getItem() == PGRRegistry.PORTAL_GUN.get());
			if (isHoldingPortalGun && is.getTag() != null) { // && !GrabHandler.hasHandlerType((EntityLivingBase)mc.player, Side.CLIENT, null)
				CompoundNBT tag = is.getTag();
				ChannelIndicator indicator = Reference.serverEH.getPortalChannelIndicator(tag.getString("uuid"), tag.getString("channelName"), mc.player.getEntityWorld().getDimensionKey());
				if (indicator.info.colorA != -1) {
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(770, 771);
					GlStateManager.alphaFunc(516, 0.005F);
					MainWindow reso = Minecraft.getInstance().getMainWindow();
					double width = reso.getScaledWidth();
					double height = reso.getScaledHeight();
					double size = Math.min(width, height) * PGRConfig.CLIENT.portalgunIndicatorSize.get() / 100.0D;
					double posX = (width + (PGRConfig.CLIENT.portalgunIndicatorSize.get() / 20.) - size) / 2.0D;
					double posY = (height + (PGRConfig.CLIENT.portalgunIndicatorSize.get() / 20.) - size) / 2.0D;
					RendererHelper.setColorFromInt(indicator.info.colorA);
					RendererHelper.drawTexture(new MatrixStack(), indicator.portalAPlaced ? this.texFullL : this.texEmptyL, posX, posY, size, size, 0.0D);
					RendererHelper.setColorFromInt(indicator.info.colorB);
					RendererHelper.drawTexture(new MatrixStack(), indicator.portalBPlaced ? this.texFullR : this.texEmptyR, posX, posY, size, size, 0.0D);
					RendererHelper.setColorFromInt(16777215);
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
				boolean isHoldingPortalGun = (is.getItem() == PGRRegistry.PORTAL_GUN.get());
				if (this.zoom) {
					if (this.zoomCounter < 5 && !mc.isGamePaused())
						this.zoomCounter++;
					if (!isHoldingPortalGun) {
						this.zoom = false;
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
			this.zoom = false;
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

//	public void onHandRender(RenderHandEvent event) {
//		Minecraft mc = Minecraft.getInstance();
//		ItemStack is = mc.player.getHeldItemMainhand();
//		boolean isHoldingPortalGun = (is.getItem() == PGRRegistry.PORTAL_GUN.get());
//		if (isHoldingPortalGun && this.zoomCounter >= 0)
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