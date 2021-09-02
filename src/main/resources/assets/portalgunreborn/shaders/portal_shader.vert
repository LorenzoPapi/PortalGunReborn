#version 330

in vec4 pos;
in vec4 col;
varying vec4 color;

out vec4 gl_Position;

void main() {
	gl_Position = pos;
	color = col;
}