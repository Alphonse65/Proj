package fr.rise;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final Minecraft mc = Minecraft.getInstance();

    public final String name, description;
    public final Category category;
    public final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    protected <S extends Setting<?>> S add(S s) { settings.add(s); return s; }

    public boolean isEnabled() { return enabled; }
    public void toggle() { setEnabled(!enabled); }
    public void setEnabled(boolean e) {
        if (e == enabled) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
    }

    protected void info(String msg) { Compat.msg("\u00a7d[Rise] \u00a7f" + msg); }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    /** Phase d'extraction : ajoute des boites 3D a dessiner a travers les murs. */
    public void extract(Boxes out, float partial) {}
}
