package fr.rise.mods;

import fr.rise.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Nuker extends Module {
    private final Setting.Num radius = add(new Setting.Num("Rayon", 4, 1, 5, true));
    private final Setting.Choice mode = add(new Setting.Choice("Mode", 0, "Tout", "Minerais"));
    private final Setting.Bool noBelow = add(new Setting.Bool("Ne pas creuser sous les pieds", true));

    public Nuker() { super("Nuker", "Casse automatiquement les blocs autour de vous.", Category.MONDE); }

    @Override public void onTick() {
        if (Compat.screen() != null) return;
        BlockPos o = mc.player.blockPosition();
        int r = radius.i();
        BlockPos best = null; double bd = Double.MAX_VALUE;
        Vec3 eye = mc.player.getEyePosition();
        for (int dx = -r; dx <= r; dx++) for (int dy = -r; dy <= r; dy++) for (int dz = -r; dz <= r; dz++) {
            BlockPos p = o.offset(dx, dy, dz);
            BlockState st = mc.level.getBlockState(p);
            if (st.isAir() || !st.getFluidState().isEmpty() || st.getDestroySpeed(mc.level, p) < 0) continue;
            if (noBelow.on() && p.getY() < mc.player.getBlockY()) continue;
            if (mode.idx() == 1) {
                String id = Compat.blockId(st.getBlock());
                if (!id.endsWith("_ore") && !id.equals("ancient_debris")) continue;
            }
            double d = eye.distanceToSqr(Vec3.atCenterOf(p));
            if (d > 20.25 || d >= bd) continue;
            bd = d; best = p;
        }
        if (best == null) return;
        Compat.destroy(best);
        Compat.swing();
    }
}
