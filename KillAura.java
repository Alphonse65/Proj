package fr.rise.mods;

import fr.rise.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class KillAura extends Module {
    private final Setting.Num range = add(new Setting.Num("Portee", 4.2, 1, 6, false));
    private final Setting.Bool players = add(new Setting.Bool("Joueurs", true));
    private final Setting.Bool hostiles = add(new Setting.Bool("Monstres", true));
    private final Setting.Bool animals = add(new Setting.Bool("Animaux", false));
    private final Setting.Bool rotate = add(new Setting.Bool("Rotation", true));
    private final Setting.Bool waitCd = add(new Setting.Bool("Attendre le cooldown", true));
    private final Setting.Num cps = add(new Setting.Num("CPS (sans cooldown)", 12, 1, 20, true));
    private int timer;

    public KillAura() { super("KillAura", "Attaque automatiquement l'entite la plus proche.", Category.COMBAT); }

    @Override public void onTick() {
        if (Compat.screen() != null) return;
        timer++;
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        Vec3 eye = mc.player.getEyePosition();
        for (Entity e : Compat.entities()) {
            if (!(e instanceof LivingEntity le) || e == mc.player || !le.isAlive()) continue;
            MobCategory cat = e.getType().getCategory();
            boolean ok = (players.on() && e instanceof Player p && !p.isSpectator())
                    || (hostiles.on() && cat == MobCategory.MONSTER) || (animals.on() && cat == MobCategory.CREATURE);
            if (!ok) continue;
            double d = Util.distToBox(eye, e.getBoundingBox());
            if (d <= range.d() && d < best) { best = d; target = le; }
        }
        if (target == null) return;

        if (rotate.on()) Util.lookAt(eye, target.getBoundingBox().getCenter());
        if (waitCd.on()) { if (mc.player.getAttackStrengthScale(0.5f) < 1f) return; }
        else if (timer < 20.0 / cps.i()) return;
        timer = 0;
        Compat.attack(target);
        Compat.swing();
    }
}
