package fr.rise;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Regroupe les appels les plus sensibles aux changements de version de Minecraft.
 * Si une mise a jour casse la compilation, c'est ici qu'il faut corriger en priorite.
 */
public final class Compat {
    public static final Minecraft mc = Minecraft.getInstance();

    public enum Click { PICKUP, QUICK_MOVE, SWAP }

    public static void msg(String s) { if (mc.player != null) mc.player.sendSystemMessage(Component.literal(s)); }

    public static void command(String c) {
        var con = mc.getConnection();
        if (con != null) con.sendCommand(c.startsWith("/") ? c.substring(1) : c);
    }

    public static void chat(String s) { var con = mc.getConnection(); if (con != null) con.sendChat(s); }

    public static Screen screen() { return mc.gui.screen(); }
    public static void setScreen(Screen s) { mc.gui.setScreen(s); }

    public static void disconnect(String reason) {
        var con = mc.getConnection();
        if (con != null) con.getConnection().disconnect(Component.literal(reason));
    }

    public static int selected() { return mc.player.getInventory().getSelectedSlot(); }
    public static void select(int slot) { if (slot >= 0 && slot < 9) mc.player.getInventory().setSelectedSlot(slot); }
    public static ItemStack stack(int invIndex) { return mc.player.getInventory().getItem(invIndex); }

    public static void click(int containerId, int slot, int button, Click mode) {
        ClickType t = switch (mode) {
            case PICKUP -> ClickType.PICKUP;
            case QUICK_MOVE -> ClickType.QUICK_MOVE;
            case SWAP -> ClickType.SWAP;
        };
        mc.gameMode.handleInventoryMouseClick(containerId, slot, button, t, mc.player);
    }

    public static void useBlock(InteractionHand h, BlockHitResult r) { mc.gameMode.useItemOn(mc.player, h, r); }
    public static void useItem(InteractionHand h) { mc.gameMode.useItem(mc.player, h); }
    public static void attack(Entity e) { mc.gameMode.attack(mc.player, e); }
    public static void destroy(BlockPos p) { mc.gameMode.continueDestroyBlock(p, Direction.UP); }
    public static void swing() { mc.player.swing(InteractionHand.MAIN_HAND); }

    public static boolean onGround() { return mc.player.onGround(); }
    public static Iterable<Entity> entities() { return mc.level.entitiesForRendering(); }

    public static LevelChunk chunk(int cx, int cz) {
        ChunkAccess c = mc.level.getChunk(cx, cz, ChunkStatus.FULL, false);
        return c instanceof LevelChunk lc ? lc : null;
    }

    public static String itemId(ItemStack s) { return BuiltInRegistries.ITEM.getKey(s.getItem()).getPath(); }
    public static String blockId(Block b) { return BuiltInRegistries.BLOCK.getKey(b).getPath(); }

    public static String tooltip(ItemStack s) {
        StringBuilder sb = new StringBuilder();
        for (Component c : s.getTooltipLines(Item.TooltipContext.EMPTY, mc.player, TooltipFlag.NORMAL))
            sb.append(c.getString()).append('\n');
        return sb.toString();
    }

    public static void sendOnGround() {
        var con = mc.getConnection();
        if (con != null) con.send(new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision));
    }

    public static boolean key(net.minecraft.client.KeyMapping k) { return k.isDown(); }
}
