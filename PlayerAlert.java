package fr.rise.mods;

import fr.rise.*;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerAlert extends Module {
    private final Setting.Bool disconnect = add(new Setting.Bool("Se deconnecter", false));
    private final Set<UUID> seen = new HashSet<>();
    private int tick;

    public PlayerAlert() { super("Alerte joueurs", "Previent quand un joueur entre dans votre zone.", Category.UTILE); }

    @Override public void onEnable() { seen.clear(); }

    @Override public void onTick() {
        if (tick++ % 10 != 0) return;
        Set<UUID> now = new HashSet<>();
        for (Player p : mc.level.players()) {
            if (p == mc.player) continue;
            now.add(p.getUUID());
            if (seen.add(p.getUUID())) {
                info(p.getName().getString() + " est proche (" + Math.round(mc.player.distanceTo(p)) + " blocs)");
                if (disconnect.on()) Compat.disconnect("[Alerte] Joueur proche : " + p.getName().getString());
            }
        }
        seen.retainAll(now);
    }
}
