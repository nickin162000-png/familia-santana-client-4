package com.familiasantana.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Hud {

    public static boolean editing = false;
    public static final List<El> ELEMENTS = new ArrayList<El>();

    private static final SimpleDateFormat CLOCK = new SimpleDateFormat("HH:mm:ss");
    private static final List<Long> LEFT = new ArrayList<Long>();
    private static final List<Long> RIGHT = new ArrayList<Long>();
    private static final float[] KEY_ANIM = new float[7];
    private static final int BG = 0x8C0E0E12;

    /** Um elemento do HUD que pode ser arrastado. fx/fy = posicao como fracao da tela. */
    public static class El {
        public final String name;
        private final float dfx, dfy;
        public float fx, fy;
        public float x, y, w, h;

        El(String name, float px, float py) {
            this.name = name;
            this.dfx = px / 960F;
            this.dfy = py / 540F;
            this.fx = Config.getFloat("pos." + name + ".x", dfx);
            this.fy = Config.getFloat("pos." + name + ".y", dfy);
        }

        public void reset() {
            fx = dfx;
            fy = dfy;
        }
    }

    public static void init() {
        ELEMENTS.clear();
        ELEMENTS.add(new El("Marca", 4, 4));
        ELEMENTS.add(new El("FPS", 4, 24));
        ELEMENTS.add(new El("CPS", 4, 42));
        ELEMENTS.add(new El("Ping", 4, 60));
        ELEMENTS.add(new El("Coordenadas", 4, 78));
        ELEMENTS.add(new El("Direcao", 4, 96));
        ELEMENTS.add(new El("Relogio", 4, 114));
        ELEMENTS.add(new El("Keystrokes", 4, 447));
        ELEMENTS.add(new El("Armor HUD", 902, 225));
        ELEMENTS.add(new El("Efeitos", 840, 4));
    }

    public static void resetAll() {
        for (El e : ELEMENTS) e.reset();
    }

    public static void savePositions() {
        for (El e : ELEMENTS) {
            Config.setFloat("pos." + e.name + ".x", e.fx);
            Config.setFloat("pos." + e.name + ".y", e.fy);
        }
        Config.save();
    }

    public static void click(int button) {
        long now = System.currentTimeMillis();
        if (button == 0) LEFT.add(now);
        else if (button == 1) RIGHT.add(now);
    }

    private static void prune(List<Long> list, long now) {
        Iterator<Long> it = list.iterator();
        while (it.hasNext()) {
            if (now - it.next() > 1000) it.remove();
        }
    }

    // ---------- render ----------

    public static void render(Minecraft mc, ScaledResolution sr) {
        long now = System.currentTimeMillis();
        prune(LEFT, now);
        prune(RIGHT, now);

        float sw = sr.getScaledWidth();
        float sh = sr.getScaledHeight();

        for (El e : ELEMENTS) {
            if (!Config.get(e.name)) continue;
            e.x = e.fx * sw;
            e.y = e.fy * sh;

            String n = e.name;
            if (n.equals("Marca")) brand(e);
            else if (n.equals("FPS")) pill(e, "FPS", String.valueOf(Minecraft.getDebugFPS()));
            else if (n.equals("CPS")) pill(e, "CPS", LEFT.size() + " | " + RIGHT.size());
            else if (n.equals("Ping")) pill(e, "Ping", ping(mc) + " ms");
            else if (n.equals("Coordenadas")) {
                pill(e, "XYZ", MathHelper.floor_double(mc.thePlayer.posX) + " "
                        + MathHelper.floor_double(mc.thePlayer.posY) + " "
                        + MathHelper.floor_double(mc.thePlayer.posZ));
            } else if (n.equals("Direcao")) pill(e, "Dir", mc.thePlayer.getHorizontalFacing().getName());
            else if (n.equals("Relogio")) pill(e, "Hora", CLOCK.format(new Date()));
            else if (n.equals("Keystrokes")) keystrokes(e, mc);
            else if (n.equals("Armor HUD")) armor(e, mc);
            else if (n.equals("Efeitos")) effects(e, mc);
        }
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private static int ping(Minecraft mc) {
        if (mc.getNetHandler() == null) return 0;
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        return info == null ? 0 : info.getResponseTime();
    }

    private static void brand(El e) {
        String t = "Familia Santana";
        float w = Fonts.width(t, 1.1F, true) + 16;
        float h = 17;
        e.w = w;
        e.h = h;
        if (Config.get("Fundo HUD")) RenderUtil.roundedRect(e.x, e.y, e.x + w, e.y + h, 4, BG);
        RenderUtil.roundedRect(e.x + 4, e.y + 4, e.x + 6, e.y + h - 4, 1F, Config.accent());
        Fonts.drawC(t, e.x + 10, e.y + h / 2F, Config.accent(), 1.1F, true, Config.get("Sombra"));
    }

    private static void pill(El e, String label, String value) {
        boolean sh = Config.get("Sombra");
        float lw = Fonts.width(label + " ", 1F, false);
        float vw = Fonts.width(value, 1F, true);
        float h = 15;
        float w = lw + vw + 12;
        e.w = w;
        e.h = h;
        if (Config.get("Fundo HUD")) RenderUtil.roundedRect(e.x, e.y, e.x + w, e.y + h, 4, BG);
        float cy = e.y + h / 2F;
        Fonts.drawC(label + " ", e.x + 6, cy, 0xFFB4B4BE, 1F, false, sh);
        Fonts.drawC(value, e.x + 6 + lw, cy, 0xFFFFFFFF, 1F, true, sh);
    }

    // ---------- keystrokes ----------

    private static void keystrokes(El e, Minecraft mc) {
        int s = 22;
        int g = 3;
        float x = e.x;
        float y = e.y;
        e.w = s * 3 + g * 2;
        e.h = s * 3 + g * 3 + 12;

        boolean[] down = {
                mc.gameSettings.keyBindForward.isKeyDown(),
                mc.gameSettings.keyBindLeft.isKeyDown(),
                mc.gameSettings.keyBindBack.isKeyDown(),
                mc.gameSettings.keyBindRight.isKeyDown(),
                Mouse.isButtonDown(0),
                Mouse.isButtonDown(1),
                mc.gameSettings.keyBindJump.isKeyDown()
        };
        for (int i = 0; i < KEY_ANIM.length; i++) {
            KEY_ANIM[i] += ((down[i] ? 1F : 0F) - KEY_ANIM[i]) * 0.3F;
        }

        keyBox(0, "W", x + s + g, y, s, s, null);
        keyBox(1, "A", x, y + s + g, s, s, null);
        keyBox(2, "S", x + s + g, y + s + g, s, s, null);
        keyBox(3, "D", x + (s + g) * 2, y + s + g, s, s, null);

        int mw = (s * 3 + g * 2 - g) / 2;
        float my = y + (s + g) * 2;
        keyBox(4, "LMB", x, my, mw, s, LEFT.size() + " CPS");
        keyBox(5, "RMB", x + mw + g, my, mw, s, RIGHT.size() + " CPS");

        float sy = my + s + g;
        float sw = s * 3 + g * 2;
        RenderUtil.roundedRect(x, sy, x + sw, sy + 12, 4,
                RenderUtil.lerpColor(BG, RenderUtil.withAlpha(Config.accent(), 0xD0), KEY_ANIM[6]));
        RenderUtil.roundedRect(x + sw / 2F - 12, sy + 5, x + sw / 2F + 12, sy + 7, 1F, 0xFFFFFFFF);
    }

    private static void keyBox(int idx, String label, float x, float y, float w, float h, String sub) {
        RenderUtil.roundedRect(x, y, x + w, y + h, 4,
                RenderUtil.lerpColor(BG, RenderUtil.withAlpha(Config.accent(), 0xD0), KEY_ANIM[idx]));
        boolean sh = Config.get("Sombra");
        if (sub == null) {
            float tw = Fonts.width(label, 1F, true);
            Fonts.drawC(label, x + (w - tw) / 2F, y + h / 2F, 0xFFFFFFFF, 1F, true, sh);
        } else {
            float tw = Fonts.width(label, 0.9F, true);
            Fonts.drawC(label, x + (w - tw) / 2F, y + 8, 0xFFFFFFFF, 0.9F, true, sh);
            float sw = Fonts.width(sub, 0.7F, false);
            Fonts.drawC(sub, x + (w - sw) / 2F, y + h - 6, 0xFFC8C8D2, 0.7F, false, sh);
        }
    }

    // ---------- armor ----------

    private static void armor(El e, Minecraft mc) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        stacks.add(mc.thePlayer.getHeldItem());
        for (int i = 3; i >= 0; i--) stacks.add(mc.thePlayer.inventory.armorInventory[i]);

        e.w = 52;
        e.h = stacks.size() * 18 + 6;
        if (Config.get("Fundo HUD")) {
            RenderUtil.roundedRect(e.x + 26, e.y, e.x + 52, e.y + e.h, 5, BG);
        }

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            float iy = e.y + 4 + i * 18;
            if (stack == null) continue;

            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int) (e.x + 31), (int) iy);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, (int) (e.x + 31), (int) iy);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
            GlStateManager.disableLighting();

            if (stack.isItemStackDamageable()) {
                int left = stack.getMaxDamage() - stack.getItemDamage();
                float pct = left / (float) stack.getMaxDamage();
                String dur = String.valueOf(left);
                float w = Fonts.width(dur, 0.9F, true);
                Fonts.drawC(dur, e.x + 22 - w, iy + 8,
                        RenderUtil.lerpColor(0xFFFF4757, 0xFF2ED573, pct), 0.9F, true, true);
            }
        }
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    // ---------- efeitos ----------

    private static void effects(El e, Minecraft mc) {
        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        List<String> names = new ArrayList<String>();
        List<String> times = new ArrayList<String>();
        for (PotionEffect effect : effects) {
            String name = I18n.format(effect.getEffectName());
            if (effect.getAmplifier() > 0) name += " " + (effect.getAmplifier() + 1);
            names.add(name);
            times.add(Potion.getDurationString(effect));
        }
        if (names.isEmpty() && editing) {
            names.add("Velocidade II");
            times.add("0:30");
        }
        if (names.isEmpty()) {
            e.w = 0;
            e.h = 0;
            return;
        }

        boolean sh = Config.get("Sombra");
        float y = e.y;
        float maxW = 0;
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i) + " ";
            String time = times.get(i);
            float nw = Fonts.width(name, 1F, false);
            float tw = Fonts.width(time, 1F, true);
            float w = nw + tw + 12;
            float h = 15;
            if (Config.get("Fundo HUD")) RenderUtil.roundedRect(e.x, y, e.x + w, y + h, 4, BG);
            Fonts.drawC(name, e.x + 6, y + h / 2F, 0xFFFFFFFF, 1F, false, sh);
            Fonts.drawC(time, e.x + 6 + nw, y + h / 2F, Config.accent(), 1F, true, sh);
            if (w > maxW) maxW = w;
            y += h + 3;
        }
        e.w = maxW;
        e.h = y - e.y - 3;
    }
}
