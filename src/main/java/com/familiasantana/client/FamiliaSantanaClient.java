package com.familiasantana.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "familiasantana", name = "Familia Santana Client", version = "4.0",
        clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class FamiliaSantanaClient {

    private static final String CATEGORY = "Familia Santana Client";

    private KeyBinding menuKey;
    private KeyBinding zoomKey;

    private final Minecraft mc = Minecraft.getMinecraft();

    private float oldGamma = 1.0F;
    private boolean fullbrightApplied = false;
    private boolean zooming = false;
    private boolean oldSmooth = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Config.load();
        Hud.init();
        menuKey = new KeyBinding("Abrir menu", Keyboard.KEY_RSHIFT, CATEGORY);
        zoomKey = new KeyBinding("Zoom", Keyboard.KEY_C, CATEGORY);
        ClientRegistry.registerKeyBinding(menuKey);
        ClientRegistry.registerKeyBinding(zoomKey);
        MinecraftForge.EVENT_BUS.register(this);
    }

    // ---------- Entrada ----------

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        if (e.buttonstate) Hud.click(e.button);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        if (menuKey.isPressed()) {
            mc.displayGuiScreen(new ModernGui());
        }
    }

    // ---------- Menus ----------

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        if (!Config.get("Menus Modernos")) return;
        if (e.gui instanceof GuiMainMenu) {
            e.gui = new ModernMainMenu();
        } else if (e.gui instanceof GuiIngameMenu && mc.theWorld != null) {
            e.gui = new ModernPauseMenu();
        }
    }

    // ---------- Tick ----------

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || mc.thePlayer == null) return;

        if (Config.get("Toggle Sprint")
                && mc.gameSettings.keyBindForward.isKeyDown()
                && !mc.thePlayer.isSneaking()
                && !mc.thePlayer.isUsingItem()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }

        if (Config.get("Fullbright")) {
            if (!fullbrightApplied) {
                oldGamma = mc.gameSettings.gammaSetting;
                fullbrightApplied = true;
            }
            mc.gameSettings.gammaSetting = 100.0F;
        } else if (fullbrightApplied) {
            mc.gameSettings.gammaSetting = oldGamma;
            fullbrightApplied = false;
        }

        boolean wantZoom = Config.get("Zoom") && zoomKey.isKeyDown() && mc.currentScreen == null;
        if (wantZoom && !zooming) {
            oldSmooth = mc.gameSettings.smoothCamera;
            mc.gameSettings.smoothCamera = true;
            zooming = true;
        } else if (!wantZoom && zooming) {
            mc.gameSettings.smoothCamera = oldSmooth;
            zooming = false;
        }
    }

    @SubscribeEvent
    public void onFov(FOVUpdateEvent e) {
        if (zooming) {
            e.newfov = e.fov * 0.25F;
        }
    }

    // ---------- Interface do jogo (substitui a padrao) ----------

    @SubscribeEvent
    public void onPre(RenderGameOverlayEvent.Pre e) {
        if (mc.thePlayer == null) return;
        ScaledResolution sr = e.resolution;

        switch (e.type) {
            case HOTBAR:
                if (Config.get("Hotbar Moderna") && !mc.playerController.isSpectator()) {
                    e.setCanceled(true);
                    GameUi.hotbar(mc, sr);
                }
                break;
            case HEALTH:
                if (Config.get("Barras de Status")) {
                    e.setCanceled(true);
                    GameUi.status(mc, sr);
                }
                break;
            case ARMOR:
            case FOOD:
            case AIR:
            case EXPERIENCE:
                if (Config.get("Barras de Status")) e.setCanceled(true);
                break;
            case CROSSHAIRS:
                if (Config.get("Mira Moderna") && !mc.gameSettings.showDebugInfo) {
                    e.setCanceled(true);
                    if (mc.gameSettings.thirdPersonView == 0) GameUi.crosshair(sr);
                }
                break;
            default:
                break;
        }
    }

    // ---------- HUD ----------

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post e) {
        if (e.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.thePlayer == null || mc.gameSettings.showDebugInfo) return;

        Hud.render(mc, e.resolution);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }
}
