package fr.rise.mods;

import fr.rise.*;

public class Sprint extends Module {
    public Sprint() { super("Sprint auto", "Court automatiquement quand vous avancez.", Category.MOUVEMENT); }

    @Override public void onTick() {
        if (mc.options.keyUp.isDown() && !mc.player.horizontalCollision && !mc.player.isShiftKeyDown()
                && mc.player.getFoodData().getFoodLevel() > 6)
            mc.player.setSprinting(true);
    }
}
