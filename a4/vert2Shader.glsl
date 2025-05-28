#version 430 

layout(location = 0) uniform mat4 mv_matrix;
layout(location = 1) uniform mat4 p_matrix;
uniform int lineColor;

const vec4 vertices[6] = vec4[6](
    vec4(0.0, 0.0, 0.0, 1.0),  // Origin
    vec4(3.0, 0.0, 0.0, 1.0),  // X-axis
    vec4(0.0, 0.0, 0.0, 1.0),  // Origin
    vec4(0.0, 3.0, 0.0, 1.0),  // Y-axis
    vec4(0.0, 0.0, 0.0, 1.0),  // Origin
    vec4(0.0, 0.0, 3.0, 1.0)   // Z-axis
);

void main() {
    gl_Position = p_matrix * mv_matrix * vertices[gl_VertexID];
}