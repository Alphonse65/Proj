package fr.rise.mods;

import fr.rise.*;

public class AutoWalk extends Module {
    public AutoWalk() { super("Auto Walk", "Avance automatiquement tout droit.", Category.MOUVEMENT); }
    @Override public void onTick() { mc.options.keyUp.setDown(true); }
    @Override public void onDisable() { mc.options.keyUp.setDown(false); }
}
