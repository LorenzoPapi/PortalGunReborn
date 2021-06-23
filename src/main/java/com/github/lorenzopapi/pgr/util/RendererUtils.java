package com.github.lorenzopapi.pgr.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

public class RendererUtils {

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
		bufferbuilder.pos(matrix, (float) posX, (float) (posY + height), (float) zLevel).tex((float) u2, (float) v2).endVertex();
		bufferbuilder.pos(matrix, (float) (posX + width), (float) (posY + height), (float) zLevel).tex((float) u1, (float) v2).endVertex();
		bufferbuilder.pos(matrix, (float) (posX + width), (float) posY, (float) zLevel).tex((float) u1, (float) v1).endVertex();
		bufferbuilder.pos(matrix, (float) posX, (float) posY, (float) zLevel).tex((float) u2, (float) v1).endVertex();
		tessellator.draw();
	}

	public static void drawSquare(
			Vector4f corner1, Vector4f corner2, Vector4f corner3, Vector4f corner4, Vector3f normal, IVertexBuilder builder, int packedLightIn,
			float minU, float maxU, float minV, float maxV
	) {
		builder.addVertex(
				corner1.getX(), corner1.getY(), corner1.getZ(),
				1f, 1f, 1f, 1f,
				maxU, minV,
				OverlayTexture.NO_OVERLAY, packedLightIn,
				normal.getX(), normal.getY(), normal.getZ()
		);
		builder.addVertex(
				corner2.getX(), corner2.getY(), corner2.getZ(),
				1f, 1f, 1f, 1f,
				maxU, maxV,
				OverlayTexture.NO_OVERLAY, packedLightIn,
				normal.getX(), normal.getY(), normal.getZ()
		);
		builder.addVertex(
				corner3.getX(), corner3.getY(), corner3.getZ(),
				1f, 1f, 1f, 1f,
				minU, maxV,
				OverlayTexture.NO_OVERLAY, packedLightIn,
				normal.getX(), normal.getY(), normal.getZ()
		);
		builder.addVertex(
				corner4.getX(), corner4.getY(), corner4.getZ(),
				1f, 1f, 1f, 1f,
				minU, minV,
				OverlayTexture.NO_OVERLAY, packedLightIn,
				normal.getX(), normal.getY(), normal.getZ()
		);
	}
}
