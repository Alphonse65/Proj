package fr.rise.mods;

import fr.rise.*;

public class Flight extends Module {
    private final Setting.Num speed = add(new Setting.Num("Vitesse", 1.0, 0.1, 5, false));
    private final Setting.Num vspeed = add(new Setting.Num("Vitesse verticale", 0.6, 0.1, 3, false));
    private final Setting.Bool antiKick = add(new Setting.Bool("Anti-kick (petite chute)", true));
    private int tick;

    public Flight() { super("Flight", "Vol libre (Espace = monter, Shift = descendre).", Category.MOUVEMENT); }

    @Override public void onTick() {
        double[] d = Util.moveDir();
        double vy = 0;
        if (mc.options.keyJump.isDown()) vy = vspeed.d();
        else if (mc.options.keyShift.isDown()) vy = -vspeed.d();
        else if (antiKick.on() && tick++ % 40 == 0) vy = -0.06;
        mc.player.setDeltaMovement(d[0] * speed.d(), vy, d[1] * speed.d());
        mc.player.fallDistance = 0;
    }
}
