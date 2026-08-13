#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

uniform float NostrumTime;
uniform float Speed;
uniform float Intensity;

out vec4 vertexColor;
out vec2 texCoord0;

out float magicOffset;


// ============================================================
// Main
// ============================================================

void main() {

    // --------------------------------------------------------
    // Glyph geometry MUST remain untouched.
    // --------------------------------------------------------

    gl_Position =
    ProjMat *
    ModelViewMat *
    vec4(Position, 1.0);


    vertexColor = Color;
    texCoord0 = UV0;


    // --------------------------------------------------------
    // Glyph specific magic phase
    // --------------------------------------------------------

    float time =
    NostrumTime *
    Speed;


    float wave1 =
    sin(
            Position.x * 7.0
            +
            Position.y * 5.0
            +
            time * 1.7
    );


    float wave2 =
    sin(
            Position.x * 13.0
            -
            Position.y * 4.0
            -
            time * 1.1
    );


    float wave3 =
    sin(
            Position.x * 3.0
            +
            Position.y * 17.0
            +
            time * 0.7
    );


    magicOffset =
    (
    wave1 * 0.50
    +
    wave2 * 0.30
    +
    wave3 * 0.20
    )
    *
    Intensity;
}