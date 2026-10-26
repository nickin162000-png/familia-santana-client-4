package com.familiasantana.client;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Config {
    public static final String[] NAMES = {
            // HUD
            "Marca", "FPS", "CPS", "Ping", "Coordenadas", "Direcao", "Relogio",
            "Keystrokes", "Armor HUD", "Efeitos",
            // Utilidades
            "Toggle Sprint", "Zoom", "Fullbright",
            // Interface
            "Hotbar Moderna", "Barras de Status", "Mira Moderna", "Menus Modernos",
            // Aparencia
            "Fundo HUD", "Sombra", "Fonte Moderna", "Blur"
    };

    public static final int[] ACCENTS = {
            0xFF7C5CFF, 0xFF2D9CFF, 0xFF2ED573, 0xFFFF4757, 0xFFFFA502, 0xFFFF6BCB
    };

    private static final Map<String, Boolean> MAP = new HashMap<String, Boolean>();
    private static final Map<String, String> EXTRA = new HashMap<String, String>();
    private static int accent = 0;
    private static File file;

    public static void load() {
        file = new File(Minecraft.getMinecraft().mcDataDir, "config/familiasantana.properties");
        Properties p = new Properties();
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                p.load(in);
            } catch (IOException ignored) {
            }
        }
        for (String n : NAMES) {
            boolean def = !n.equals("Fullbright");
            MAP.put(n, Boolean.parseBoolean(p.getProperty(n, String.valueOf(def))));
        }
        for (String key : p.stringPropertyNames()) {
            if (key.startsWith("pos.")) EXTRA.put(key, p.getProperty(key));
        }
        try {
            accent = Integer.parseInt(p.getProperty("accent", "0"));
        } catch (NumberFormatException e) {
            accent = 0;
        }
        if (accent < 0 || accent >= ACCENTS.length) accent = 0;
    }

    public static void save() {
        Properties p = new Properties();
        for (String n : NAMES) p.setProperty(n, String.valueOf(get(n)));
        p.setProperty("accent", String.valueOf(accent));
        for (Map.Entry<String, String> en : EXTRA.entrySet()) p.setProperty(en.getKey(), en.getValue());
        try {
            file.getParentFile().mkdirs();
            try (OutputStream out = new FileOutputStream(file)) {
                p.store(out, "Familia Santana Client");
            }
        } catch (IOException ignored) {
        }
    }

    public static boolean get(String name) {
        Boolean b = MAP.get(name);
        return b != null && b;
    }

    public static void toggle(String name) {
        MAP.put(name, !get(name));
    }

    public static float getFloat(String key, float def) {
        String v = EXTRA.get(key);
        if (v == null) return def;
        try {
            return Float.parseFloat(v);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static void setFloat(String key, float value) {
        EXTRA.put(key, String.valueOf(value));
    }

    public static int accent() {
        return ACCENTS[accent];
    }

    public static int accentIndex() {
        return accent;
    }

    public static void setAccent(int i) {
        if (i >= 0 && i < ACCENTS.length) accent = i;
    }
}
