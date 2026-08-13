#version 150

uniform float NostrumTime;
uniform float RealTime;

uniform float TooltipX;
uniform float TooltipY;
uniform float TooltipWidth;
uniform float TooltipHeight;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 fragPos;

out vec4 fragColor;

const float PI  = 3.14159265359;
const float TAU = 6.28318530718;


// ============================================================
// Settings
// ============================================================

// 時計そのものの大きさ。
// ツールチップサイズには依存しない。
const float CLOCK_SIZE = 150.0;


// ============================================================
// Hash / Noise
// ============================================================

float hash21(vec2 p)
{
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);

    return fract(p.x * p.y);
}


float noise(vec2 p)
{
    vec2 i = floor(p);
    vec2 f = fract(p);

    f = f * f * (3.0 - 2.0 * f);

    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));

    return mix(
            mix(a, b, f.x),
            mix(c, d, f.x),
            f.y
    );
}


float fbm(vec2 p)
{
    float value = 0.0;
    float amplitude = 0.5;

    for (int i = 0; i < 5; i++)
    {
        value += noise(p) * amplitude;

        p *= 2.02;
        amplitude *= 0.5;
    }

    return value;
}


// ============================================================
// Basic shapes
// ============================================================

float ring(
        vec2 p,
        float radius,
        float thickness
)
{
    float d = abs(length(p) - radius);

    return 1.0 -
    smoothstep(
            thickness,
            thickness + 0.004,
            d
    );
}


float radialGlow(
        vec2 p,
        float radius,
        float softness
)
{
    float d = length(p);

    return 1.0 -
    smoothstep(
            radius,
            radius + softness,
            d
    );
}


// ============================================================
// Tooltip coordinates
// ============================================================

vec2 tooltipUV()
{
    return vec2(
            (fragPos.x - TooltipX) / max(TooltipWidth, 1.0),
            (fragPos.y - TooltipY) / max(TooltipHeight, 1.0)
    );
}


// ============================================================
// Fixed-size clock coordinates
// ============================================================

vec2 clockUV()
{
    vec2 tooltipCenter = vec2(
            TooltipX + TooltipWidth * 0.5,
            TooltipY + TooltipHeight * 0.5
    );

    vec2 p = fragPos - tooltipCenter;

    p /= CLOCK_SIZE;

    return p + vec2(0.5);
}


float clockMask()
{
    vec2 p = clockUV() - vec2(0.5);

    float d = length(p);

    return 1.0 -
    smoothstep(
            0.485,
            0.515,
            d
    );
}


// ============================================================
// Coordinate helpers
// ============================================================

vec2 centered(vec2 uv)
{
    return uv - vec2(0.5);
}


float polarAngle(vec2 p)
{
    return atan(p.y, p.x);
}


// ============================================================
// Engraved ring
// ============================================================

float engravedRing(
        vec2 p,
        float radius,
        float thickness
)
{
    float d = abs(length(p) - radius);

    float dark =
    1.0 -
    smoothstep(
            thickness * 0.55,
            thickness,
            d
    );

    float bright =
    1.0 -
    smoothstep(
            thickness,
            thickness + 0.006,
            d
    );

    return bright * 0.75 + dark * 0.25;
}


// ============================================================
// Radial tick marks
// ============================================================

float radialTicks(
        vec2 p,
        float radius,
        float count,
        float width,
        float tickLength
)
{
    float r = length(p);
    float angle = atan(p.y, p.x);

    float sector =
    abs(
            fract(
                    angle / TAU * count + 0.5
            ) - 0.5
    );

    float angular =
    1.0 -
    smoothstep(
            width,
            width + 0.012,
            sector
    );

    float radial =
    1.0 -
    smoothstep(
            tickLength,
            tickLength + 0.012,
            abs(r - radius)
    );

    return angular * radial;
}

// ============================================================
// Major clock marks
// ============================================================

