package com.github.lorenzopapi.pgr.rendering.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class PGRShaderProgram {
	private final int id;
	private final int vertId, fragId;
	
	private final int projMat;
	
	public PGRShaderProgram(PGRShader vert, PGRShader frag) {
		id = GlStateManager.createProgram();
		GL20.glAttachShader(id, vertId = vert.getId());
		GL20.glAttachShader(id, fragId = frag.getId());
		
//		GL20.glBindAttribLocation(id, 0, "pos");
//		GL20.glBindAttribLocation(id, 1, "col");
		
		projMat = GL20.glGetUniformLocation(id, "projectionMatrix");
		
		GL20.glLinkProgram(id);
		GL20.glValidateProgram(id);
		
		int logLength = GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH);
		System.out.println(GL20.glGetProgramInfoLog(id));
		if (logLength > 0) {
			throw new RuntimeException();
		}
//		String log = GL20.glGetProgramInfoLog(id);
//		if (!log.equals("")) System.out.println(log);
	}
	
	public void delete() {
		GL20.glDetachShader(id, vertId);
		GL20.glDetachShader(id, fragId);
		GL20.glDeleteShader(vertId);
		GL20.glDeleteShader(fragId);
		GlStateManager.deleteProgram(id);
	}
	
	public void bind() {
		GlStateManager.useProgram(id);
	}
	
	private final FloatBuffer buffer = FloatBuffer.allocate(20);
	
	public void uniformMatrix(String projMat, Matrix4f matrix4f) {
		if (projMat.equals("projectionMatrix")) {
			matrix4f.write(buffer);
			GL20.glUniformMatrix4fv(this.projMat, true, buffer);
			buffer.clear();
		} else {
			throw new RuntimeException("no");
		}
	}
}