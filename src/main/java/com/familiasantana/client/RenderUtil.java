package com.familiasantana.client;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderUtil {

    private static void begin2D() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    }

    private static void end2D() {
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    public static void setColor(int argb) {
        float a = (argb >>> 24 & 255) / 255F;
        float r = (argb >> 16 & 255) / 255F;
        float g = (argb >> 8 & 255) / 255F;
        float b = (argb & 255) / 255F;
        GlStateManager.color(r, g, b, a);
    }

    public static void rect(float x, float y, float x2, float y2, int color) {
        begin2D();
        setColor(color);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y2);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x2, y);
        GL11.glEnd();
        end2D();
    }

    public static void roundedRect(float x, float y, float x2, float y2, float r, int color) {
        r = Math.min(r, Math.min((x2 - x) / 2F, (y2 - y) / 2F));
        if (r <= 0.5F) {
            rect(x, y, x2, y2, color);
            return;
        }
        begin2D();
        setColor(color);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f((x + x2) / 2F, (y + y2) / 2F);
        int seg = 8;
        float[][] corners = {
                {x + r, y + r, 180F},
                {x2 - r, y + r, 270F},
                {x2 - r, y2 - r, 0F},
                {x + r, y2 - r, 90F}
        };
        for (float[] c : corners) {
            for (int i = 0; i <= seg; i++) {
                double ang = Math.toRadians(c[2] + 90.0 * i / seg);
                GL11.glVertex2d(c[0] + Math.cos(ang) * r, c[1] + Math.sin(ang) * r);
            }
        }
        GL11.glVertex2d(x + r + Math.cos(Math.toRadians(180)) * r, y + r + Math.sin(Math.toRadians(180)) * r);
        GL11.glEnd();
        end2D();
    }

    public static void circle(float cx, float cy, float r, int color) {
        begin2D();
        setColor(color);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= 28; i++) {
            double ang = Math.PI * 2 * i / 28;
            GL11.glVertex2d(cx + Math.cos(ang) * r, cy + Math.sin(ang) * r);
        }
        GL11.glEnd();
        end2D();
    }

    public static int lerpColor(int c1, int c2, float t) {
        t = Math.max(0F, Math.min(1F, t));
        int a = (int) (((c1 >>> 24) & 255) * (1 - t) + ((c2 >>> 24) & 255) * t);
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int withAlpha(int color, int alpha) {
        return ((alpha & 255) << 24) | (color & 0xFFFFFF);
    }

    public static boolean hover(int mx, int my, float x, float y, float x2, float y2) {
        return mx >= x && mx <= x2 && my >= y && my <= y2;
    }
}
