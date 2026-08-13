#version 150

uniform sampler2D Sampler0;

uniform float NostrumTime;
uniform float Speed;
uniform float Intensity;

uniform vec3 ColorA;
uniform vec3 ColorB;

in vec4 vertexColor;
in vec2 texCoord0;

in float magicOffset;

out vec4 outColor;


// ============================================================
// HASH
// ============================================================

float hash21(vec2 p) {

    p =
    fract(
            p *
            vec2(
                    127.1,
                    311.7
            )
    );

    p +=
    dot(
            p,
            p + 19.19
    );

    return fract(
            p.x * p.y
    );
}


// ============================================================
// 2D NOISE
// ============================================================

float noise(vec2 p) {

    vec2 i =
    floor(p);

    vec2 f =
    fract(p);


    f =
    f * f *
    (3.0 - 2.0 * f);


    float a =
    hash21(i);

    float b =
    hash21(
            i +
            vec2(1.0, 0.0)
    );

    float c =
    hash21(
            i +
            vec2(0.0, 1.0)
    );

    float d =
    hash21(
            i +
            vec2(1.0, 1.0)
    );


    return mix(
            mix(a, b, f.x),
            mix(c, d, f.x),
            f.y
    );
}


// ============================================================
// DOMAIN WARP
// ============================================================

vec2 domainWarp(
        vec2 p,
        float time
) {

    float n1 =
    noise(
            p * 2.5
            +
            time * 0.12
    );


    float n2 =
    noise(
            p * 5.0
            -
            time * 0.08
    );


    p.x +=
    sin(
            p.y * 7.0
            +
            n1 * 5.0
            +
            time
    )
    *
    0.035;


    p.y +=
    cos(
            p.x * 9.0
            -
            n2 * 4.0
            -
            time * 0.7
    )
    *
    0.035;


    return p;
}


// ============================================================
// AETHER FLOW
//
// 文字内部を流れるエーテル。
// ============================================================

float aetherFlow(
        vec2 uv,
        float time
) {

    vec2 p =
    uv;


    p =
    domainWarp(
            p,
            time
    );


    float n =
    noise(
            p * 4.0
            +
            time * 0.15
    );


    float flow =
    sin(
            p.x * 12.0
            +
            p.y * 7.0
            +
            n * 5.0
            -
            time * 2.0
    );


    flow =
    smoothstep(
            0.60,
            1.0,
            flow
    );


    return flow;
}


// ============================================================
// AETHER VEINS
//
// 細い魔力の筋。
// ============================================================

float aetherVeins(
        vec2 uv,
        float time
) {

    vec2 p =
    uv * 5.0;


    float n =
    noise(
            p * 1.5
            +
            time * 0.1
    );


    float line1 =
    sin(
            p.x * 8.0
            +
            p.y * 4.0
            +
            n * 4.0
            -
            time * 1.8
    );


    float line2 =
    sin(
            p.x * 5.0
            -
            p.y * 11.0
            -
            n * 3.0
            +
            time * 1.2
    );


    line1 =
    smoothstep(
            0.80,
            1.0,
            line1
    );


    line2 =
    smoothstep(
            0.84,
            1.0,
            line2
    );


    return
    line1 * 0.65
    +
    line2 * 0.35;
}


// ============================================================
// HEXAGON
// ============================================================

float hexagon(
        vec2 p,
        float radius
) {

    p =
    abs(p);


    return
    max(
            p.x * 0.866025
            +
            p.y * 0.5,
            p.y
    )
    -
    radius;
}


// ============================================================
// HEXAGON RING
// ============================================================

float hexRing(
        vec2 p,
        float radius,
        float thickness
) {

    float d =
    abs(
            hexagon(
                    p,
                    radius
            )
    );


    return
    1.0 -
    smoothstep(
            thickness,
            thickness + 0.008,
            d
    );
}


// ============================================================
// RUNE RING
//
// 円ではなく六角形を中心とした錬成構造。
// ============================================================

