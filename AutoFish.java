package fr.rise.mods;

import fr.rise.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

public class AutoFish extends Module {
    private final Setting.Num recastDelay = add(new Setting.Num("Delai recast (ticks)", 20, 5, 100, true));
    private int timer;

    public AutoFish() { super("Auto Fish", "Peche automatiquement (canne a peche en main).", Category.MONDE); }

    @Override public void onTick() {
        if (!mc.player.getMainHandItem().is(Items.FISHING_ROD)) return;
        if (timer > 0) { timer--; return; }
        var hook = mc.player.fishing;
        if (hook == null) {
            Compat.useItem(InteractionHand.MAIN_HAND);
            timer = recastDelay.i();
        } else if (hook.tickCount > 20 && hook.isInWater() && hook.getDeltaMovement().y < -0.075) {
            Compat.useItem(InteractionHand.MAIN_HAND);
            timer = recastDelay.i();
        }
    }
}
