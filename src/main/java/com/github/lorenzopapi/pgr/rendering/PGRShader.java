package com.github.lorenzopapi.pgr.rendering;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.resources.IResourceManager;

import java.io.IOException;

public class PGRShader extends Shader {
	private final Framebuffer second;
	
	public PGRShader(IResourceManager resourceManager, String programName, Framebuffer framebufferInIn, Framebuffer secondFramebuffer, Framebuffer framebufferOutIn) throws IOException {
		super(resourceManager, programName, framebufferInIn, framebufferOutIn);
		this.second = secondFramebuffer;
	}
	
	@Override
	public void render(float partialTicks) {
		this.getShaderManager().func_216537_a("ClippingTexture", this.framebufferIn::func_242996_f);
		super.render(partialTicks);
	}
}