float runeRing(
        vec2 uv,
        float radius,
        float rotation,
        float time
) {

    vec2 p =
    uv -
    vec2(0.5);


    float c =
    cos(rotation);

    float s =
    sin(rotation);


    p =
    mat2(
            c, -s,
            s,  c
    )
    *
    p;


    float ring =
    hexRing(
            p,
            radius,
            0.008
    );


    // --------------------------------------------------------
    // Rune segmentation
    // --------------------------------------------------------

    float angle =
    atan(
            p.y,
            p.x
    );


    float segments =
    sin(
            angle * 9.0
            +
            time * 0.45
    );


    segments =
    smoothstep(
            -0.1,
            0.25,
            segments
    );


    // --------------------------------------------------------
    // Noise breakup
    // --------------------------------------------------------

    float breakup =
    noise(
            vec2(
                    angle * 4.0,
                    time * 0.12
            )
    );


    return
    ring
    *
    segments
    *
    mix(
            0.35,
            1.0,
            breakup
    );
}


// ============================================================
// ARCANE ORBIT
//
// 錬成陣を周回するエネルギー。
// ============================================================

float arcaneOrbit(
        vec2 uv,
        float time
) {

    vec2 p =
    uv -
    vec2(0.5);


    float angle =
    atan(
            p.y,
            p.x
    );


    float radius =
    length(p);


    float orbitRadius =
    0.31
    +
    sin(
            angle * 4.0
            +
            time * 0.6
    )
    *
    0.018;


    float orbit =
    1.0 -
    smoothstep(
            0.006,
            0.014,
            abs(
                    radius -
                    orbitRadius
            )
    );


    float energy =
    sin(
            angle * 16.0
            -
            time * 3.0
    );


    energy =
    smoothstep(
            0.70,
            1.0,
            energy
    );


    return
    orbit
    *
    (
    0.15
    +
    energy * 0.85
    );
}


// ============================================================
// PARTICLE FIELD
//
// 魔力粒子。
// ============================================================

float magicParticles(
        vec2 uv,
        float time
) {

    vec2 grid =
    uv * 14.0;


    vec2 cell =
    floor(grid);


    vec2 local =
    fract(grid);


    float result =
    0.0;


    for (int y = -1; y <= 1; y++) {

        for (int x = -1; x <= 1; x++) {

            vec2 offset =
            vec2(
                    float(x),
                    float(y)
            );


            vec2 id =
            cell +
            offset;


            float h =
            hash21(id);


            // 粒子密度
            if (h < 0.72) {
                continue;
            }


            // ------------------------------------------------
            // Random particle position
            // ------------------------------------------------

            vec2 pos =
            vec2(
                    hash21(id + 13.7),
                    hash21(id + 47.2)
            );


            // ------------------------------------------------
            // Attraction toward center
            // ------------------------------------------------

            float orbit =
            time *
            (
            0.4 +
            h * 0.8
            );


            vec2 center =
            vec2(0.5);


            vec2 toCenter =
            center -
            (
            id +
            pos
            );


            float distanceToCenter =
            length(
                    toCenter
            );


            vec2 direction =
            normalize(
                    toCenter
            );


            vec2 perpendicular =
            vec2(
                    -direction.y,
                    direction.x
            );


            vec2 particlePos =
            id +
            pos;


            particlePos +=
            direction
            *
            (
            sin(
                    time *
                    (
                    0.3 +
                    h
                    )
            )
            *
            0.025
            );


            particlePos +=
            perpendicular
            *
            sin(
                    orbit
                    +
                    h * 20.0
            )
            *
            0.015;


            // ------------------------------------------------
            // Distance
            // ------------------------------------------------

            vec2 diff =
            uv -
            particlePos;


            float d =
            length(
                    diff
            );


            float size =
            mix(
                    0.002,
                    0.006,
                    h
            );


            float particle =
            1.0 -
            smoothstep(
                    size * 0.2,
                    size,
                    d
            );


            // ------------------------------------------------
            // Twinkle
            // ------------------------------------------------

            float twinkle =
            0.5 +
            0.5 *
            sin(
                    time *
                    (
                    2.0 +
                    h * 5.0
                    )
                    +
                    h * 30.0
            );


            result =
            max(
                    result,
                    particle *
                    twinkle
            );
        }
    }


    return result;
}


// ============================================================
// PARTICLE STREAK
//
// 中心へ吸い込まれる細い光。
// ============================================================

float particleStreak(
        vec2 uv,
        float time
) {

    vec2 p =
    uv -
    vec2(0.5);


    float radius =
    length(p);


    float angle =
    atan(
            p.y,
            p.x
    );


    float streak =
    sin(
            angle * 18.0
            -
            radius * 28.0
            -
            time * 3.0
    );


    streak =
    smoothstep(
            0.86,
            1.0,
            streak
    );


    float falloff =
    smoothstep(
            0.42,
            0.04,
            radius
    );


    return
    streak *
    falloff;
}


