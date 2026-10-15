package fr.rise.mods;

import fr.rise.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class FullBright extends Module {
    private int tick;
    public FullBright() { super("Fullbright", "Vision nocturne visuelle (cote client).", Category.VISUEL); }

    @Override public void onTick() {
        if (tick++ % 100 == 0) mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
    }
    @Override public void onDisable() { if (mc.player != null) mc.player.removeEffect(MobEffects.NIGHT_VISION); }
}
