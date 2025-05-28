#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec3 vertNormal;

out vec3 vNormal;
out vec3 vVertPos;

uniform mat4 mv_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;

void main(void) {
    vVertPos = (mv_matrix * vec4(vertPos, 1.0)).xyz;
    vNormal = (norm_matrix * vec4(vertNormal, 1.0)).xyz;
    gl_Position = p_matrix * mv_matrix * vec4(vertPos, 1.0);
}