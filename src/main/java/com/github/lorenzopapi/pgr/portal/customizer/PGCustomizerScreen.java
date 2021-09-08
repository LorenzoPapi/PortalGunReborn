package com.github.lorenzopapi.pgr.portal.customizer;

import com.github.lorenzopapi.pgr.util.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class PGCustomizerScreen extends ContainerScreen<PGCustomizerContainer> {
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/customizer.png");

	public PGCustomizerScreen(PGCustomizerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		this.titleX = 40;
		this.playerInventoryTitleY = this.ySize - 80;
		this.xSize = 174;
		this.ySize = 179;
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		int x1 = this.guiLeft + 40;
		int y1 = this.guiTop + 55;
		this.fillGradient(stack, x1, y1, x1+128, y1+9, 0, 0xFF0000);
		this.fillGradient(stack, x1, y1+9, x1+128, y1+18, 0, 0x00FF00);
		this.fillGradient(stack, x1, y1+18, x1+128, y1+27, 0, 0x0000FF);
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(stack, mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int x, int y) {
		this.renderBackground(stack);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		this.blit(stack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}
}
