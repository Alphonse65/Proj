package fr.rise.mods;

import fr.rise.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class AutoArmor extends Module {
    private final Setting.Num delay = add(new Setting.Num("Delai (ticks)", 4, 1, 20, true));
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final int[] SLOT_ID = {5, 6, 7, 8};
    private int timer;

    public AutoArmor() { super("Auto Armor", "Equipe automatiquement la meilleure armure.", Category.COMBAT); }

    private static int score(ItemStack s) {
        if (s.isEmpty()) return 0;
        String p = Compat.itemId(s);
        if (p.contains("elytra")) return 0;
        if (p.startsWith("netherite")) return 6;
        if (p.startsWith("diamond")) return 5;
        if (p.startsWith("iron")) return 4;
        if (p.startsWith("chainmail")) return 3;
        if (p.startsWith("golden")) return 2;
        return 1;
    }

    private void quickMove(int slotId) {
        Compat.click(mc.player.inventoryMenu.containerId, slotId, 0, Compat.Click.QUICK_MOVE);
    }

    @Override public void onTick() {
        if (timer > 0) { timer--; return; }
        if (Compat.screen() != null) return;
        for (int k = 0; k < 4; k++) {
            int cur = score(mc.player.getItemBySlot(SLOTS[k]));
            int bestIdx = -1, bestScore = cur;
            for (int i = 0; i < 36; i++) {
                ItemStack st = Compat.stack(i);
                var eq = st.get(DataComponents.EQUIPPABLE);
                if (eq == null || eq.slot() != SLOTS[k]) continue;
                int sc = score(st);
                if (sc > bestScore) { bestScore = sc; bestIdx = i; }
            }
            if (bestIdx < 0) continue;
            if (cur > 0) {
                if (Bots.freeSlots() == 0) continue;
                quickMove(SLOT_ID[k]);
            } else {
                quickMove(bestIdx < 9 ? 36 + bestIdx : bestIdx);
            }
            timer = delay.i();
            return;
        }
    }
}