float majorMarks(vec2 p)
{
    float result = 0.0;

    float angle = atan(p.y, p.x);
    float radius = length(p);

    float sector =
    abs(
            fract(
                    angle / TAU * 12.0 + 0.5
            ) - 0.5
    );

    float angular =
    1.0 -
    smoothstep(
            0.025,
            0.040,
            sector
    );

    float radial =
    1.0 -
    smoothstep(
            0.415,
            0.428,
            radius
    );

    result += angular * radial;

    return result;
}


// ============================================================
// Diamond hour markers
// ============================================================

float diamond(
        vec2 p,
        float size
)
{
    float d =
    abs(p.x) +
    abs(p.y);

    return 1.0 -
    smoothstep(
            size,
            size + 0.012,
            d
    );
}


float hourDiamonds(vec2 p)
{
    float result = 0.0;

    float angle = atan(p.y, p.x);
    float radius = length(p);

    float sector =
    floor(
            angle / TAU * 12.0 + 0.5
    );

    float targetAngle =
    (sector + 0.0) *
    TAU / 12.0;

    vec2 q =
    vec2(
            cos(targetAngle),
            sin(targetAngle)
    ) *
    0.395;

    float d =
    length(p - q);

    result +=
    1.0 -
    smoothstep(
            0.018,
            0.032,
            d
    );

    return result;
}


// ============================================================
// Roman-style numeral strokes
//
// 数字そのものを文字として描画せず、
// 時計盤上に「古代刻印」らしい記号を作る。
// ============================================================

float numeralStroke(
        vec2 p,
        vec2 a,
        vec2 b,
        float thickness
)
{
    vec2 pa = p - a;
    vec2 ba = b - a;

    float h =
    clamp(
            dot(pa, ba) /
            dot(ba, ba),
            0.0,
            1.0
    );

    float d =
    length(
            pa - ba * h
    );

    return 1.0 -
    smoothstep(
            thickness,
            thickness + 0.008,
            d
    );
}


float numeral(vec2 p, int type)
{
    float result = 0.0;

    if (type == 1)
    {
        result += numeralStroke(
                p,
                vec2(0.0, -0.030),
                vec2(0.0, 0.030),
                0.008
        );

        result += numeralStroke(
                p,
                vec2(-0.014, -0.020),
                vec2(0.0, -0.030),
                0.006
        );
    }
    else if (type == 2)
    {
        result += numeralStroke(
                p,
                vec2(-0.018, -0.020),
                vec2(0.018, -0.020),
                0.006
        );

        result += numeralStroke(
                p,
                vec2(0.018, -0.020),
                vec2(-0.018, 0.020),
                0.006
        );

        result += numeralStroke(
                p,
                vec2(-0.018, 0.020),
                vec2(0.018, 0.020),
                0.006
        );
    }
    else if (type == 3)
    {
        result += numeralStroke(
                p,
                vec2(-0.018, -0.025),
                vec2(0.018, -0.025),
                0.006
        );

        result += numeralStroke(
                p,
                vec2(0.018, -0.025),
                vec2(-0.018, 0.0),
                0.006
        );

        result += numeralStroke(
                p,
                vec2(-0.018, 0.0),
                vec2(0.018, 0.025),
                0.006
        );

        result += numeralStroke(
                p,
                vec2(0.018, 0.025),
                vec2(-0.018, 0.025),
                0.006
        );
    }

    return result;
}


// ============================================================
// Decorative radial engraving
// ============================================================

float radialEngraving(
        vec2 p,
        float time
)
{
    float radius = length(p);
    float angle = atan(p.y, p.x);

    float pattern =
    sin(angle * 36.0);

    pattern =
    pow(
            abs(pattern),
            18.0
    );

    float radial =
    smoothstep(
            0.275,
            0.31,
            radius
    ) *
    (1.0 -
    smoothstep(
            0.315,
            0.35,
            radius
    ));

    return pattern * radial;
}


// ============================================================
// Gear-like outer teeth
// ============================================================

