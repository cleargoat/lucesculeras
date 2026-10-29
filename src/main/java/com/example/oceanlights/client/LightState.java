package com.example.oceanlights.client;

import net.minecraft.util.Mth;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Estado de la luz (color, intensidad, encendida o apagada). */
public final class LightState {
    public static final Map<String, Integer> NAMES = new LinkedHashMap<>();

    static {
        // Ingles
        NAMES.put("yellow", 0xFFE933);
        NAMES.put("orange", 0xFF7A0A);
        NAMES.put("red", 0xFF1A1A);
        NAMES.put("blue", 0x2F52FF);
        NAMES.put("pink", 0xFF5CBE);
        NAMES.put("white", 0xFFFFFF);
        NAMES.put("green", 0x20E040);
        NAMES.put("lime", 0xA6FF2E);
        NAMES.put("cyan", 0x20E8FF);
        NAMES.put("aqua", 0x20E8FF);
        NAMES.put("purple", 0x9A33FF);
        NAMES.put("violet", 0xB04CFF);
        NAMES.put("magenta", 0xFF22E0);
        NAMES.put("gold", 0xFFC21A);
        NAMES.put("lightblue", 0x6FC3FF);
        NAMES.put("turquoise", 0x30E0C0);
        NAMES.put("brown", 0xA05A20);
        // Espanol
        NAMES.put("amarillo", 0xFFE933);
        NAMES.put("naranja", 0xFF7A0A);
        NAMES.put("rojo", 0xFF1A1A);
        NAMES.put("azul", 0x2F52FF);
        NAMES.put("rosa", 0xFF5CBE);
        NAMES.put("blanco", 0xFFFFFF);
        NAMES.put("verde", 0x20E040);
        NAMES.put("celeste", 0x6FC3FF);
        NAMES.put("morado", 0x9A33FF);
        NAMES.put("dorado", 0xFFC21A);
    }

    private static volatile boolean enabled = false;
    private static volatile boolean rainbow = false;
    private static volatile int rgb = 0xFFFFFF;
    private static volatile float intensity = 1.0f;

    private LightState() {
    }

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean v) { enabled = v; }
    public static float getIntensity() { return intensity; }
    public static void setIntensity(float v) { intensity = v; }

    /** Devuelve true si el color es valido: nombre, "rainbow"/"arcoiris" o hexadecimal de 6 digitos. */
    public static boolean setColor(String text) {
        String t = text.toLowerCase(Locale.ROOT).trim();
        if (t.startsWith("#")) t = t.substring(1);
        if (t.equals("rainbow") || t.equals("arcoiris")) {
            rainbow = true;
            enabled = true;
            return true;
        }
        Integer c = NAMES.get(t);
        if (c == null && t.matches("[0-9a-f]{6}")) {
            c = Integer.parseInt(t, 16);
        }
        if (c == null) return false;
        rainbow = false;
        rgb = c;
        enabled = true;
        return true;
    }

    /** Color actual como {r, g, b} entre 0 y 1. */
    public static float[] currentColor(float timeSeconds) {
        int c = rainbow ? Mth.hsvToRgb((timeSeconds * 0.08f) % 1f, 0.85f, 1f) : rgb;
        return new float[]{((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f};
    }
}
