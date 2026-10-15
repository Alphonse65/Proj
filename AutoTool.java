package fr.rise.mods;

import fr.rise.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoTool extends Module {
    public AutoTool() { super("Auto Tool", "Choisit l'outil le plus rapide quand vous cassez un bloc.", Category.MONDE); }

    @Override public void onTick() {
        if (!mc.options.keyAttack.isDown() || Compat.screen() != null) return;
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        BlockState st = mc.level.getBlockState(hit.getBlockPos());
        int best = Compat.selected();
        float bestSpeed = Compat.stack(best).getDestroySpeed(st);
        for (int i = 0; i < 9; i++) {
            float sp = Compat.stack(i).getDestroySpeed(st);
            if (sp > bestSpeed) { bestSpeed = sp; best = i; }
        }
        Compat.select(best);
    }
}
