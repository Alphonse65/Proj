package fr.rise.ui;

import com.mojang.blaze3d.platform.InputConstants;
import fr.rise.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Menu pastel : onglets de categories, modules a gauche, reglages a droite. */
public class ClickGui extends Screen {
    private static final int W = 480, H = 340, SET_H = 28, ROW_H = 17;
    private static final int BG = 0xFFFFF4FA, HEAD = 0xFFFFA8D3, ACCENT = 0xFFFF7EBB, TEXT = 0xFF7A4B6C,
            SUB = 0xFFB58AA9, SEL = 0xFFE6D6FF, ROW = 0xFFFFE8F3, ON = 0xFF7FE0B5, OFF = 0xFFE3D3DE, WHITE = 0xFFFFFFFF;

    private static Category lastCat = Category.COMBAT;
    private static Module lastSel;

    private Category cat = lastCat;
    private Module selected;
    private int px, py, scroll, listScroll;
    private Setting.Num dragging;
    private Setting.Text focused;
    private int sx0, sx1;

    public ClickGui() {
        super(Component.literal("Rise"));
        List<Module> ms = Modules.inCategory(cat);
        selected = (lastSel != null && lastSel.category == cat) ? lastSel : ms.get(0);
    }

    @Override protected void init() { px = (width - W) / 2; py = (height - H) / 2; sx0 = px + 166; sx1 = px + W - 14; }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { lastCat = cat; lastSel = selected; super.onClose(); }

