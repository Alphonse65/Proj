package fr.rise.ui;

import fr.rise.Module;
import fr.rise.Modules;
import fr.rise.mods.HudInfos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class Hud {
    private static final int[] COLORS = {0xFFFF8CC6, 0xFFC9A8FF, 0xFF8FE3C0, 0xFFFFC38A, 0xFF9AD0FF};

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        var font = mc.font;
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();

        g.text(font, "\u2665 Rise", 5, 5, 0xFFFF8CC6, true);

        List<Module> on = new ArrayList<>();
        for (Module m : Modules.all()) if (m.isEnabled()) on.add(m);
        on.sort(Comparator.comparingInt((Module m) -> -font.width(m.name)));
        int y = 4, i = 0;
        for (Module m : on) {
            int w = font.width(m.name), x = sw - w - 8, col = COLORS[i % COLORS.length];
            g.fill(x - 3, y - 1, sw, y + 10, 0x66000000);
            g.fill(sw - 2, y - 1, sw, y + 10, col);
            g.text(font, m.name, x, y + 1, col, true);
            y += 11; i++;
        }

        HudInfos h = Modules.get(HudInfos.class);
        if (h != null && h.isEnabled()) {
            List<String> lines = new ArrayList<>();
            if (h.coords.on()) {
                int x = mc.player.getBlockX(), yy = mc.player.getBlockY(), z = mc.player.getBlockZ();
                lines.add("XYZ " + x + " " + yy + " " + z);
                boolean nether = mc.level.dimension() == Level.NETHER;
                lines.add(nether ? "Overworld " + x * 8 + " " + z * 8 : "Nether " + x / 8 + " " + z / 8);
            }
            if (h.fps.on()) lines.add("FPS " + mc.getFps());
            if (h.ping.on() && mc.getConnection() != null) {
                var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
                if (info != null) lines.add("Ping " + info.getLatency() + " ms");
            }
            if (h.speed.on()) lines.add(String.format(Locale.US, "%.1f blocs/s", h.bps));
            int ly = sh - 12 * lines.size() - 4;
            for (String s : lines) { g.text(font, s, 5, ly, 0xFFFFD6EC, true); ly += 12; }
        }
    }
}
