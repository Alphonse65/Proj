package fr.rise;

import fr.rise.mods.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.List;

import static fr.rise.Compat.mc;

public final class Bots {
    public static boolean baritoneLoaded() {
        FabricLoader l = FabricLoader.getInstance();
        return l.isModLoaded("baritone") || l.isModLoaded("baritone-meteor") || l.isModLoaded("baritone-api-fabric");
    }

    /** Commande Baritone (#...). Envoyee seulement si Baritone est installe (sinon elle fuiterait dans le chat public). */
    public static boolean baritone(String cmd) {
        if (!baritoneLoaded()) { Compat.msg("\u00a7d[Rise] \u00a7cBaritone n'est pas installe."); return false; }
        Compat.chat("#" + cmd);
        return true;
    }

    public static void command(String cmd) { Compat.command(cmd); }

    public static int freeSlots() {
        int n = 0;
        for (int i = 0; i < 36; i++) if (Compat.stack(i).isEmpty()) n++;
        return n;
    }

    public static boolean hasPickaxe() {
        for (int i = 0; i < 36; i++) if (Compat.stack(i).is(ItemTags.PICKAXES)) return true;
        return false;
    }

    public static boolean hasTotemInInventory() {
        for (int i = 0; i < 36; i++) if (Compat.stack(i).is(Items.TOTEM_OF_UNDYING)) return true;
        return false;
    }

    public static boolean hasTotem() { return mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING) || hasTotemInInventory(); }

    /** Amene l'item de l'inventaire (0-35) dans la main principale. */
    public static void toMainHand(int invIndex) {
        if (invIndex < 9) { Compat.select(invIndex); return; }
        Compat.click(0, invIndex, Compat.selected(), Compat.Click.SWAP);
    }

    public static void stopAll() {
        if (baritoneLoaded()) baritone("stop");
        for (Module m : List.of(Modules.get(FarmBot.class), Modules.get(DiamondMiner.class), Modules.get(BuilderBot.class),
                Modules.get(MarketBot.class), Modules.get(AutoSell.class), Modules.get(Nuker.class),
                Modules.get(AutoWalk.class), Modules.get(AutoFish.class), Modules.get(Flight.class)))
            if (m != null) m.setEnabled(false);
    }
}