float gearTeeth(
        vec2 p,
        float radius,
        float count
)
{
    float r = length(p);
    float angle = atan(p.y, p.x);

    float teeth =
    cos(
            angle * count
    );

    teeth =
    smoothstep(
            0.55,
            0.90,
            teeth
    );

    float radial =
    1.0 -
    smoothstep(
            0.008,
            0.020,
            abs(
                    r - radius
            )
    );

    return teeth * radial;
}


// ============================================================
// Clock hands
// ============================================================

float hand(
        vec2 p,
        float angle,
        float handLength,
        float width,
        float tail
)
{
    vec2 dir =
    vec2(
            cos(angle + PI),
            sin(angle + PI)
    );

    vec2 side =
    vec2(
            -dir.y,
            dir.x
    );

    float along =
    dot(p, dir);

    float across =
    abs(dot(p, side));

    float body =
    1.0 -
    smoothstep(
            width,
            width + 0.006,
            across
    );

    // 中心から先端側
    float front =
    smoothstep(
            -0.002,
            0.006,
            along
    ) *
    (1.0 -
    smoothstep(
            handLength,
            handLength + 0.012,
            along
    ));

    float back =
    (1.0 -
    smoothstep(
            -tail - 0.008,
            -tail,
            along
    )) *
    (1.0 -
    smoothstep(
            -0.002,
            0.006,
            along
    ));

    return body * max(front, back);
}


// ============================================================
// Ornate hand tip
// ============================================================

float handTip(
        vec2 p,
        float angle,
        float radius
)
{
    vec2 center =
    vec2(
            cos(angle + PI),
            sin(angle + PI)
    ) * radius;

    float d =
    length(
            p - center
    );

    return 1.0 -
    smoothstep(
            0.012,
            0.024,
            d
    );
}

// ============================================================
// Clock hands
// ============================================================

float clockHands(vec2 p, float time)
{
    // 0:00:00 ～ 23:59:59
    float totalSeconds = mod(time, 86400.0);

    float hours = floor(totalSeconds / 3600.0);
    float minutes = floor(mod(totalSeconds, 3600.0) / 60.0);
    float seconds = mod(totalSeconds, 60.0);

    // --------------------------------------------------------
    // 秒針
    // 00秒 = 12時
    // 15秒 = 3時
    // 30秒 = 6時
    // 45秒 = 9時
    // --------------------------------------------------------

    float secondsAngle =
    -PI * 0.5
    + seconds / 60.0 * TAU;


    // --------------------------------------------------------
    // 分針
    //
    // 48分50秒なら
    // 48 + 50/60 分の位置
    // --------------------------------------------------------

    float minuteValue =
    minutes + seconds / 60.0;

    float minutesAngle =
    -PI * 0.5
    + minuteValue / 60.0 * TAU;


    // --------------------------------------------------------
    // 時針
    //
    // 11:48:50なら
    // 11 + 48/60 + 50/3600
    // --------------------------------------------------------

    float hourValue =
    mod(hours, 12.0)
    + minutes / 60.0
    + seconds / 3600.0;

    float hoursAngle =
    -PI * 0.5
    + hourValue / 12.0 * TAU;


    float result = 0.0;

    result += hand(
            p,
            hoursAngle,
            0.245,
            0.014,
            0.018
    );

    result += hand(
            p,
            minutesAngle,
            0.335,
            0.010,
            0.022
    ) * 0.95;

    result += hand(
            p,
            secondsAngle,
            0.375,
            0.004,
            0.030
    ) * 0.85;

    result += handTip(
            p,
            secondsAngle,
            0.375
    ) * 0.7;

    return clamp(result, 0.0, 1.0);
}

// ============================================================
// Central jewel
// ============================================================

float centerJewel(vec2 p)
{
    float d = length(p);

    float outer =
    1.0 -
    smoothstep(
            0.032,
            0.044,
            d
    );

    float inner =
    1.0 -
    smoothstep(
            0.012,
            0.020,
            d
    );

    return outer * 0.55 +
    inner * 0.75;
}


// ============================================================
// Clock face
// ============================================================

