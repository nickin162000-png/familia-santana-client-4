package com.familiasantana.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

/** Tela para arrastar os elementos do HUD para onde quiser. */
public class HudEditorGui extends GuiScreen {
    private final GuiScreen parent;
    private Hud.El drag;
    private Hud.El hover;
    private float offX, offY;
    private boolean guideV, guideH;
    private final float[] btnAnim = new float[2];

    public HudEditorGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        Hud.editing = true;
    }

    @Override
    public void onGuiClosed() {
        Hud.editing = false;
        Hud.savePositions();
    }

    private Hud.El hit(int mx, int my) {
        List<Hud.El> list = Hud.ELEMENTS;
        for (int i = list.size() - 1; i >= 0; i--) {
            Hud.El e = list.get(i);
            if (!Config.get(e.name) || e.w <= 0) continue;
            if (RenderUtil.hover(mx, my, e.x - 3, e.y - 3, e.x + e.w + 3, e.y + e.h + 3)) return e;
        }
        return null;
    }

    private float[] resetBounds() {
        return new float[]{width / 2F - 115, height - 40, width / 2F - 5, height - 18};
    }

    private float[] doneBounds() {
        return new float[]{width / 2F + 5, height - 40, width / 2F + 115, height - 18};
    }

    private void outline(float x, float y, float x2, float y2, int c) {
        RenderUtil.rect(x, y, x2, y + 1, c);
        RenderUtil.rect(x, y2 - 1, x2, y2, c);
        RenderUtil.rect(x, y, x + 1, y2, c);
        RenderUtil.rect(x2 - 1, y, x2, y2, c);
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        Gui.drawRect(0, 0, width, height, 0x55000000);
        int accent = Config.accent();
        if (drag == null) hover = hit(mx, my);

        for (Hud.El e : Hud.ELEMENTS) {
            if (!Config.get(e.name) || e.w <= 0) continue;
            boolean sel = e == drag;
            boolean hov = e == hover;
            float x1 = e.x - 3, y1 = e.y - 3, x2 = e.x + e.w + 3, y2 = e.y + e.h + 3;
            RenderUtil.roundedRect(x1, y1, x2, y2, 5, sel ? RenderUtil.withAlpha(accent, 0x40) : (hov ? 0x30FFFFFF : 0x18FFFFFF));
            outline(x1, y1, x2, y2, sel ? accent : (hov ? 0xFFFFFFFF : 0x80FFFFFF));
            float ly = e.y < 14 ? y2 + 6 : y1 - 6;
            Fonts.drawC(e.name, x1, ly, sel ? accent : 0xFFE0E0EA, 0.75F, true, true);
        }

        if (drag != null) {
            if (guideV) RenderUtil.rect(width / 2F - 0.5F, 0, width / 2F + 0.5F, height, RenderUtil.withAlpha(accent, 0x90));
            if (guideH) RenderUtil.rect(0, height / 2F - 0.5F, width, height / 2F + 0.5F, RenderUtil.withAlpha(accent, 0x90));
        }

        String hint = "Arraste os elementos para mover  |  ESC para concluir";
        float hw = Fonts.width(hint, 1F, false) + 20;
        RenderUtil.roundedRect(width / 2F - hw / 2F, 10, width / 2F + hw / 2F, 28, 6, 0xCC141418);
        Fonts.drawC(hint, width / 2F - hw / 2F + 10, 19, 0xFFFFFFFF, 1F, false, false);

        float[] rb = resetBounds();
        float[] db = doneBounds();
        btnAnim[0] = Ui.approach(btnAnim[0], RenderUtil.hover(mx, my, rb[0], rb[1], rb[2], rb[3]));
        btnAnim[1] = Ui.approach(btnAnim[1], RenderUtil.hover(mx, my, db[0], db[1], db[2], db[3]));
        Ui.button("Resetar tudo", rb[0], rb[1], rb[2] - rb[0], rb[3] - rb[1], btnAnim[0], false);
        Ui.button("Concluir", db[0], db[1], db[2] - db[0], db[3] - db[1], btnAnim[1], true);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        super.mouseClicked(mx, my, btn);
        if (btn != 0) return;

        float[] rb = resetBounds();
        float[] db = doneBounds();
        if (RenderUtil.hover(mx, my, rb[0], rb[1], rb[2], rb[3])) {
            Hud.resetAll();
            Hud.savePositions();
            return;
        }
        if (RenderUtil.hover(mx, my, db[0], db[1], db[2], db[3])) {
            mc.displayGuiScreen(parent);
            return;
        }

        Hud.El e = hit(mx, my);
        if (e != null) {
            drag = e;
            offX = mx - e.x;
            offY = my - e.y;
        }
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        if (drag == null) return;
        float w = drag.w;
        float h = drag.h;
        float nx = mx - offX;
        float ny = my - offY;
        guideV = false;
        guideH = false;

        if (Math.abs(nx) < 5) nx = 0;
        if (Math.abs(nx + w - width) < 5) nx = width - w;
        if (Math.abs(nx + w / 2F - width / 2F) < 5) {
            nx = width / 2F - w / 2F;
            guideV = true;
        }
        if (Math.abs(ny) < 5) ny = 0;
        if (Math.abs(ny + h - height) < 5) ny = height - h;
        if (Math.abs(ny + h / 2F - height / 2F) < 5) {
            ny = height / 2F - h / 2F;
            guideH = true;
        }

        nx = Math.max(0, Math.min(width - w, nx));
        ny = Math.max(0, Math.min(height - h, ny));
        drag.fx = nx / width;
        drag.fy = ny / height;
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        super.mouseReleased(mx, my, state);
        if (drag != null) {
            drag = null;
            guideV = false;
            guideH = false;
            Hud.savePositions();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