// ============================================================
// CORE
//
// 中央の錬成コア。
// ============================================================

float alchemicalCore(
        vec2 uv,
        float time
) {

    vec2 p =
    uv -
    vec2(0.5);


    float d =
    length(p);


    float pulse =
    sin(
            time * 0.75
    );


    pulse =
    pulse * 0.5
    +
    0.5;


    pulse =
    smoothstep(
            0.72,
            1.0,
            pulse
    );


    float core =
    1.0 -
    smoothstep(
            0.0,
            0.045,
            d
    );


    float glow =
    1.0 -
    smoothstep(
            0.0,
            0.20,
            d
    );


    return
    (
    core * 2.0
    +
    glow * 0.35
    )
    *
    pulse;
}


// ============================================================
// STAR
// ============================================================

float star(
        vec2 uv,
        vec2 cell,
        float time
) {

    float h =
    hash21(cell);


    if (h < 0.82) {
        return 0.0;
    }


    vec2 center =
    vec2(
            hash21(cell + 1.7),
            hash21(cell + 8.3)
    );


    vec2 p =
    uv -
    (
    cell +
    center
    );


    float d =
    length(p);


    float size =
    mix(
            0.012,
            0.035,
            hash21(cell + 3.1)
    );


    float core =
    1.0 -
    smoothstep(
            0.0,
            size,
            d
    );


    float vertical =
    exp(
            -abs(p.x) * 190.0
    )
    *
    exp(
            -abs(p.y) * 30.0
    );


    float horizontal =
    exp(
            -abs(p.y) * 190.0
    )
    *
    exp(
            -abs(p.x) * 30.0
    );


    float rays =
    max(
            vertical,
            horizontal
    );


    float twinkle =
    0.65
    +
    0.35 *
    sin(
            time *
            (
            2.0 +
            h * 4.0
            )
            +
            h * 20.0
    );


    return
    (
    core * 1.5
    +
    rays
    )
    *
    twinkle;
}


// ============================================================
// ALCHEMICAL PULSE
// ============================================================

float alchemicalPulse(
        float time
) {

    float p =
    sin(
            time * 0.75
    );


    p =
    p * 0.5
    +
    0.5;


    return
    smoothstep(
            0.72,
            1.0,
            p
    );
}


// ============================================================
// MAIN
// ============================================================

