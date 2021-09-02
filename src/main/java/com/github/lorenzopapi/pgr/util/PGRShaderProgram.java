package com.github.lorenzopapi.pgr.util;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL20;

public class PGRShaderProgram {
	private final int id;
	private final int vertId, fragId;
	
	public PGRShaderProgram(PGRShader vert, PGRShader frag) {
		id = GlStateManager.createProgram();
		GlStateManager.attachShader(id, vertId = vert.getId());
		GlStateManager.attachShader(id, fragId = frag.getId());
		
		GL20.glBindAttribLocation(id, 0, "pos");
		GL20.glBindAttribLocation(id, 1, "col");
		
		GlStateManager.linkProgram(id);
		GL20.glValidateProgram(id);
//		String log = GL20.glGetProgramInfoLog(id);
//		if (!log.equals("")) System.out.println(log);
	}
	
	public void delete() {
		GL20.glDetachShader(id, vertId);
		GL20.glDetachShader(id, fragId);
		GlStateManager.deleteProgram(id);
	}
	
	public void bind() {
		GlStateManager.useProgram(id);
	}
}
