package fr.rise.mods;

import fr.rise.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FarmBot extends Module {
    private final Setting.Choice mode = add(new Setting.Choice("Mode", 0, "Portee", "Baritone"));
    private final Setting.Num reach = add(new Setting.Num("Portee", 4, 1, 5, true));
    private final Setting.Num delay = add(new Setting.Num("Delai (ticks)", 2, 0, 10, true));
    private final Setting.Bool replant = add(new Setting.Bool("Replanter", true));
    private final Setting.Num bRange = add(new Setting.Num("Rayon Baritone", 30, 5, 100, true));

    private static final List<Item> SEEDS = List.of(Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS, Items.NETHER_WART);
    private int timer;

    public FarmBot() { super("Farm Bot", "Recolte et replante les cultures (Portee ou Baritone #farm).", Category.BOTS); }

    @Override public void onEnable() { if (mode.idx() == 1) Bots.baritone("farm " + bRange.i()); }
    @Override public void onDisable() { if (mode.idx() == 1) Bots.baritone("stop"); }

    @Override public void onTick() {
        if (mode.idx() != 0 || Compat.screen() != null) return;
        if (timer > 0) { timer--; return; }
        BlockPos o = mc.player.blockPosition();
        int r = reach.i();

        for (int dx = -r; dx <= r; dx++) for (int dy = -2; dy <= 2; dy++) for (int dz = -r; dz <= r; dz++) {
            BlockPos pos = o.offset(dx, dy, dz);
            if (!inReach(pos)) continue;
            BlockState st = mc.level.getBlockState(pos);
            boolean mature = (st.getBlock() instanceof CropBlock c && c.isMaxAge(st))
                    || (st.getBlock() instanceof NetherWartBlock && st.getValue(NetherWartBlock.AGE) >= 3);
            if (mature) {
                Compat.destroy(pos);
                Compat.swing();
                timer = delay.i();
                return;
            }
        }
        if (!replant.on()) return;
        for (int dx = -r; dx <= r; dx++) for (int dy = -2; dy <= 1; dy++) for (int dz = -r; dz <= r; dz++) {
            BlockPos soil = o.offset(dx, dy, dz);
            BlockState ss = mc.level.getBlockState(soil);
            boolean farmland = ss.getBlock() instanceof FarmBlock, sand = ss.is(Blocks.SOUL_SAND);
            if ((!farmland && !sand) || !mc.level.getBlockState(soil.above()).isAir() || !inReach(soil)) continue;
            int slot = -1;
            for (int i = 0; i < 9; i++) {
                var s = Compat.stack(i);
                if (SEEDS.contains(s.getItem()) && (sand == s.is(Items.NETHER_WART))) { slot = i; break; }
            }
            if (slot < 0) return;
            Compat.select(slot);
            Vec3 hit = Vec3.atCenterOf(soil).add(0, 0.5, 0);
            Compat.useBlock(InteractionHand.MAIN_HAND, new BlockHitResult(hit, Direction.UP, soil, false));
            Compat.swing();
            timer = delay.i();
            return;
        }
    }

    private boolean inReach(BlockPos p) { return mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(p)) <= 20.25; }
}
