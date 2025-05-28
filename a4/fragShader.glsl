#version 430

in vec2 tc;
in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVec;
in vec4 shadow_coord;

out vec4 fragColor;

struct PositionalLight {
    vec4 ambient, diffuse, specular;
    vec3 position;
};

struct Material {
    vec4 ambient, diffuse, specular;
    float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 v_matrix;
uniform int useTexture;
uniform bool useHeightMap;

layout (binding=0) uniform sampler2D currentTexture;
layout (binding=1) uniform sampler2DShadow shadowTex;
layout (binding=2) uniform sampler2D heightMap;  // For terrain coloring

float lookup(float x, float y) {
    return textureProj(shadowTex, shadow_coord + vec4(x * 0.001 * shadow_coord.w,
                                                     y * 0.001 * shadow_coord.w,
                                                     -0.01, 0.0));
}

void main(void) {
    // Shadow mapping with PCF
    float shadowFactor = 0.0;
    float swidth = 2.5;
    vec2 o = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
    shadowFactor += lookup(-1.5 * swidth + o.x,  1.5 * swidth - o.y);
    shadowFactor += lookup(-1.5 * swidth + o.x, -0.5 * swidth - o.y);
    shadowFactor += lookup( 0.5 * swidth + o.x,  1.5 * swidth - o.y);
    shadowFactor += lookup( 0.5 * swidth + o.x, -0.5 * swidth - o.y);
    shadowFactor /= 4.0;

    // Lighting calculations
    vec3 L = normalize(varyingLightDir);
    vec3 N = normalize(varyingNormal);
    vec3 V = normalize(-v_matrix[3].xyz - varyingVertPos);
    vec3 H = normalize(varyingHalfVec);

    vec3 ambient = (globalAmbient * material.ambient + light.ambient * material.ambient).xyz;
    vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(dot(L, N), 0.0);
    vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(dot(H, N), 0.0), material.shininess * 3.0);

    vec3 lighting = ambient + shadowFactor * (diffuse + specular);

    // Final color with optional height-based coloring
    vec4 texColor = texture(currentTexture, tc);
    if (useHeightMap) {
        float height = texture(heightMap, tc).r;
        texColor = mix(texColor, vec4(height, height, height, 1.0), 0.3);
    }
    fragColor = vec4(lighting, 1.0) * texColor;
}