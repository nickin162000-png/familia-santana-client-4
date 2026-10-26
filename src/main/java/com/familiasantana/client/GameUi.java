package com.familiasantana.client;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** Substitui a interface padrao do jogo (hotbar, vida, fome, xp, mira). */
public class GameUi {
    private static final int BG = 0x8C0E0E12;
    private static float selX = -1F;
    private static float animHp = -1F;
    private static float animFood = -1F;

    private static float clamp(float v) {
        return Math.max(0F, Math.min(1F, v));
    }

    // ---------- hotbar ----------

    public static void hotbar(Minecraft mc, ScaledResolution sr) {
        int sw = sr.getScaledWidth();
        int sh = sr.getScaledHeight();
        int slot = 20;
        int gap = 2;
        float total = 9 * slot + 8 * gap;
        float x0 = sw / 2F - total / 2F;
        float top = sh - 27;

        RenderUtil.roundedRect(x0 - 5, sh - 30, x0 + total + 5, sh - 4, 7, BG);

        int cur = mc.thePlayer.inventory.currentItem;
        float target = x0 + cur * (slot + gap);
        if (selX < 0F) selX = target;
        selX += (target - selX) * 0.35F;

        for (int i = 0; i < 9; i++) {
            float sx = x0 + i * (slot + gap);
            RenderUtil.roundedRect(sx, top, sx + slot, top + slot, 4, 0x22FFFFFF);
        }
        RenderUtil.roundedRect(selX, top, selX + slot, top + slot, 4,
                RenderUtil.withAlpha(Config.accent(), 0xD0));

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null) continue;
            int ix = (int) (x0 + i * (slot + gap)) + 2;
            int iy = (int) top + 2;
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, ix, iy);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, ix, iy);
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    // ---------- barras de status ----------

    public static void status(Minecraft mc, ScaledResolution sr) {
        EntityPlayer p = mc.thePlayer;
        int sw = sr.getScaledWidth();
        int sh = sr.getScaledHeight();
        float x0 = sw / 2F - 103;
        float full = 206;
        float barW = 96;
        float rightX = x0 + full - barW;

        // XP
        float xpY = sh - 35;
        RenderUtil.roundedRect(x0, xpY, x0 + full, xpY + 3, 1.5F, BG);
        float xp = clamp(p.experience);
        if (xp > 0.01F) RenderUtil.roundedRect(x0, xpY, x0 + Math.max(3F, full * xp), xpY + 3, 1.5F, 0xFF2ED573);

        // vida
        float hy = sh - 48;
        float hh = 10;
        float hp = p.getHealth();
        float max = Math.max(1F, p.getMaxHealth());
        float hpFrac = clamp(hp / max);
        if (animHp < 0F) animHp = hpFrac;
        animHp += (hpFrac - animHp) * 0.15F;
        String hpText = String.valueOf((int) Math.ceil(hp));
        float abs = p.getAbsorptionAmount();
        if (abs > 0F) hpText += " +" + (int) Math.ceil(abs);
        bar(x0, hy, barW, hh, animHp, 0xFFFF4757, hpText);

        // fome
        float foodFrac = clamp(p.getFoodStats().getFoodLevel() / 20F);
        if (animFood < 0F) animFood = foodFrac;
        animFood += (foodFrac - animFood) * 0.15F;
        bar(rightX, hy, barW, hh, animFood, 0xFFFFA502, String.valueOf(p.getFoodStats().getFoodLevel()));

        // nivel de xp (entre as barras)
        if (p.experienceLevel > 0) {
            String lv = String.valueOf(p.experienceLevel);
            float lw = Fonts.width(lv, 0.85F, true);
            Fonts.drawC(lv, sw / 2F - lw / 2F, hy + hh / 2F, 0xFF9BFFB8, 0.85F, true, true);
        }

        // armadura (linha fina acima da vida)
        int armor = p.getTotalArmorValue();
        if (armor > 0) {
            RenderUtil.roundedRect(x0, hy - 5, x0 + barW, hy - 2, 1.5F, BG);
            RenderUtil.roundedRect(x0, hy - 5, x0 + Math.max(3F, barW * clamp(armor / 20F)), hy - 2, 1.5F, 0xFF9FB4CC);
        }

        // ar (debaixo d'agua)
        if (p.isInsideOfMaterial(Material.water)) {
            float air = clamp(p.getAir() / 300F);
            RenderUtil.roundedRect(rightX, hy - 6, rightX + barW, hy - 2, 2F, BG);
            RenderUtil.roundedRect(rightX, hy - 6, rightX + Math.max(4F, barW * air), hy - 2, 2F, 0xFF3EA6FF);
        }

        GlStateManager.enableBlend();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private static void bar(float x, float y, float w, float h, float frac, int color, String text) {
        RenderUtil.roundedRect(x, y, x + w, y + h, h / 2F, BG);
        if (frac > 0.01F) RenderUtil.roundedRect(x, y, x + Math.max(h, w * clamp(frac)), y + h, h / 2F, color);
        float tw = Fonts.width(text, 0.8F, true);
        Fonts.drawC(text, x + (w - tw) / 2F, y + h / 2F, 0xFFFFFFFF, 0.8F, true, true);
    }

    // ---------- mira ----------

    public static void crosshair(ScaledResolution sr) {
        float cx = sr.getScaledWidth() / 2F;
        float cy = sr.getScaledHeight() / 2F;
        float gap = 3F;
        float len = 5F;
        int shadow = 0x66000000;
        int white = 0xE6FFFFFF;

        tick(cx - gap - len, cy - 1, cx - gap, cy + 1, shadow);
        tick(cx + gap, cy - 1, cx + gap + len, cy + 1, shadow);
        tick(cx - 1, cy - gap - len, cx + 1, cy - gap, shadow);
        tick(cx - 1, cy + gap, cx + 1, cy + gap + len, shadow);

        tick(cx - gap - len, cy - 0.5F, cx - gap, cy + 0.5F, white);
        tick(cx + gap, cy - 0.5F, cx + gap + len, cy + 0.5F, white);
        tick(cx - 0.5F, cy - gap - len, cx + 0.5F, cy - gap, white);
        tick(cx - 0.5F, cy + gap, cx + 0.5F, cy + gap + len, white);

        RenderUtil.circle(cx, cy, 1.1F, Config.accent());
    }

    private static void tick(float x, float y, float x2, float y2, int color) {
        RenderUtil.rect(x, y, x2, y2, color);
    }
}
