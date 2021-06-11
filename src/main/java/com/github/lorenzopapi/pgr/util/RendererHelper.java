package com.github.lorenzopapi.pgr.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

public class RendererHelper {

	public static void setColorFromInt(int color) {
		setColorFromInt(color, 1.0F);
	}

	public static void setColorFromInt(int color, float alpha) {
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		GL11.glColor4f(r, g, b, alpha);
	}

	public static void drawTexture(MatrixStack stack, ResourceLocation resource, double posX, double posY, double width, double height, double zLevel) {
		Minecraft.getInstance().getTextureManager().bindTexture(resource);
		draw(stack, posX, posY, width, height, zLevel);
	}

	public static void draw(MatrixStack stack, double posX, double posY, double width, double height, double zLevel) {
		draw(stack, posX, posY, width, height, zLevel, 0D, 1D, 0D, 1D);
	}

	public static void draw(MatrixStack stack, double posX, double posY, double width, double height, double zLevel, double u1, double u2, double v1, double v2) {
		Matrix4f matrix = stack.getLast().getMatrix();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(matrix, (float)posX, (float)(posY + height), (float)zLevel).tex((float)u1, (float)v2).endVertex();
		bufferbuilder.pos(matrix, (float)(posX + width), (float)(posY + height), (float)zLevel).tex((float)u2, (float)v2).endVertex();
		bufferbuilder.pos(matrix, (float)(posX + width), (float)posY, (float)zLevel).tex((float)u2, (float)v1).endVertex();
		bufferbuilder.pos(matrix, (float)posX, (float)posY, (float)zLevel).tex((float)u1, (float)v1).endVertex();
		tessellator.draw();
	}
}
