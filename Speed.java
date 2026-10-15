package fr.rise.mods;

import fr.rise.*;

public class Speed extends Module {
    private final Setting.Num speed = add(new Setting.Num("Vitesse", 0.4, 0.1, 1.5, false));
    private final Setting.Bool bhop = add(new Setting.Bool("Bunny hop", true));

    public Speed() { super("Speed", "Vitesse de deplacement augmentee.", Category.MOUVEMENT); }

    @Override public void onTick() {
        if (!Util.moving()) return;
        if (bhop.on() && Compat.onGround()) mc.player.jumpFromGround();
        double[] d = Util.moveDir();
        mc.player.setDeltaMovement(d[0] * speed.d(), mc.player.getDeltaMovement().y, d[1] * speed.d());
    }
}
