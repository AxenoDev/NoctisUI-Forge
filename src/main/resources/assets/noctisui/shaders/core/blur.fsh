#version 150

uniform sampler2D InputSampler;
uniform vec2 InputResolution;
uniform vec2 BlurDirection;
uniform vec2 uSize;
uniform vec2 uLocation;

uniform float radius;
uniform float Brightness;
uniform float ApplyMask;

out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, float radius) {
    return length(max(abs(center) - size + radius, 0.0)) - radius;
}

vec4 gaussianBlur1D(sampler2D tex, vec2 uv, vec2 texelStep) {
    vec4 result = texture(tex, uv) * 0.2270270270;
    result += texture(tex, uv + texelStep * 1.0) * 0.1945945946;
    result += texture(tex, uv - texelStep * 1.0) * 0.1945945946;
    result += texture(tex, uv + texelStep * 2.0) * 0.1216216216;
    result += texture(tex, uv - texelStep * 2.0) * 0.1216216216;
    result += texture(tex, uv + texelStep * 3.0) * 0.0540540541;
    result += texture(tex, uv - texelStep * 3.0) * 0.0540540541;
    result += texture(tex, uv + texelStep * 4.0) * 0.0162162162;
    result += texture(tex, uv - texelStep * 4.0) * 0.0162162162;
    return result;
}

void main() {
    vec2 uv = (gl_FragCoord.xy + 0.5) / InputResolution;

    if (ApplyMask > 1.5) {
        vec4 color = texture(InputSampler, uv);

        vec2 halfSize = uSize * 0.5;
        vec2 centerPos = gl_FragCoord.xy - uLocation - halfSize;
        float sdf = roundedBoxSDF(centerPos, halfSize, radius);

        if (sdf > 1.0) {
            discard;
        }

        float alpha = 1.0 - smoothstep(-1.5, 1.5, sdf);
        fragColor = vec4(color.rgb * Brightness, alpha);
        return;
    }

    vec2 texelStep = BlurDirection / InputResolution;
    vec4 blurred = gaussianBlur1D(InputSampler, uv, texelStep);

    if (ApplyMask < 0.5) {
        fragColor = blurred;
        return;
    }

    vec2 halfSize = uSize * 0.5;
    vec2 centerPos = gl_FragCoord.xy - uLocation - halfSize;
    float sdf = roundedBoxSDF(centerPos, halfSize, radius);

    if (sdf > 1.0) {
        discard;
    }

    float alpha = 1.0 - smoothstep(-1.5, 1.5, sdf);
    fragColor = vec4(blurred.rgb * Brightness, alpha);
}