float clockFace(
        vec2 uv,
        float time
)
{
    vec2 p =
    uv - vec2(0.5);

    float result = 0.0;

    // Main engraved rings.
    result += engravedRing(
            p,
            0.470,
            0.006
    );

    result += engravedRing(
            p,
            0.450,
            0.003
    );

    result += engravedRing(
            p,
            0.425,
            0.003
    );

    result += engravedRing(
            p,
            0.365,
            0.002
    );

    result += engravedRing(
            p,
            0.255,
            0.002
    );

    // 60 minute marks.
    result +=
    radialTicks(
            p,
            0.410,
            60.0,
            0.060,
            0.026
    ) * 0.65;

    // 12 major marks.
    result +=
    radialTicks(
            p,
            0.408,
            12.0,
            0.085,
            0.040
    );

    // Diamond hour ornaments.
    result +=
    hourDiamonds(p) * 0.75;

    // Fine radial engraving.
    result +=
    radialEngraving(
            p,
            time
    ) * 0.5;

    // Outer gear.
    result +=
    gearTeeth(
            p,
            0.478,
            36.0
    ) * 0.65;

    return clamp(
            result,
            0.0,
            1.0
    );
}


// ============================================================
// Inner mechanism
// ============================================================

float mechanism(
        vec2 p,
        float time
)
{
    float result = 0.0;

    float angle =
    atan(p.y, p.x);

    float radius =
    length(p);

    // Inner rotating ring.
    float rotating =
    1.0 -
    smoothstep(
            0.004,
            0.012,
            abs(
                    radius - 0.285
            )
    );

    float teeth =
    pow(
            max(
                    cos(
                            angle * 16.0 +
                            time * 0.18
                    ),
                    0.0
            ),
            12.0
    );

    result +=
    rotating *
    teeth *
    0.8;

    // Small mechanical holes.
    float holes =
    pow(
            max(
                    sin(
                            angle * 8.0 -
                            time * 0.12
                    ),
                    0.0
            ),
            20.0
    );

    holes *=
    smoothstep(
            0.18,
            0.22,
            radius
    ) *
    (1.0 -
    smoothstep(
            0.25,
            0.29,
            radius
    ));

    result += holes;

    return result;
}


// ============================================================
// Gear wheel
// ============================================================

float gear(
        vec2 p,
        vec2 center,
        float radius,
        float teeth,
        float rotation
)
{
    vec2 q =
    p - center;

    float r =
    length(q);

    float a =
    atan(q.y, q.x);

    float tooth =
    cos(
            a * teeth +
            rotation
    );

    float outer =
    1.0 -
    smoothstep(
            0.008,
            0.020,
            abs(
                    r -
                    (
                    radius +
                    tooth * 0.012
                    )
            )
    );

    float inner =
    1.0 -
    smoothstep(
            0.012,
            0.022,
            r
    );

    float centerHole =
    1.0 -
    smoothstep(
            0.014,
            0.026,
            abs(
                    r -
                    radius * 0.38
            )
    );

    return outer * 0.8 +
    inner * 0.3 +
    centerHole * 0.6;
}


// ============================================================
// Mechanical gears
// ============================================================

float gears(
        vec2 p,
        float time
)
{
    float result = 0.0;

    result +=
    gear(
            p,
            vec2(-0.145, 0.115),
            0.080,
            12.0,
            time * 0.25
    ) * 0.75;

    result +=
    gear(
            p,
            vec2(0.145, 0.115),
            0.060,
            10.0,
            -time * 0.30
    ) * 0.65;

    result +=
    gear(
            p,
            vec2(0.0, -0.170),
            0.045,
            9.0,
            time * 0.38
    ) * 0.55;

    return result;
}


// ============================================================
// Engraved decorative arcs
// ============================================================

