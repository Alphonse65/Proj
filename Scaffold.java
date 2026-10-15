package fr.rise.mods;

import fr.rise.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;

public class Scaffold extends Module {
    private final Setting.Bool restore = add(new Setting.Bool("Remettre le slot", true));

    public Scaffold() { super("Scaffold", "Pose des blocs sous vos pieds pendant que vous marchez.", Category.MONDE); }

    @Override public void onTick() {
        if (Compat.screen() != null) return;
        BlockPos below = mc.player.blockPosition().below();
        if (!mc.level.getBlockState(below).canBeReplaced()) return;
        int slot = Util.findHotbar(s -> s.getItem() instanceof BlockItem);
        if (slot < 0) return;
        int prev = Compat.selected();
        Compat.select(slot);
        Util.place(below);
        if (restore.on()) Compat.select(prev);
    }
}
