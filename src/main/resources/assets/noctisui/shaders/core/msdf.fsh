#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float pxRange;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

void main() {
    vec4 msd = texture(Sampler0, texCoord0);
    float sd = median(msd.r, msd.g, msd.b);

    vec2 msdfUnitExtent = pxRange / vec2(textureSize(Sampler0, 0));
    vec2 pixelAndDist = max(fwidth(texCoord0), vec2(0.00001));

    float scale = min(msdfUnitExtent.x / pixelAndDist.x, msdfUnitExtent.y / pixelAndDist.y);

    float screenPxDistance = scale * (sd - 0.5);
    float opacity = clamp(screenPxDistance + 0.5, 0.0, 1.0);

    if (opacity <= 0.0) {
        discard;
    }

    fragColor = vertexColor * vec4(1.0, 1.0, 1.0, opacity) * ColorModulator;
}