float decorativeArcs(
        vec2 p,
        float time
)
{
    float radius =
    length(p);

    float angle =
    atan(p.y, p.x);

    float result = 0.0;

    float arc1 =
    sin(
            angle * 6.0 +
            time * 0.10
    );

    arc1 =
    smoothstep(
            0.65,
            0.95,
            arc1
    );

    arc1 *=
    smoothstep(
            0.32,
            0.35,
            radius
    ) *
    (1.0 -
    smoothstep(
            0.37,
            0.39,
            radius
    ));

    result += arc1;

    float arc2 =
    sin(
            angle * 10.0 -
            time * 0.08
    );

    arc2 =
    pow(
            max(
                    arc2,
                    0.0
            ),
            8.0
    );

    arc2 *=
    smoothstep(
            0.405,
            0.42,
            radius
    ) *
    (1.0 -
    smoothstep(
            0.43,
            0.445,
            radius
    ));

    result += arc2;

    return result;
}


// ============================================================
// Clock glow
// ============================================================

float clockGlow(
        vec2 p
)
{
    float d =
    length(p);

    float glow =
    1.0 -
    smoothstep(
            0.25,
            0.58,
            d
    );

    return glow;
}


// ============================================================
// Background nebula
// ============================================================

vec3 nebula(
        vec2 uv,
        float time
)
{
    vec2 p =
    uv * 3.2;

    p.x +=
    time * 0.008;

    p.y -=
    time * 0.006;

    float n =
    fbm(p);

    float n2 =
    fbm(
            p * 1.8 +
            vec2(3.0, 7.0)
    );

    float cloud =
    smoothstep(
            0.30,
            0.75,
            n * 0.65 +
            n2 * 0.35
    );

    return
    vec3(
            0.008,
            0.004,
            0.015
    )
    +
    cloud *
    vec3(
            0.035,
            0.008,
            0.055
    );
}


// ============================================================
// Stars
// ============================================================

float stars(
        vec2 uv,
        float time
)
{
    // --------------------------------------------------------
    // 星の密度
    // --------------------------------------------------------

    vec2 p = uv * 30.0;

    vec2 cell = floor(p);
    vec2 local = fract(p) - 0.5;

    float h = hash21(cell);

    // そこそこ星を出す
    float starChance =
    smoothstep(
            0.72,
            0.94,
            h
    );

    if (starChance <= 0.0)
    return 0.0;


    // --------------------------------------------------------
    // 星ごとの個体差
    // --------------------------------------------------------

    float seed =
    hash21(
            cell + vec2(17.3, 8.1)
    );

    float size =
    mix(
            0.055,
            0.115,
            seed
    );

    float brightness =
    mix(
            0.65,
            1.35,
            hash21(
                    cell + vec2(4.7, 21.9)
            )
    );


    // --------------------------------------------------------
    // ゆっくり明滅
    // --------------------------------------------------------

    float twinkleSpeed =
    mix(
            0.8,
            2.2,
            hash21(
                    cell + vec2(31.2, 14.7)
            )
    );

    float phase =
    hash21(
            cell + vec2(12.8, 44.1)
    ) * TAU;

    float twinkle =
    0.72 +
    0.28 *
    sin(
            time * twinkleSpeed +
            phase
    );

    brightness *= twinkle;


    // --------------------------------------------------------
    // 距離
    // --------------------------------------------------------

    float d =
    length(local);


    // ========================================================
    // 中心核
    // ========================================================

    float core =
    1.0 -
    smoothstep(
            size * 0.10,
            size * 0.30,
            d
    );


    // ========================================================
    // 柔らかい光輪
    // ========================================================

    float halo =
    1.0 -
    smoothstep(
            size * 0.20,
            size * 2.8,
            d
    );

    halo *= 0.42;


    // ========================================================
    // 縦方向の回折光
    // ========================================================

    float vertical =
    1.0 -
    smoothstep(
            0.0,
            size * 4.5,
            abs(local.x)
    );

    vertical *=
    1.0 -
    smoothstep(
            size * 0.12,
            size * 0.45,
            abs(local.y)
    );


    // ========================================================
    // 横方向の回折光
    // ========================================================

    float horizontal =
    1.0 -
    smoothstep(
            0.0,
            size * 4.5,
            abs(local.y)
    );

    horizontal *=
    1.0 -
    smoothstep(
            size * 0.12,
            size * 0.45,
            abs(local.x)
    );


    // ========================================================
    // X字の魔力回折
    // ========================================================

    float diagonal1 =
    1.0 -
    smoothstep(
            0.0,
            size * 3.2,
            abs(local.x - local.y)
    );

    diagonal1 *=
    1.0 -
    smoothstep(
            size * 0.10,
            size * 0.32,
            abs(local.x + local.y)
    );


    float diagonal2 =
    1.0 -
    smoothstep(
            0.0,
            size * 3.2,
            abs(local.x + local.y)
    );

    diagonal2 *=
    1.0 -
    smoothstep(
            size * 0.10,
            size * 0.32,
            abs(local.x - local.y)
    );


    // ========================================================
    // 回折光
    // ========================================================

    float diffraction =
    vertical * 0.75 +
    horizontal * 0.75 +
    diagonal1 * 0.28 +
    diagonal2 * 0.28;


    // ========================================================
    // 魔力の縦筋
    // ========================================================

    float magicRay =
    pow(
            max(
                    0.0,
                    1.0 -
                    abs(local.x) / max(size * 5.0, 0.001)
            ),
            3.0
    );

    magicRay *=
    exp(
            -abs(local.y) /
            max(size * 1.8, 0.001)
    );


    // ========================================================
    // 合成
    // ========================================================

    float result =
    core * 1.8 +
    halo +
    diffraction * 0.72 +
    magicRay * 0.35;


    return
    result *
    brightness *
    starChance;
}

