package fr.rise;

import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/** Collecteur de boites a dessiner a travers les murs (rempli pendant la phase d'extraction). */
public final class Boxes {
    public record BoxState(double x1, double y1, double z1, double x2, double y2, double z2,
                           float r, float g, float b, float a) {}

    public static final int MAX = 1500;
    public final List<BoxState> list = new ArrayList<>();

    public void add(AABB box, float r, float g, float b, float a) {
        if (list.size() >= MAX) return;
        list.add(new BoxState(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, a));
    }
}
