#version 430

layout (location = 0) in vec3 position;

out vec3 originalPosition;

uniform mat4 mv_matrix;
uniform mat4 p_matrix;

void main(void) {
    originalPosition = position;
    gl_Position = p_matrix * mv_matrix * vec4(position, 1.0);
}