// ============================================================
// Magic dust
// ============================================================

float magicDust(
        vec2 uv,
        float time
)
{
    vec2 p =
    uv * 90.0;

    vec2 cell =
    floor(p);

    vec2 local =
    fract(p) - 0.5;

    float h =
    hash21(cell);

    float size =
    mix(
            0.008,
            0.028,
            h
    );

    float dust =
    1.0 -
    smoothstep(
            size,
            size + 0.025,
            length(local)
    );

    dust *=
    smoothstep(
            0.72,
            0.96,
            h
    );

    float movement =
    sin(
            time * 1.5 +
            h * 80.0
    );

    dust *=
    0.45 +
    movement * 0.25;

    return dust;
}


// ============================================================
// Tooltip frame
// ============================================================

float outerBorder(
        vec2 uv
)
{
    float d =
    min(
            min(
                    uv.x,
                    1.0 - uv.x
            ),
            min(
                    uv.y,
                    1.0 - uv.y
            )
    );

    return 1.0 -
    smoothstep(
            0.010,
            0.028,
            d
    );
}


float innerBorder(
        vec2 uv
)
{
    float d =
    min(
            min(
                    uv.x,
                    1.0 - uv.x
            ),
            min(
                    uv.y,
                    1.0 - uv.y
            )
    );

    return 1.0 -
    smoothstep(
            0.030,
            0.050,
            d
    );
}

float secondMagicWave(
        vec2 p,
        float time
)
{
    float phase = fract(time);
    float radius = length(p);

    // ========================================================
    // 中心 → 時計の外側まで広がる
    // ========================================================

    float waveRadius =
    phase * 0.90;

    float distance =
    abs(radius - waveRadius);


    // ========================================================
    // 波そのもの
    // ========================================================

    float wave =
    1.0 -
    smoothstep(
            0.0,
            0.014,
            distance
    );


    // ========================================================
    // 波の周囲に広がる魔力
    // ========================================================

    float aura =
    1.0 -
    smoothstep(
            0.008,
            0.075,
            distance
    );


    // ========================================================
    // 中心から発生
    // ========================================================

    float centerFade =
    smoothstep(
            0.015,
            0.085,
            radius
    );


    // ========================================================
    // 外側まで届くほど少しずつ減衰
    // ========================================================

    float distanceFade =
    1.0 -
    smoothstep(
            0.55,
            0.90,
            waveRadius
    ) * 0.35;


    // ========================================================
    // 中心に残る余韻
    // ========================================================

    float residue =
    (1.0 - phase) *
    (1.0 -
    smoothstep(
            0.0,
            0.16,
            radius
    ));


    return
    (
    wave * 0.85 +
    aura * 0.20 +
    residue * 0.12
    )
    * centerFade
    * distanceFade;
}

