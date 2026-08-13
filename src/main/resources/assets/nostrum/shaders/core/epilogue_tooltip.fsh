#version 150

in vec4 vertexColor;
in vec2 screenPosition;

uniform float NostrumTime;
uniform float RealTime;

uniform float TooltipX;
uniform float TooltipY;
uniform float TooltipWidth;
uniform float TooltipHeight;

out vec4 fragColor;

const float PI = 3.14159265359;
const float TAU = 6.28318530718;


// ------------------------------------------------------------
// Hash
// ------------------------------------------------------------

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));

    p += dot(
            p,
            p + 45.32
    );

    return fract(
            p.x * p.y
    );
}


// ------------------------------------------------------------
// Noise
// ------------------------------------------------------------

float noise(vec2 p) {

    vec2 i = floor(p);
    vec2 f = fract(p);

    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(
            mix(a, b, f.x),
            mix(c, d, f.x),
            f.y
    );
}


// ------------------------------------------------------------
// FBM
// ------------------------------------------------------------

float fbm(vec2 p) {

    float value = 0.0;
    float amplitude = 0.5;

    value += noise(p) * amplitude;

    p *= 2.03;
    amplitude *= 0.5;

    value += noise(p) * amplitude;

    p *= 2.01;
    amplitude *= 0.5;

    value += noise(p) * amplitude;

    p *= 2.02;
    amplitude *= 0.5;

    value += noise(p) * amplitude;

    return value;
}


// ------------------------------------------------------------
// Main
// ------------------------------------------------------------

void main() {

    // --------------------------------------------------------
    // Tooltip local UV
    // --------------------------------------------------------

    vec2 uv =
    (gl_FragCoord.xy -
    vec2(
            TooltipX,
            TooltipY
    ))
    /
    vec2(
            TooltipWidth,
            TooltipHeight
    );

    uv.y = 1.0 - uv.y;


    // --------------------------------------------------------
    // Center
    // --------------------------------------------------------

    vec2 centered =
    uv - vec2(0.5);

    float aspect =
    TooltipWidth /
    max(
            TooltipHeight,
            1.0
    );

    centered.x *= aspect;

    float radius =
    length(centered);


    // --------------------------------------------------------
    // Time
    // --------------------------------------------------------

    float time =
    NostrumTime * 0.018;

    float realTime =
    RealTime * 0.00035;


    // --------------------------------------------------------
    // Cosmic flow
    // --------------------------------------------------------

    vec2 flow =
    uv * 3.5;

    flow.x +=
    time * 0.15;

    flow.y -=
    time * 0.10;


    // --------------------------------------------------------
    // Nebula
    // --------------------------------------------------------

    float nebula =
    fbm(flow);

    nebula +=
    fbm(
            flow * 2.4
            - vec2(
                    time * 0.20,
                    -time * 0.13
            )
    ) * 0.35;


    // --------------------------------------------------------
    // Colors
    // --------------------------------------------------------

    vec3 voidColor =
    vec3(
            0.006,
            0.009,
            0.028
    );

    vec3 abyss =
    vec3(
            0.006,
            0.014,
            0.045
    );

    vec3 blue =
    vec3(
            0.012,
            0.035,
            0.12
    );

    vec3 violet =
    vec3(
            0.075,
            0.025,
            0.16
    );


    vec3 color =
    voidColor;

    color +=
    abyss *
    nebula *
    1.2;

    color +=
    blue *
    pow(
            nebula,
            2.0
    ) *
    0.65;

    color +=
    violet *
    pow(
            nebula,
            3.0
    ) *
    0.35;


    // --------------------------------------------------------
    // Last Light
    // --------------------------------------------------------

    float lastLight =
    1.0 -
    smoothstep(
            0.05,
            0.72,
            radius
    );

    lastLight =
    pow(
            lastLight,
            2.5
    );

    color +=
    vec3(
            0.02,
            0.075,
            0.25
    ) *
    lastLight *
    0.45;


    // --------------------------------------------------------
    // Ring of the End
    // --------------------------------------------------------

    float ring =
    abs(
            radius -
            0.47
    );

    float endRing =
    1.0 -
    smoothstep(
            0.0,
            0.035,
            ring
    );

    float pulse =
    0.65 +
    0.35 *
    sin(
            time * 2.0 +
            radius * 18.0
    );

    color +=
    vec3(
            0.025,
            0.10,
            0.40
    ) *
    endRing *
    pulse *
    0.40;


    // --------------------------------------------------------
    // Ancient waves
    // --------------------------------------------------------

    float wave =
    sin(
            radius * 38.0 -
            time * 3.0
    );

    wave =
    smoothstep(
            0.65,
            1.0,
            wave
    );

    wave *=
    exp(
            -radius * 2.0
    );

    color +=
    vec3(
            0.015,
            0.07,
            0.25
    ) *
    wave *
    0.20;


    // --------------------------------------------------------
    // Corners
    // --------------------------------------------------------

    float cornerX =
    smoothstep(
            0.15,
            0.0,
            min(
                    uv.x,
                    1.0 - uv.x
            )
    );

    float cornerY =
    smoothstep(
            0.15,
            0.0,
            min(
                    uv.y,
                    1.0 - uv.y
            )
    );

    float corners =
    cornerX *
    cornerY;

    float cornerPulse =
    0.75 +
    0.25 *
    sin(
            realTime * 4.0
    );

    color +=
    vec3(
            0.035,
            0.12,
            0.50
    ) *
    corners *
    cornerPulse *
    0.35;


    // --------------------------------------------------------
    // Stars
    // --------------------------------------------------------

    vec2 starGrid =
    floor(
            uv * 42.0
    );

    float star =
    hash(starGrid);

    float starMask =
    step(
            0.985,
            star
    );

    float starPulse =
    0.5 +
    0.5 *
    sin(
            realTime * 3.0 +
            star * 40.0
    );

    color +=
    vec3(
            0.18,
            0.38,
            1.0
    ) *
    starMask *
    starPulse *
    0.20;


    // --------------------------------------------------------
    // Edge
    // --------------------------------------------------------

    float edge =
    smoothstep(
            0.35,
            0.72,
            radius
    );

    color *=
    1.0 -
    edge * 0.30;


    // --------------------------------------------------------
    // Final
    // --------------------------------------------------------

    color *=
    vertexColor.rgb;

    color *= 0.82;

    color =
    min(
            color,
            vec3(1.0)
    );

    fragColor =
    vec4(
            color,
            1.0
    );
}