package fr.rise;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public final class Util {
    private static final Minecraft mc = Minecraft.getInstance();

    public static double distToBox(Vec3 p, AABB b) {
        double x = Math.max(b.minX, Math.min(p.x, b.maxX));
        double y = Math.max(b.minY, Math.min(p.y, b.maxY));
        double z = Math.max(b.minZ, Math.min(p.z, b.maxZ));
        return p.distanceTo(new Vec3(x, y, z));
    }

    /** Index (0-8) du premier item de la barre rapide qui correspond, sinon -1. */
    public static int findHotbar(Predicate<ItemStack> pred) {
        for (int i = 0; i < 9; i++) if (pred.test(Compat.stack(i))) return i;
        return -1;
    }

    public static void lookAt(Vec3 from, Vec3 to) {
        double dx = to.x - from.x, dy = to.y - from.y, dz = to.z - from.z;
        double h = Math.sqrt(dx * dx + dz * dz);
        mc.player.setYRot((float) Math.toDegrees(Math.atan2(dz, dx)) - 90f);
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(dy, h)));
    }

    /** Pose un bloc en s'appuyant sur un voisin solide. */
    public static boolean place(BlockPos target) {
        if (!mc.level.getBlockState(target).canBeReplaced()) return false;
        for (Direction d : Direction.values()) {
            BlockPos n = target.relative(d);
            BlockState s = mc.level.getBlockState(n);
            Direction face = d.getOpposite();
            if (s.isAir() || s.canBeReplaced() || !s.isFaceSturdy(mc.level, n, face)) continue;
            Vec3 hit = Vec3.atCenterOf(n).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
            Compat.useBlock(InteractionHand.MAIN_HAND, new BlockHitResult(hit, face, n, false));
            Compat.swing();
            return true;
        }
        return false;
    }

    public static boolean moving() {
        return mc.options.keyUp.isDown() || mc.options.keyDown.isDown() || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
    }

    /** Direction souhaitee (x,z) a partir des touches, selon le regard du joueur. Taille 1 si mouvement, sinon 0. */
    public static double[] moveDir() {
        double f = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
        double s = (mc.options.keyLeft.isDown() ? 1 : 0) - (mc.options.keyRight.isDown() ? 1 : 0);
        double yaw = Math.toRadians(mc.player.getYRot());
        double vx = -Math.sin(yaw) * f + Math.cos(yaw) * s;
        double vz = Math.cos(yaw) * f + Math.sin(yaw) * s;
        double len = Math.sqrt(vx * vx + vz * vz);
        return len == 0 ? new double[]{0, 0} : new double[]{vx / len, vz / len};
    }
}