float starMagicWave(
        vec2 p,
        float time
)
{
    float phase =
    fract(time);

    float radius =
    length(p);

    // 時計の中心から外側へ
    // 0.47 = 時計外周
    // 1.20 = 星まで届く範囲
    float waveRadius =
    phase * 1.20;

    float distance =
    abs(radius - waveRadius);


    // ========================================================
    // 波そのもの
    // ========================================================

    float wave =
    1.0 -
    smoothstep(
            0.0,
            0.012,
            distance
    );


    // ========================================================
    // 波の周囲に広がる魔力
    // ========================================================

    float aura =
    1.0 -
    smoothstep(
            0.008,
            0.055,
            distance
    );


    // ========================================================
    // 時計内部では強く
    // 外へ出るほど自然に減衰
    // ========================================================

    float distanceFade =
    1.0 -
    smoothstep(
            0.45,
            1.15,
            waveRadius
    ) * 0.65;


    // ========================================================
    // 時計中心付近では波を発生させる
    // ========================================================

    float centerFade =
    smoothstep(
            0.025,
            0.10,
            radius
    );


    return
    (
    wave * 0.85 +
    aura * 0.35
    )
    *
    centerFade
    *
    distanceFade;
}

// ============================================================
// Main
// ============================================================

void main()
{
    vec2 uv =
    tooltipUV();

    if (
        uv.x < -0.02 ||
        uv.x > 1.02 ||
        uv.y < -0.02 ||
        uv.y > 1.02
    )
    {
        discard;
    }

    float time =
    NostrumTime;




    // ========================================================
    // Base
    // ========================================================

    vec3 color =
    vec3(
            0.005,
            0.003,
            0.010
    );


    // ========================================================
    // Background
    // ========================================================


    color +=
    nebula(
            uv,
            time
    );


    float starField =
    stars(
            uv,
            time
    );


    // ========================================================
    // 星への魔力干渉
    // 時計の中心から外側へ広がる
    // ========================================================

    vec2 tooltipCenter =
    vec2(
            TooltipX + TooltipWidth * 0.5,
            TooltipY + TooltipHeight * 0.5
    );

    vec2 starP =
    fragPos - tooltipCenter;

    starP /=
    CLOCK_SIZE;

    float magicInfluence =
    secondMagicWave(
            starP,
            time
    );


    // ========================================================
    // 星の色
    // ========================================================

    vec3 starWhite =
    vec3(
            0.72,
            0.72,
            0.72
    );

    vec3 starMagic =
    vec3(
            0.55,
            0.12,
            0.95
    );

    float starMagicAmount =
    clamp(
            magicInfluence * 2.0,
            0.0,
            1.0
    );

    vec3 starColor =
    mix(
            starWhite,
            starMagic,
            starMagicAmount
    );

    color +=
    starField *
    starColor;


    // ========================================================
    // Clock coordinates
    // ========================================================

    vec2 cuv =
    clockUV();

    vec2 p =
    cuv - vec2(0.5);

    float mask =
    clockMask();


    // ========================================================
    // Clock aura
    // ========================================================

    float glow =
    clockGlow(p);

    color +=
    glow *
    mask *
    vec3(
            0.025,
            0.007,
            0.035
    );

    float wave =
    secondMagicWave(
            p,
            time
    );

    color +=
    wave *
    vec3(
            0.035,
            0.008,
            0.055
    );


    // ========================================================
    // Clock face
    // ========================================================

    float face =
    1.0 -
    smoothstep(
            0.455,
            0.485,
            length(p)
    );

    color +=
    face *
    mask *
    vec3(
            0.018,
            0.009,
            0.012
    );


    // ========================================================
    // Main engraved structure
    // ========================================================

    float engraving =
    clockFace(
            cuv,
            time
    );

    color +=
    engraving *
    mask *
    vec3(
            0.22,
            0.085,
            0.018
    );


    // ========================================================
    // Dark engraving shadow
    // ========================================================

    float engravingShadow =
    clockFace(
            cuv + vec2(0.003, 0.003),
            time
    );

    color -=
    engravingShadow *
    mask *
    vec3(
            0.035,
            0.015,
            0.006
    );


    // ========================================================
    // Mechanical gears
    // ========================================================

    float gearPattern =
    gears(
            p,
            time
    );

    color +=
    gearPattern *
    mask *
    vec3(
            0.105,
            0.038,
            0.010
    );


    // ========================================================
    // Decorative arcs
    // ========================================================

    float arcs =
    decorativeArcs(
            p,
            time
    );

    color +=
    arcs *
    mask *
    vec3(
            0.15,
            0.045,
            0.015
    );


    // ========================================================
    // Inner mechanism
    // ========================================================

    float mech =
    mechanism(
            p,
            time
    );

    color +=
    mech *
    mask *
    vec3(
            0.13,
            0.045,
            0.012
    );


    // ========================================================
    // Clock hands
    // ========================================================

    float hands =
    clockHands(
            p,
            RealTime
    );

    color +=
    hands *
    mask *
    vec3(
            0.32,
            0.12,
            0.025
    );


    // ========================================================
    // Center jewel
    // ========================================================

    float jewel =
    centerJewel(p);

    color +=
    jewel *
    mask *
    vec3(
            0.38,
            0.12,
            0.035
    );


    // ========================================================
    // Center glow
    // ========================================================

    float centerGlow =
    radialGlow(
            p,
            0.035,
            0.045
    );

    color +=
    centerGlow *
    mask *
    vec3(
            0.20,
            0.025,
            0.04
    );


    // ========================================================
    // Outer mechanical teeth
    // ========================================================

    float teeth =
    gearTeeth(
            p,
            0.482,
            36.0
    );

    color +=
    teeth *
    mask *
    vec3(
            0.13,
            0.045,
            0.012
    );


    // ========================================================
    // Clock edge
    // ========================================================

    float clockEdge =
    ring(
            p,
            0.475,
            0.006
    );

    color +=
    clockEdge *
    mask *
    vec3(
            0.28,
            0.085,
            0.015
    );


    // ========================================================
    // Inner clock edge
    // ========================================================

    float innerClockEdge =
    ring(
            p,
            0.435,
            0.003
    );

    color +=
    innerClockEdge *
    mask *
    vec3(
            0.12,
            0.035,
            0.010
    );


    // ========================================================
    // Tooltip frame
    // ========================================================

    float inner =
    innerBorder(
            uv
    );

    color +=
    inner *
    vec3(
            0.045,
            0.012,
            0.060
    );


    // ========================================================
    // Black outer edge
    // ========================================================

    float edge =
    outerBorder(
            uv
    );

    color =
    mix(
            color,
            vec3(
                    0.001,
                    0.0005,
                    0.002
            ),
            edge * 0.82
    );


    // ========================================================
    // Violet rim
    // ========================================================

    color +=
    edge *
    vec3(
            0.035,
            0.006,
            0.050
    );


    // ========================================================
    // Vignette
    // ========================================================

    vec2 center =
    uv - vec2(0.5);

    float vignette =
    1.0 -
    smoothstep(
            0.30,
            0.72,
            length(center)
    );

    color *=
    0.72 +
    vignette * 0.28;


    // ========================================================
    // Tone mapping
    // ========================================================

    color =
    pow(
            max(
                    color,
                    vec3(0.0)
            ),
            vec3(0.92)
    );


    // ========================================================
    // Final
    // ========================================================

    fragColor =
    vec4(
            color,
            0.94
    );
}