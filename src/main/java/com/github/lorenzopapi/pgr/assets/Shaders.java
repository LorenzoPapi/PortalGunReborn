package com.github.lorenzopapi.pgr.assets;

import com.github.lorenzopapi.pgr.util.PGRShader;
import com.github.lorenzopapi.pgr.util.PGRShaderProgram;
import org.lwjgl.opengl.GL40;

// TODO: asset loader for shaders

public class Shaders {
	private static PGRShader vertexShader = new PGRShader(
			GL40.GL_VERTEX_SHADER,
			"#version 330\n" +
					"layout(location = 0) in vec3 pos;\n" +
					"layout(location = 1) in vec4 col;\n" +
					"varying vec4 color;" +
//					"out vec4 gl_Position;\n" +
					"\n" +
					"void main() {\n" +
					"\tgl_Position = vec4(pos, 1);\n" +
					"\tcolor = col;\n" +
					"}"
	);
	
	private static PGRShader fragmentShader = new PGRShader(
			GL40.GL_FRAGMENT_SHADER,
			"#version 330\n" +
					"in vec4 color;\n" +
					"\n" +
					"void main() {\n" +
					"\tgl_FragColor = color;\n" +
					"}"
	);
	
	private static PGRShaderProgram program = new PGRShaderProgram(vertexShader, fragmentShader);
	
	public static void init() {
	}
	
	public static void bindProgram() {
//		program.delete();
//		program = new PGRShaderProgram(vertexShader, fragmentShader);
		program.bind();
	}
}
