package fr.rise.mods;

import fr.rise.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;

public class ChestStealer extends Module {
    private final Setting.Num delay = add(new Setting.Num("Delai (ticks)", 1, 0, 10, true));
    private final Setting.Bool close = add(new Setting.Bool("Fermer quand vide", true));
    private int timer;

    public ChestStealer() { super("Chest Stealer", "Vide automatiquement coffres, barils et shulkers ouverts.", Category.MONDE); }

    @Override public void onTick() {
        AbstractContainerMenu h = mc.player.containerMenu;
        if (h == mc.player.inventoryMenu) return;
        if (!(h instanceof ChestMenu) && !(h instanceof ShulkerBoxMenu)) return;
        if (timer > 0) { timer--; return; }
        int size = h.slots.size() - 36;
        for (int i = 0; i < size; i++) {
            if (!h.getSlot(i).getItem().isEmpty()) {
                Compat.click(h.containerId, i, 0, Compat.Click.QUICK_MOVE);
                timer = delay.i();
                return;
            }
        }
        if (close.on()) mc.player.closeContainer();
    }
}
