package com.github.lorenzopapi.pgr.rendering.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL40;

// TODO: asset loader for shaders

public class PGRShaders {
	//	private static PGRShader vertexShader = new PGRShader(
//			GL40.GL_VERTEX_SHADER,
//			"#version 120\n" +
////					"layout(location = 0) in vec3 pos;\n" +
////					"layout(location = 1) in vec4 col;\n" +
//					"varying out vec4 color;\n" +
////					"out vec4 gl_Position;\n" +
//					"\n" +
//					"void main() {\n" +
//					"\tgl_Position = vec4(gl_Vertex.xyz, 1);\n" +
////					"\tcolor = col;\n" +
//					"\tcolor = gl_Color;\n" +
//					"}"
//	);
//
//	private static PGRShader fragmentShader = new PGRShader(
//			GL40.GL_FRAGMENT_SHADER,
//			"#version 120\n" +
//					"in vec4 color;\n" +
//					"\n" +
//					"void main() {\n" +
//					"\tgl_FragColor = color;\n" +
//					"}"
//	);
//
//	private static PGRShaderProgram program = new PGRShaderProgram(vertexShader, fragmentShader);
	private static PGRShaderProgram program = null;
	
	public static void init() {
	}
	
	public static void bindProgram() {
//		program.delete();
//		program = new PGRShaderProgram(vertexShader, fragmentShader);
		program.bind();
	}
	
	public static void unbind() {
		GlStateManager.useProgram(0);
	}
	
	public static void reload(Pair<String, String> objectIn) {
		if (program != null) program.delete();
		PGRShader vertexShader = new PGRShader(GL40.GL_VERTEX_SHADER, objectIn.getFirst());
		PGRShader fragmentShader = new PGRShader(GL40.GL_FRAGMENT_SHADER, objectIn.getSecond());
		program = new PGRShaderProgram(vertexShader, fragmentShader);
	}
	
	public static void setMatrix(Matrix4f matrix4f) {
		program.uniformMatrix("projectionMatrix", matrix4f);
	}
}