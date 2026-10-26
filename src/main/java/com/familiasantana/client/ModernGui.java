package com.familiasantana.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModernGui extends GuiScreen {

    private static final int PW = 440;
    private static final int PH = 270;
    private static final String[] TABS = {"HUD", "Utilidades", "Interface", "Aparencia"};

    private int tab = 0;
    private long openTime;
    private boolean blurOn = false;
    private final Map<String, Float> anim = new HashMap<String, Float>();
    private final float[] tabAnim = new float[TABS.length];
    private float editAnim = 0F;

    @Override
    public void initGui() {
        openTime = System.currentTimeMillis();
        for (String n : Config.NAMES) anim.put(n, Config.get(n) ? 1F : 0F);
        for (int i = 0; i < tabAnim.length; i++) tabAnim[i] = i == tab ? 1F : 0F;
        applyBlur();
    }

    @Override
    public void onGuiClosed() {
        try {
            if (blurOn && mc.entityRenderer.isShaderActive()) mc.entityRenderer.stopUseShader();
        } catch (Throwable ignored) {
        }
        blurOn = false;
    }

    private void applyBlur() {
        try {
            boolean want = Config.get("Blur") && OpenGlHelper.shadersSupported;
            if (want && !blurOn) {
                if (mc.entityRenderer.isShaderActive()) mc.entityRenderer.stopUseShader();
                mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
                blurOn = true;
            } else if (!want && blurOn) {
                mc.entityRenderer.stopUseShader();
                blurOn = false;
            }
        } catch (Throwable ignored) {
        }
    }

    // ---------- layout ----------

    private int px() {
        return (width - PW) / 2;
    }

    private int py() {
        return (height - PH) / 2;
    }

    private boolean ready() {
        return System.currentTimeMillis() - openTime >= 180;
    }

    private String[] items(int t) {
        switch (t) {
            case 0:
                return new String[]{"Marca", "FPS", "CPS", "Ping", "Coordenadas", "Direcao", "Relogio",
                        "Keystrokes", "Armor HUD", "Efeitos"};
            case 1:
                return new String[]{"Toggle Sprint", "Zoom", "Fullbright"};
            case 2:
                return new String[]{"Hotbar Moderna", "Barras de Status", "Mira Moderna", "Menus Modernos"};
            default:
                return new String[]{"Fundo HUD", "Sombra", "Fonte Moderna", "Blur"};
        }
    }

    private int cols(int t) {
        return t == 0 ? 2 : 1;
    }

    private int rowH(int t) {
        return t == 0 ? 26 : 34;
    }

    private String desc(String n) {
        if (n.equals("Toggle Sprint")) return "Corre sozinho ao andar para frente";
        if (n.equals("Zoom")) return "Segure a tecla C para aproximar";
        if (n.equals("Fullbright")) return "Brilho maximo em lugares escuros";
        if (n.equals("Hotbar Moderna")) return "Hotbar arredondada com seletor animado";
        if (n.equals("Barras de Status")) return "Vida, fome, armadura, ar e XP em barras";
        if (n.equals("Mira Moderna")) return "Mira fina com ponto central colorido";
        if (n.equals("Menus Modernos")) return "Tela inicial e menu de pausa novos";
        if (n.equals("Fundo HUD")) return "Painel escuro atras dos textos";
        if (n.equals("Sombra")) return "Sombra nos textos do HUD";
        if (n.equals("Fonte Moderna")) return "Usa a fonte moderna no lugar da padrao";
        if (n.equals("Blur")) return "Desfoca o jogo quando o menu abre";
        return null;
    }

    private float[] rowBounds(int t, int i) {
        int cols = cols(t);
        int gap = 6;
        int cw = PW - 116 - 12;
        int colW = (cw - gap * (cols - 1)) / cols;
        int col = i % cols;
        int row = i / cols;
        float x = px() + 116 + col * (colW + gap);
        float y = py() + 50 + row * (rowH(t) + gap);
        return new float[]{x, y, x + colW, y + rowH(t)};
    }

    private float[] tabBounds(int i) {
        float y = py() + 70 + i * 28;
        return new float[]{px() + 14, y, px() + 98, y + 24};
    }

    private float[] editBounds() {
        return new float[]{px() + PW - 12 - 120, py() + 14, px() + PW - 12, py() + 36};
    }

    private float swatchX(int i) {
        return px() + 116 + 10 + i * 26;
    }

    private float swatchY() {
        return py() + 50 + 4 * 40 + 32;
    }

    // ---------- desenho ----------

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        float t = Math.min(1F, (System.currentTimeMillis() - openTime) / 180F);
        float e = 1F - (1F - t) * (1F - t) * (1F - t);
        boolean ready = t >= 1F;
        int accent = Config.accent();

        Gui.drawRect(0, 0, width, height, RenderUtil.withAlpha(0x000000, (int) (110 * e)));

        GlStateManager.pushMatrix();
        float sc = 0.92F + 0.08F * e;
        GlStateManager.translate(width / 2F, height / 2F, 0F);
        GlStateManager.scale(sc, sc, 1F);
        GlStateManager.translate(-width / 2F, -height / 2F, 0F);

        int px = px();
        int py = py();

        RenderUtil.roundedRect(px, py, px + PW, py + PH, 10, 0xF0141418);
        RenderUtil.roundedRect(px + 8, py + 8, px + 104, py + PH - 8, 8, 0xFF1B1B22);

        Fonts.drawC("Familia", px + 18, py + 24, accent, 1.4F, true, false);
        Fonts.drawC("Santana", px + 18, py + 38, 0xFFFFFFFF, 1.4F, true, false);
        Fonts.drawC("CLIENT 4.0", px + 18, py + 54, 0xFF7A7A88, 0.8F, false, false);

        for (int i = 0; i < TABS.length; i++) {
            float[] b = tabBounds(i);
            boolean hov = ready && RenderUtil.hover(mx, my, b[0], b[1], b[2], b[3]);
            tabAnim[i] += ((i == tab ? 1F : 0F) - tabAnim[i]) * 0.25F;
            int bg = RenderUtil.lerpColor(hov ? 0x30FFFFFF : 0x00FFFFFF, RenderUtil.withAlpha(accent, 70), tabAnim[i]);
            RenderUtil.roundedRect(b[0], b[1], b[2], b[3], 6, bg);
            if (tabAnim[i] > 0.05F) {
                RenderUtil.roundedRect(b[0] + 2, b[1] + 6, b[0] + 5, b[3] - 6, 1.5F,
                        RenderUtil.withAlpha(accent, (int) (255 * tabAnim[i])));
            }
            int tc = RenderUtil.lerpColor(0xFF9A9AA8, 0xFFFFFFFF, tabAnim[i]);
            Fonts.drawC(TABS[i], b[0] + 14, (b[1] + b[3]) / 2F, tc, 1F, tabAnim[i] > 0.5F, false);
        }
        Fonts.drawC("Right Shift para fechar", px + 16, py + PH - 20, 0xFF6A6A78, 0.75F, false, false);

        String[] list = items(tab);
        int on = 0;
        for (String n : list) if (Config.get(n)) on++;
        Fonts.drawC(TABS[tab], px + 116, py + 22, 0xFFFFFFFF, 1.3F, true, false);
        Fonts.drawC(on + " de " + list.length + " ativos", px + 116, py + 37, 0xFF7A7A88, 0.8F, false, false);

        // botao do editor de posicoes
        if (tab == 0) {
            float[] eb = editBounds();
            boolean hov = ready && RenderUtil.hover(mx, my, eb[0], eb[1], eb[2], eb[3]);
            editAnim = Ui.approach(editAnim, hov);
            Ui.button("Editar posicoes", eb[0], eb[1], eb[2] - eb[0], eb[3] - eb[1], editAnim, true);
        }

        for (int i = 0; i < list.length; i++) {
            String name = list[i];
            float[] b = rowBounds(tab, i);
            boolean hov = ready && RenderUtil.hover(mx, my, b[0], b[1], b[2], b[3]);
            RenderUtil.roundedRect(b[0], b[1], b[2], b[3], 6, hov ? 0xFF272731 : 0xFF1E1E26);

            String d = desc(name);
            float cy = (b[1] + b[3]) / 2F;
            if (d != null) {
                Fonts.drawC(name, b[0] + 10, b[1] + 11, 0xFFFFFFFF, 1F, true, false);
                Fonts.drawC(d, b[0] + 10, b[1] + 24, 0xFF7A7A88, 0.8F, false, false);
            } else {
                Fonts.drawC(name, b[0] + 10, cy, 0xFFE8E8F0, 1F, false, false);
            }

            Float cur = anim.get(name);
            float a = cur == null ? 0F : cur;
            a += ((Config.get(name) ? 1F : 0F) - a) * 0.25F;
            anim.put(name, a);

            float tw = 28F;
            float th = 14F;
            float tx = b[2] - 10 - tw;
            float ty = cy - th / 2F;
            RenderUtil.roundedRect(tx, ty, tx + tw, ty + th, th / 2F, RenderUtil.lerpColor(0xFF3A3A46, accent, a));
            RenderUtil.circle(tx + th / 2F + a * (tw - th), cy, th / 2F - 2F, 0xFFFFFFFF);
        }

        if (tab == 3) {
            Fonts.drawC("Cor de destaque", px + 116, swatchY() - 20, 0xFFE8E8F0, 1F, true, false);
            for (int i = 0; i < Config.ACCENTS.length; i++) {
                float sx = swatchX(i);
                float sy = swatchY();
                if (i == Config.accentIndex()) RenderUtil.circle(sx, sy, 11, 0xFFFFFFFF);
                RenderUtil.circle(sx, sy, i == Config.accentIndex() ? 8.5F : 9F, Config.ACCENTS[i]);
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    // ---------- entrada ----------

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        super.mouseClicked(mx, my, btn);
        if (btn != 0 || !ready()) return;

        for (int i = 0; i < TABS.length; i++) {
            float[] b = tabBounds(i);
            if (RenderUtil.hover(mx, my, b[0], b[1], b[2], b[3])) {
                tab = i;
                return;
            }
        }

        if (tab == 0) {
            float[] eb = editBounds();
            if (RenderUtil.hover(mx, my, eb[0], eb[1], eb[2], eb[3])) {
                mc.displayGuiScreen(new HudEditorGui(this));
                return;
            }
        }

        String[] list = items(tab);
        for (int i = 0; i < list.length; i++) {
            float[] b = rowBounds(tab, i);
            if (RenderUtil.hover(mx, my, b[0], b[1], b[2], b[3])) {
                Config.toggle(list[i]);
                Config.save();
                if (list[i].equals("Blur")) applyBlur();
                return;
            }
        }

        if (tab == 3) {
            for (int i = 0; i < Config.ACCENTS.length; i++) {
                float dx = mx - swatchX(i);
                float dy = my - swatchY();
                if (dx * dx + dy * dy <= 12 * 12) {
                    Config.setAccent(i);
                    Config.save();
                    return;
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_RSHIFT) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
