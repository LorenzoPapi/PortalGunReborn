package com.github.lorenzopapi.pgr.rendering.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20;

public class PGRShader {
	private static final Logger LOGGER = LogManager.getLogger("Portal Gun Reborn:Shaders");
	
	private final int id;
	
	public PGRShader(int type, String src) {
		id = GlStateManager.createShader(type);
		GlStateManager.shaderSource(id, src);
		GlStateManager.compileShader(id);
		System.out.println(GL20.glGetShaderi(id, GL20.GL_SHADER_TYPE));
		int len = GL20.glGetShaderi(id, GL20.GL_SHADER_SOURCE_LENGTH);
		if (len == 0) LOGGER.warn("WARNING: Shader length is 0!");
		String log = GL20.glGetShaderInfoLog(id);
		if (!log.isEmpty()) {
			LOGGER.warn(log);
			if (!FMLEnvironment.production) throw new RuntimeException();
		}
		if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
			int logLen = GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH);
			LOGGER.warn("Failed to compile shader");
		}
	}
	
	public void delete() {
		GlStateManager.deleteShader(id);
	}
	
	public int getId() {
		return id;
	}
}