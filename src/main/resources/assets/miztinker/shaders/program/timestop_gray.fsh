#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform float Spread;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 desaturated = mix(color.rgb, vec3(gray), 0.85);
    vec3 shade = desaturated * 0.62 + vec3(0.11);

    float aspect = OutSize.x / max(OutSize.y, 1.0);
    vec2 centered = texCoord - vec2(0.5);
    centered.x *= aspect;

    float distanceFromCenter = length(centered);
    float edgeWidth = 0.075;
    float maxRadius = length(vec2(0.5 * aspect, 0.5)) + edgeWidth;
    float progress = smoothstep(0.0, 1.0, clamp(Spread, 0.0, 1.0));
    float radius = maxRadius * progress;

    float spread = 1.0 - smoothstep(radius - edgeWidth, radius + edgeWidth, distanceFromCenter);
    float wave = 1.0 - smoothstep(0.0, edgeWidth, abs(distanceFromCenter - radius));
    vec3 waveTint = vec3(0.82, 0.88, 1.0);

    vec3 result = mix(color.rgb, shade, spread);
    result = mix(result, waveTint, wave * (1.0 - progress) * 0.12);
    fragColor = vec4(result, color.a);
}