    // ---------- dessin ----------
    private static void round(GuiGraphicsExtractor c, int x, int y, int w, int h, int r, int col) {
        r = Math.min(r, Math.min(w, h) / 2);
        for (int i = 0; i < r; i++) {
            double d = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * r - d * d));
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, col);
            c.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, col);
        }
        c.fill(x, y + r, x + w, y + h - r, col);
    }

    private static void toggleSwitch(GuiGraphicsExtractor c, int x, int y, boolean on) {
        round(c, x, y, 24, 10, 5, on ? ON : OFF);
        round(c, x + (on ? 15 : 1), y + 1, 8, 8, 4, WHITE);
    }

    private int tabW() { return (W - 16) / Category.values().length; }
    private int listTop() { return py + 58; }
    private int listBottom() { return py + H - 10; }
    private int settingsTop() { return py + 96; }
    private int settingsBottom() { return py + H - 10; }
    private int maxScroll() { return Math.max(0, selected.settings.size() * SET_H - (settingsBottom() - settingsTop())); }
    private int maxListScroll() { return Math.max(0, Modules.inCategory(cat).size() * ROW_H - (listBottom() - listTop())); }

    private String trim(String s, int maxW) {
        while (font.width(s) > maxW && s.length() > 4) s = s.substring(0, s.length() - 4) + "...";
        return s;
    }

    // ---------- rendu ----------
    @Override
    public void extractRenderState(GuiGraphicsExtractor c, int mx, int my, float delta) {
        super.extractRenderState(c, mx, my, delta);
        c.fill(0, 0, width, height, 0x44301830);
        round(c, px + 3, py + 4, W, H, 9, 0x33000000);
        round(c, px, py, W, H, 9, BG);
        round(c, px, py, W, 28, 9, HEAD);
        c.fill(px, py + 14, px + W, py + 28, HEAD);
        c.text(font, "\u2665 Rise \u2665", px + 12, py + 10, WHITE, true);
        String hint = "Shift droit : fermer";
        c.text(font, hint, px + W - font.width(hint) - 12, py + 10, 0xFFFFF0F8, true);

        // onglets
        int tx = px + 8;
        for (Category k : Category.values()) {
            boolean sel = k == cat;
            round(c, tx + 1, py + 33, tabW() - 2, 16, 6, sel ? SEL : ROW);
            c.text(font, k.label, tx + (tabW() - font.width(k.label)) / 2, py + 37, sel ? ACCENT : TEXT, false);
            tx += tabW();
        }

        // modules
        List<Module> ms = Modules.inCategory(cat);
        c.enableScissor(px + 4, listTop(), px + 156, listBottom());
        int y = listTop() - listScroll;
        for (Module m : ms) {
            boolean hover = mx >= px + 8 && mx <= px + 152 && my >= y && my < y + 15 && my >= listTop() && my <= listBottom();
            round(c, px + 8, y, 144, 15, 5, m == selected ? SEL : (hover ? 0xFFFFDCEE : ROW));
            c.text(font, trim(m.name, 100), px + 14, y + 4, TEXT, false);
            toggleSwitch(c, px + 124, y + 2, m.isEnabled());
            y += ROW_H;
        }
        c.disableScissor();

        c.fill(px + 158, py + 56, px + 159, py + H - 10, 0xFFF3D3E5);

        c.text(font, selected.name, px + 166, py + 60, ACCENT, true);
        c.text(font, trim(selected.description, W - 190), px + 166, py + 74, SUB, false);
        c.fill(px + 166, py + 88, px + W - 14, py + 89, 0xFFF3D3E5);

        c.enableScissor(px + 162, settingsTop(), px + W - 6, settingsBottom());
        int sy = settingsTop() - scroll;
        for (Setting<?> s : selected.settings) { drawSetting(c, s, sy); sy += SET_H; }
        c.disableScissor();
        if (selected.settings.isEmpty()) c.text(font, "Aucun reglage.", px + 166, settingsTop() + 4, SUB, false);
    }

    private void drawSetting(GuiGraphicsExtractor c, Setting<?> s, int y) {
        int x0 = sx0, x1 = sx1;
        if (s instanceof Setting.Bool b) {
            c.text(font, b.name, x0, y + 6, TEXT, false);
            toggleSwitch(c, x1 - 24, y + 4, b.on());
        } else if (s instanceof Setting.Num n) {
            c.text(font, n.name, x0, y + 1, TEXT, false);
            String v = n.integer ? String.valueOf(n.i()) : String.valueOf(n.d());
            c.text(font, v, x1 - font.width(v), y + 1, ACCENT, false);
            round(c, x0, y + 14, x1 - x0, 6, 3, OFF);
            int fw = Math.max(6, (int) ((x1 - x0) * n.ratio()));
            round(c, x0, y + 14, fw, 6, 3, ACCENT);
            round(c, x0 + fw - 4, y + 12, 9, 10, 4, WHITE);
        } else if (s instanceof Setting.Choice ch) {
            c.text(font, ch.name, x0, y + 6, TEXT, false);
            String v = "< " + ch.get() + " >";
            int w = font.width(v) + 10;
            round(c, x1 - w, y + 1, w, 16, 6, SEL);
            c.text(font, v, x1 - w + 5, y + 5, TEXT, false);
        } else if (s instanceof Setting.Text t) {
            c.text(font, t.name, x0, y + 1, TEXT, false);
            round(c, x0, y + 12, x1 - x0, 14, 5, t == focused ? WHITE : ROW);
            String shown = trim(t.s(), x1 - x0 - 12) + (t == focused && (System.currentTimeMillis() / 500) % 2 == 0 ? "|" : "");
            c.text(font, shown, x0 + 5, y + 15, TEXT, false);
        }
    }

    // ---------- interactions ----------
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        if (event.button() != 0) return super.mouseClicked(event, doubleClick);
        focused = null;

        if (my >= py + 33 && my <= py + 49 && mx >= px + 8) {
            int idx = (int) ((mx - (px + 8)) / tabW());
            Category[] cats = Category.values();
            if (idx >= 0 && idx < cats.length) {
                cat = cats[idx]; listScroll = 0; scroll = 0;
                selected = Modules.inCategory(cat).get(0);
                return true;
            }
        }

        if (my >= listTop() && my <= listBottom() && mx >= px + 8 && mx <= px + 152) {
            int y = listTop() - listScroll;
            for (Module m : Modules.inCategory(cat)) {
                if (my >= y && my < y + 15) {
                    if (mx >= px + 124) m.toggle();
                    else { selected = m; scroll = 0; }
                    return true;
                }
                y += ROW_H;
            }
        }

        if (my >= settingsTop() && my <= settingsBottom() && mx >= px + 162) {
            int y = settingsTop() - scroll;
            for (Setting<?> s : selected.settings) {
                if (my >= y && my < y + SET_H) {
                    if (s instanceof Setting.Bool b) b.value = !b.value;
                    else if (s instanceof Setting.Choice ch) ch.next();
                    else if (s instanceof Setting.Text t) focused = t;
                    else if (s instanceof Setting.Num n) { dragging = n; n.setRatio((mx - sx0) / (double) (sx1 - sx0)); }
                    return true;
                }
                y += SET_H;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging != null) { dragging.setRatio((event.x() - sx0) / (double) (sx1 - sx0)); return true; }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (mx < px + 158) listScroll = (int) Math.max(0, Math.min(maxListScroll(), listScroll - scrollY * 14));
        else scroll = (int) Math.max(0, Math.min(maxScroll(), scroll - scrollY * 14));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (focused != null) {
            if (key == InputConstants.KEY_BACKSPACE && !focused.value.isEmpty())
                focused.value = focused.value.substring(0, focused.value.length() - 1);
            else if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_ESCAPE) focused = null;
            return true;
        }
        if (key == InputConstants.KEY_RSHIFT) { onClose(); return true; }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        int cp = event.codepoint();
        if (focused != null && cp >= 32 && focused.value.length() < 80) {
            focused.value += new String(Character.toChars(cp));
            return true;
        }
        return super.charTyped(event);
    }
}
