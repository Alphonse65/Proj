package fr.rise.mods;

import fr.rise.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class Esp extends Module {
    private final Setting.Num radius = add(new Setting.Num("Rayon (chunks)", 12, 1, 32, true));
    private final Setting.Bool chests = add(new Setting.Bool("Coffres", true));
    private final Setting.Bool enders = add(new Setting.Bool("Ender chests", true));
    private final Setting.Bool shulkers = add(new Setting.Bool("Shulker boxes", true));
    private final Setting.Bool spawners = add(new Setting.Bool("Spawners", true));
    private final Setting.Bool anchors = add(new Setting.Bool("Respawn anchors", true));

    private record Hit(BlockPos pos, float r, float g, float b) {}
    private volatile List<Hit> hits = List.of();
    private int tick;

    public Esp() { super("ESP Stockage", "Boites a travers les murs : coffres, shulkers, spawners, anchors.", Category.VISUEL); }

    @Override public void onDisable() { hits = List.of(); }

    @Override
    public void onTick() {
        if (tick++ % 20 != 0) return;
        List<Hit> found = new ArrayList<>();
        int pcx = mc.player.getBlockX() >> 4, pcz = mc.player.getBlockZ() >> 4, r = radius.i();

        for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
            int cx = pcx + dx, cz = pcz + dz;
            LevelChunk chunk = Compat.chunk(cx, cz);
            if (chunk == null) continue;

            for (BlockEntity be : chunk.getBlockEntities().values()) {
                BlockPos p = be.getBlockPos();
                if (be instanceof EnderChestBlockEntity) { if (enders.on()) found.add(new Hit(p, 0.7f, 0.2f, 1f)); }
                else if (be instanceof ChestBlockEntity) { if (chests.on()) found.add(new Hit(p, 1f, 0.65f, 0f)); }
                else if (be instanceof ShulkerBoxBlockEntity) { if (shulkers.on()) found.add(new Hit(p, 0f, 0.9f, 0.9f)); }
                else if (be instanceof SpawnerBlockEntity) { if (spawners.on()) found.add(new Hit(p, 1f, 0.1f, 0.1f)); }
            }

            if (!anchors.on()) continue;
            LevelChunkSection[] secs = chunk.getSections();
            for (int i = 0; i < secs.length; i++) {
                LevelChunkSection s = secs[i];
                if (s == null || s.hasOnlyAir() || !s.maybeHas(st -> st.is(Blocks.RESPAWN_ANCHOR))) continue;
                int bx = cx << 4, bz = cz << 4, by = (chunk.getMinSectionY() + i) << 4;
                for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++)
                    if (s.getBlockState(x, y, z).is(Blocks.RESPAWN_ANCHOR))
                        found.add(new Hit(new BlockPos(bx + x, by + y, bz + z), 0.9f, 0f, 0.7f));
            }
        }
        hits = found;
    }

    @Override
    public void extract(Boxes out, float partial) {
        for (Hit h : hits) out.add(new AABB(h.pos()), h.r(), h.g(), h.b(), 0.38f);
    }
}