void main() {

    // ========================================================
    // FONT ATLAS
    // ========================================================

    vec4 tex =
    texture(
            Sampler0,
            texCoord0
    );


    // --------------------------------------------------------
    // Preserve font alpha
    // --------------------------------------------------------

    float alpha =
    tex.a *
    vertexColor.a;


    if (alpha <= 0.0) {
        discard;
    }


    // ========================================================
    // TIME
    // ========================================================

    float time =
    NostrumTime *
    Speed;


    // ========================================================
    // BASE UV
    // ========================================================

    vec2 uv =
    gl_FragCoord.xy /
    180.0;


    // --------------------------------------------------------
    // Glyph magic phase
    // --------------------------------------------------------

    uv +=
    magicOffset *
    0.012;


    // ========================================================
    // BASE ALCHEMICAL COLOR
    // ========================================================

    float baseWave =
    sin(
            uv.x * 5.0
            +
            sin(
                    uv.y * 3.0
            ) * 2.0
            +
            time * 1.25
    );


    baseWave =
    baseWave * 0.5
    +
    0.5;


    vec3 color =
    mix(
            ColorA,
            ColorB,
            baseWave
    );


    // ========================================================
    // AETHER
    // ========================================================

    float flow =
    aetherFlow(
            uv,
            time
    );


    float veins =
    aetherVeins(
            uv,
            time
    );


    vec3 aetherColor =
    vec3(
            0.10,
            0.95,
            0.78
    );


    color +=
    aetherColor
    *
    flow
    *
    0.22
    *
    Intensity;


    color +=
    aetherColor
    *
    veins
    *
    0.09
    *
    Intensity;


    // ========================================================
    // ALCHEMICAL PULSE
    // ========================================================

    float pulse =
    alchemicalPulse(
            time
    );


    // ========================================================
    // RUNE STRUCTURE
    // ========================================================

    vec2 runeUV =
    gl_FragCoord.xy /
    vec2(
            720.0,
            720.0
    );


    float rune1 =
    runeRing(
            runeUV,
            0.16,
            time * 0.10,
            time
    );


    float rune2 =
    runeRing(
            runeUV,
            0.24,
            -time * 0.07,
            time
    );


    float rune3 =
    runeRing(
            runeUV,
            0.34,
            time * 0.04,
            time
    );


    float runeStructure =
    rune1 * 0.40
    +
    rune2 * 0.23
    +
    rune3 * 0.12;


    // --------------------------------------------------------
    // Structure becomes visible during pulse.
    // --------------------------------------------------------

    runeStructure *=
    0.10
    +
    pulse * 0.90;


    vec3 runeColor =
    vec3(
            0.50,
            0.92,
            0.86
    );


    color +=
    runeColor
    *
    runeStructure
    *
    0.55
    *
    Intensity;


    // ========================================================
    // ORBIT
    // ========================================================

    float orbit =
    arcaneOrbit(
            runeUV,
            time
    );


    color +=
    vec3(
            0.30,
            0.90,
            0.82
    )
    *
    orbit
    *
    (
    0.20
    +
    pulse * 0.55
    )
    *
    Intensity;


    // ========================================================
    // PARTICLES
    // ========================================================

    vec2 particleUV =
    gl_FragCoord.xy /
    220.0;


    float particles =
    magicParticles(
            particleUV,
            time
    );


    color +=
    vec3(
            0.70,
            1.00,
            0.96
    )
    *
    particles
    *
    0.80
    *
    Intensity;


    // ========================================================
    // PARTICLE STREAKS
    // ========================================================

    float streak =
    particleStreak(
            runeUV,
            time
    );


    color +=
    vec3(
            0.20,
            0.90,
            0.80
    )
    *
    streak
    *
    0.10
    *
    Intensity;


    // ========================================================
    // ALCHEMICAL CORE
    // ========================================================

    float core =
    alchemicalCore(
            runeUV,
            time
    );


    vec3 coreColor =
    vec3(
            0.85,
            1.00,
            0.98
    );


    color +=
    coreColor
    *
    core
    *
    0.75
    *
    Intensity;


    // ========================================================
    // CRYSTALLINE STRUCTURE
    // ========================================================

    float crystal =
    noise(
            uv * 11.0
    );


    crystal =
    smoothstep(
            0.70,
            0.92,
            crystal
    );


    color +=
    vec3(
            0.20,
            0.70,
            0.65
    )
    *
    crystal
    *
    0.035
    *
    Intensity;


    // ========================================================
    // MAGICAL STARS
    // ========================================================

    vec2 starUV =
    gl_FragCoord.xy /
    55.0;


    vec2 starCell =
    floor(
            starUV
    );


    float stars =
    0.0;


    for (int y = -1; y <= 1; y++) {

        for (int x = -1; x <= 1; x++) {

            vec2 offset =
            vec2(
                    float(x),
                    float(y)
            );


            stars =
            max(
                    stars,
                    star(
                            starUV,
                            starCell + offset,
                            time
                    )
            );
        }
    }


    color +=
    vec3(
            0.72,
            1.00,
            0.97
    )
    *
    stars
    *
    1.10
    *
    Intensity;


    // ========================================================
    // UNKNOWN ARCANE ENERGY
    //
    // ごく薄い紫。
    // ========================================================

    float unknown =
    sin(
            uv.x * 4.0
            -
            uv.y * 6.0
            +
            time * 0.35
    );


    unknown =
    smoothstep(
            0.78,
            1.0,
            unknown
    );


    color +=
    vec3(
            0.32,
            0.20,
            0.70
    )
    *
    unknown
    *
    0.025
    *
    Intensity;


    // ========================================================
    // GLYPH SPECIFIC MAGIC
    // ========================================================

    float glyphMagic =
    magicOffset *
    0.5
    +
    0.5;


    color +=
    mix(
            ColorA,
            vec3(
                    0.80,
                    1.00,
                    0.96
            ),
            glyphMagic
    )
    *
    0.055
    *
    Intensity;


    // ========================================================
    // FINAL ALCHEMICAL FLASH
    //
    // パルスの頂点でのみ、文字に白い核を作る。
    // ========================================================

    color +=
    vec3(
            0.90,
            1.00,
            0.98
    )
    *
    pulse
    *
    0.055
    *
    Intensity;


    // ========================================================
    // FINAL
    // ========================================================

    vec3 finalColor =
    color *
    vertexColor.rgb;


    outColor =
    vec4(
            finalColor,
            alpha
    );
}