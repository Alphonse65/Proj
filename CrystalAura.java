package fr.rise.mods;

import fr.rise.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CrystalAura extends Module {
    private final Setting.Num placeRange = add(new Setting.Num("Portee pose", 4.5, 1, 6, false));
    private final Setting.Num breakRange = add(new Setting.Num("Portee casse", 4.5, 1, 6, false));
    private final Setting.Num targetRange = add(new Setting.Num("Portee cible", 10, 3, 16, false));
    private final Setting.Num minDmg = add(new Setting.Num("Degats min cible", 5, 1, 20, false));
    private final Setting.Num maxSelf = add(new Setting.Num("Degats max sur moi", 5, 0, 20, false));
    private final Setting.Num delay = add(new Setting.Num("Delai (ticks)", 2, 0, 10, true));
    private final Setting.Bool place = add(new Setting.Bool("Poser", true));
    private final Setting.Bool breakC = add(new Setting.Bool("Casser", true));
    private final Setting.Bool autoSwitch = add(new Setting.Bool("Changer de slot", true));
    private final Setting.Bool rotate = add(new Setting.Bool("Rotation", true));
    private int timer;

    public CrystalAura() { super("Crystal Aura", "Pose et fait exploser des cristaux pres des joueurs ennemis.", Category.COMBAT); }

    /** Estimation des degats d'un cristal (sans armure ni obstacles : volontairement pessimiste). */
    private static double dmg(Vec3 crystal, Entity e) {
        double ratio = Math.sqrt(e.distanceToSqr(crystal)) / 12.0;
        if (ratio > 1) return 0;
        double impact = 1 - ratio;
        return (impact * impact + impact) / 2 * 84 + 1;
    }

    private boolean safe(double self) {
        return self <= maxSelf.d() && self < mc.player.getHealth() + mc.player.getAbsorptionAmount() - 1;
    }

    @Override public void onTick() {
        if (timer > 0) { timer--; return; }
        if (Compat.screen() != null) return;

        Player target = null; double best = Double.MAX_VALUE;
        for (Player p : mc.level.players()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
            double d = mc.player.distanceTo(p);
            if (d <= targetRange.d() && d < best) { best = d; target = p; }
        }
        if (target == null) return;
        Vec3 eye = mc.player.getEyePosition();

        if (breakC.on()) {
            Entity bestC = null; double bestD = -1;
            for (Entity e : Compat.entities()) {
                if (e.getType() != EntityType.END_CRYSTAL) continue;
                if (Util.distToBox(eye, e.getBoundingBox()) > breakRange.d()) continue;
                Vec3 pos = e.position();
                double dt = dmg(pos, target), ds = dmg(pos, mc.player);
                if (dt < minDmg.d() * 0.5 || !safe(ds)) continue;
                if (dt > bestD) { bestD = dt; bestC = e; }
            }
            if (bestC != null) {
                if (rotate.on()) Util.lookAt(eye, bestC.getBoundingBox().getCenter());
                Compat.attack(bestC);
                Compat.swing();
                timer = delay.i();
                return;
            }
        }

        if (!place.on()) return;
        boolean offhand = mc.player.getOffhandItem().is(Items.END_CRYSTAL);
        int slot = offhand ? -1 : Util.findHotbar(s -> s.is(Items.END_CRYSTAL));
        if (!offhand && slot < 0) return;

        int r = (int) Math.ceil(placeRange.d());
        BlockPos o = mc.player.blockPosition();
        BlockPos bestPos = null; double bestScore = -1e9;
        for (int dx = -r; dx <= r; dx++) for (int dy = -r; dy <= r; dy++) for (int dz = -r; dz <= r; dz++) {
            BlockPos base = o.offset(dx, dy, dz);
            var st = mc.level.getBlockState(base);
            if (!st.is(Blocks.OBSIDIAN) && !st.is(Blocks.BEDROCK)) continue;
            if (!mc.level.getBlockState(base.above()).isAir() || !mc.level.getBlockState(base.above(2)).isAir()) continue;
            Vec3 cpos = new Vec3(base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5);
            if (eye.distanceTo(cpos) > placeRange.d()) continue;
            AABB bb = new AABB(base.above()).expandTowards(0, 1, 0);
            if (!mc.level.getEntities((Entity) null, bb).isEmpty()) continue;
            double dt = dmg(cpos, target), ds = dmg(cpos, mc.player);
            if (dt < minDmg.d() || !safe(ds)) continue;
            double score = dt - ds;
            if (score > bestScore) { bestScore = score; bestPos = base; }
        }
        if (bestPos == null) return;

        InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (!offhand) {
            if (!autoSwitch.on() && Compat.selected() != slot) return;
            Compat.select(slot);
        }
        if (rotate.on()) Util.lookAt(eye, Vec3.atCenterOf(bestPos));
        Vec3 hit = Vec3.atCenterOf(bestPos).add(0, 0.5, 0);
        Compat.useBlock(hand, new BlockHitResult(hit, Direction.UP, bestPos, false));
        mc.player.swing(hand);
        timer = delay.i();
    }
}
