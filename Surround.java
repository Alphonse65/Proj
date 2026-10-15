package fr.rise.mods;

import fr.rise.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;

public class Surround extends Module {
    private final Setting.Num perTick = add(new Setting.Num("Blocs par tick", 2, 1, 4, true));
    private final Setting.Bool center = add(new Setting.Bool("Centrer le joueur", true));
    private final Setting.Bool disable = add(new Setting.Bool("Se desactiver apres", false));

    public Surround() { super("Surround", "Entoure vos pieds d'obsidienne.", Category.COMBAT); }

    @Override public void onEnable() {
        if (center.on() && mc.player != null)
            mc.player.setPos(Math.floor(mc.player.getX()) + 0.5, mc.player.getY(), Math.floor(mc.player.getZ()) + 0.5);
    }

    @Override public void onTick() {
        int slot = Util.findHotbar(s -> s.is(Items.OBSIDIAN) || s.is(Items.CRYING_OBSIDIAN));
        if (slot < 0) return;
        BlockPos feet = mc.player.blockPosition();
        int prev = Compat.selected(), placed = 0;
        boolean all = true;
        for (Direction d : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos p = feet.relative(d);
            if (!mc.level.getBlockState(p).canBeReplaced()) continue;
            all = false;
            if (placed >= perTick.i()) break;
            Compat.select(slot);
            if (Util.place(p)) placed++;
        }
        if (placed > 0) Compat.select(prev);
        if (all && disable.on()) setEnabled(false);
    }
}
