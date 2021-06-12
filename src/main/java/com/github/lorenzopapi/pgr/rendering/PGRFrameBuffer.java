package com.github.lorenzopapi.pgr.rendering;

import net.minecraft.client.shader.Framebuffer;

public class PGRFrameBuffer extends Framebuffer {
	public PGRFrameBuffer(int p_i51175_1_, int p_i51175_2_, boolean p_i51175_3_, boolean p_i51175_4_) {
		super(p_i51175_1_, p_i51175_2_, p_i51175_3_, p_i51175_4_);
	}
	
	/**
	 * This is here because the actual method is unmapped
	 * @return textureId
	 */
	public int getTextureID() {
		return func_242996_f();
	}
}
