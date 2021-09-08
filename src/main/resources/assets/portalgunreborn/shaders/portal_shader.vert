#version 120

varying out vec4 color;

void main() {
	gl_Position = vec4(gl_Vertex.xyz, 1);
	color = gl_Color;
}