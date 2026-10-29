#version 150

uniform sampler2D DepthSampler;
uniform mat4 InvProj;
uniform mat4 Proj;
uniform vec3 LightPos;      // posicion de la luz (espacio de camara)
uniform vec4 LightColor;    // rgb = color, a = intensidad
uniform vec4 LightParams;   // x = alcance, y = cos(cono interior), z = cos(cono exterior)

in vec2 texCoord;
out vec4 fragColor;

vec3 viewPos(vec2 uv, float d) {
    vec4 ndc = vec4(uv * 2.0 - 1.0, d * 2.0 - 1.0, 1.0);
    vec4 v = InvProj * ndc;
    return v.xyz / v.w;
}

void main() {
    float d = texture(DepthSampler, texCoord).r;
    if (d >= 0.99999 || LightColor.a <= 0.001) {
        discard;
    }

    vec3 p = viewPos(texCoord, d);

    vec3 n = normalize(cross(dFdx(p), dFdy(p)));
    if (dot(n, p) > 0.0) {
        n = -n;
    }

    vec3 toL = LightPos - p;
    float dist = length(toL);
    vec3 L = toL / dist;

    float ndl = max(dot(n, L), 0.0);

    // Cono de luz que apunta hacia donde miras (eje = -Z en espacio de camara)
    float cosA = dot(-L, vec3(0.0, 0.0, -1.0));
    float spot = smoothstep(LightParams.z, LightParams.y, cosA);

    float atten = pow(clamp(1.0 - dist / LightParams.x, 0.0, 1.0), 2.0);
    float halo = pow(clamp(1.0 - dist / 7.0, 0.0, 1.0), 2.0) * 0.18;   // brillo suave alrededor tuyo

    float lum = (spot * atten + halo) * (0.15 + 0.85 * ndl);
    if (lum <= 0.002) {
        discard;
    }

    // Sombras en espacio de pantalla hacia la luz
    float shadow = 1.0;
    vec3 q = p + n * 0.08;
    float maxT = min(dist, 12.0);
    for (int i = 1; i <= 10; i++) {
        vec3 s = q + L * (maxT * float(i) / 11.0);
        vec4 clip = Proj * vec4(s, 1.0);
        if (clip.w <= 0.0) {
            break;
        }
        vec2 suv = (clip.xy / clip.w) * 0.5 + 0.5;
        if (suv.x < 0.0 || suv.x > 1.0 || suv.y < 0.0 || suv.y > 1.0) {
            break;
        }
        float sd = texture(DepthSampler, suv).r;
        vec3 sp = viewPos(suv, sd);
        float occ = sp.z - s.z;
        if (occ > 0.15 && occ < 2.5) {
            shadow = 0.0;
            break;
        }
    }

    fragColor = vec4(LightColor.rgb * LightColor.a * lum * shadow, 1.0);
}
