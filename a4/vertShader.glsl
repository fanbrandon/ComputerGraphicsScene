#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec2 tex_coord;
layout (location = 2) in vec3 vertNormal;

out vec2 tc;
out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVec;
out vec4 shadow_coord;

struct PositionalLight {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec3 position;
};
struct Material {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;

layout (binding=0) uniform sampler2D currentTexture;
layout (binding=1) uniform sampler2DShadow shadowTex;
layout (binding=2) uniform sampler2D heightMap;
uniform bool useHeightMap;

void main(void) {
    // Apply height displacement if enabled
    vec3 displacedPos = vertPos;
    if (useHeightMap) {
        float height = texture(heightMap, tex_coord).r;
        displacedPos += vertNormal * (height / 1.0f);
    }
    
    // Original calculations using displaced position
    varyingVertPos = (m_matrix * vec4(displacedPos,1.0)).xyz;
    varyingLightDir = light.position - varyingVertPos;
    varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;
    varyingHalfVec = (varyingLightDir-varyingVertPos).xyz;
    shadow_coord = shadowMVP * vec4(displacedPos,1.0);
    
    gl_Position = p_matrix * v_matrix * m_matrix * vec4(displacedPos,1.0);
    tc = tex_coord;
}