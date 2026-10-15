package fr.rise.mods;

import fr.rise.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityEsp extends Module {
    private final Setting.Bool players = add(new Setting.Bool("Joueurs", true));
    private final Setting.Bool hostiles = add(new Setting.Bool("Monstres", true));
    private final Setting.Bool animals = add(new Setting.Bool("Animaux", false));
    private final Setting.Bool items = add(new Setting.Bool("Objets au sol", true));
    private final Setting.Bool crystals = add(new Setting.Bool("Cristaux", true));
    private final Setting.Num range = add(new Setting.Num("Portee", 128, 16, 256, true));

    public EntityEsp() { super("Entity ESP", "Boites sur les entites, a travers les murs.", Category.VISUEL); }

    @Override
    public void extract(Boxes out, float partial) {
        for (Entity e : Compat.entities()) {
            if (e == mc.player || mc.player.distanceTo(e) > range.i()) continue;
            float r, g, b;
            MobCategory cat = e.getType().getCategory();
            if (e instanceof Player && players.on()) { r = 1f; g = 0.5f; b = 0.8f; }
            else if (cat == MobCategory.MONSTER && hostiles.on()) { r = 1f; g = 0.2f; b = 0.2f; }
            else if (cat == MobCategory.CREATURE && animals.on()) { r = 0.4f; g = 1f; b = 0.5f; }
            else if (e.getType() == EntityType.ITEM && items.on()) { r = 1f; g = 0.9f; b = 0.2f; }
            else if (e.getType() == EntityType.END_CRYSTAL && crystals.on()) { r = 0.9f; g = 0.2f; b = 1f; }
            else continue;

            Vec3 lp = e.getPosition(partial);
            AABB box = e.getBoundingBox().move(lp.x - e.getX(), lp.y - e.getY(), lp.z - e.getZ());
            out.add(box, r, g, b, 0.30f);
        }
    }
}
