#version 430

in vec3 vNormal;
in vec3 vVertPos;
out vec4 fragColor;

uniform mat4 mv_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;
layout (binding = 0) uniform samplerCube skybox;

void main(void) {
    // Calculate reflection vector
    vec3 r = -reflect(normalize(-vVertPos), normalize(vNormal));
    fragColor = texture(skybox, r);
}