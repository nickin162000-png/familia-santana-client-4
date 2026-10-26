package com.familiasantana.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;

import java.io.IOException;

public class ModernPauseMenu extends GuiScreen {
    private final float[] hover = new float[5];

    private String label(int i) {
        switch (i) {
            case 0:
                return "Voltar ao jogo";
            case 1:
                return "Menu do Client";
            case 2:
                return "Editar HUD";
            case 3:
                return "Opcoes";
            default:
                return mc.isIntegratedServerRunning() ? "Salvar e sair" : "Desconectar";
        }
    }

    private float bx() {
        return width / 2F - 100;
    }

    private float by(int i) {
        return height / 2F - 66 + i * 34;
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        Gui.drawRect(0, 0, width, height, 0x99000000);

        String t = "Pausado";
        float tw = Fonts.width(t, 2F, true);
        Fonts.drawC(t, width / 2F - tw / 2F, by(0) - 28, 0xFFFFFFFF, 2F, true, false);
        RenderUtil.roundedRect(width / 2F - 14, by(0) - 14, width / 2F + 14, by(0) - 12, 1F, Config.accent());

        for (int i = 0; i < 5; i++) {
            boolean hov = RenderUtil.hover(mx, my, bx(), by(i), bx() + 200, by(i) + 26);
            hover[i] = Ui.approach(hover[i], hov);
            Ui.button(label(i), bx(), by(i), 200, 26, hover[i], i == 0);
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        super.mouseClicked(mx, my, btn);
        if (btn != 0) return;
        for (int i = 0; i < 5; i++) {
            if (!RenderUtil.hover(mx, my, bx(), by(i), bx() + 200, by(i) + 26)) continue;
            if (i == 0) {
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
            } else if (i == 1) {
                mc.displayGuiScreen(new ModernGui());
            } else if (i == 2) {
                mc.displayGuiScreen(new HudEditorGui(this));
            } else if (i == 3) {
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
            } else {
                boolean singleplayer = mc.isIntegratedServerRunning();
                mc.theWorld.sendQuittingDisconnectingPacket();
                mc.loadWorld((WorldClient) null);
                if (singleplayer) mc.displayGuiScreen(new GuiMainMenu());
                else mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
            }
            return;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
