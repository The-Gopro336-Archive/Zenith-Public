package me.gopro336.zenith.api.util.newRotations;

import java.util.ArrayList;
import java.util.List;
import me.gopro336.zenith.Zenith;
import me.gopro336.zenith.asm.mixin.imixin.IMinecraft;
import me.gopro336.zenith.asm.mixin.mixins.accessor.IEntityPlayerSP;
import me.gopro336.zenith.event.client.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class Rotation {
    public static Minecraft mc = Minecraft.getMinecraft();

    public static void placeBlock(Placement placement, EnumHand enumHand, boolean bl) {
        rightClickBlock(placement.getNeighbour(), placement.getHitVec(), enumHand, placement.getOpposite(), bl, true);
    }

    public static void placeBlockSafely(Placement placement, EnumHand enumHand, boolean packet) {
        boolean isSprinting = mc.player.isSprinting();
        boolean shouldSneak = BlockUtils.shouldSneakWhileRightClicking(placement.getNeighbour());
        if (isSprinting) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
        }
        if (shouldSneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
        placeBlock(placement, enumHand, packet);
        if (shouldSneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
        if (isSprinting) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }
    }

    public static boolean canPlaceNormally(boolean rotate) {
        if (!rotate) {
            return true;
        }
        return !Zenith.INSTANCE.rotationManager.isRotationsSet();
    }

    public static boolean canClick(BlockPos blockPos) {
        return mc.world.getBlockState(blockPos).getBlock().canCollideCheck(mc.world.getBlockState(blockPos), false);
    }

    public static List<EnumFacing> getPlacableFacings(BlockPos blockPos, boolean strictDirection, boolean rayTrace) {
        ArrayList<EnumFacing> list = new ArrayList<EnumFacing>();
        for (EnumFacing e : EnumFacing.values()) {
            IBlockState blockState2;
            RayTraceResult rayTraceBlocks;
            if (rayTrace && (rayTraceBlocks = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1.0f), new Vec3d((Vec3i)blockPos).add(0.5, 0.5, 0.5).add(new Vec3d(e.getDirectionVec()).scale(0.5)))) != null && rayTraceBlocks.typeOfHit != RayTraceResult.Type.MISS) {
                System.out.println("weary");
                continue;
            }
            BlockPos offset = blockPos.offset(e);
            if (strictDirection) {
                Vec3d positionEyes = mc.player.getPositionEyes(1.0f);
                Vec3d vec3d = new Vec3d((double)offset.getX() + 0.5, (double)offset.getY() + 0.5, (double)offset.getZ() + 0.5);
                IBlockState blockState = mc.world.getBlockState(offset);
                boolean b3 = blockState.getBlock() == Blocks.AIR || blockState.isFullBlock();
                ArrayList<EnumFacing> list2 = new ArrayList<EnumFacing>();
                list2.addAll(checkAxis(positionEyes.x - vec3d.x, EnumFacing.WEST, EnumFacing.EAST, !b3));
                list2.addAll(checkAxis(positionEyes.y - vec3d.y, EnumFacing.DOWN, EnumFacing.UP, true));
                list2.addAll(checkAxis(positionEyes.z - vec3d.z, EnumFacing.NORTH, EnumFacing.SOUTH, !b3));
                if (!list2.contains(e.getOpposite())) continue;
            }
            if ((blockState2 = mc.world.getBlockState(offset)) == null || !blockState2.getBlock().canCollideCheck(blockState2, false) || blockState2.getMaterial().isReplaceable()) continue;
            list.add(e);
        }
        return list;
    }

    public static Placement preparePlacement(BlockPos blockPos, boolean rotate, boolean instant, boolean strictDirection, boolean rayTrace) {
        EnumFacing enumFacing = null;
        Vec3d vec3d = null;
        double n = 69420.0;
        for (EnumFacing enumFacing2 : getPlacableFacings(blockPos, strictDirection, rayTrace)) {
            Vec3d add = new Vec3d((Vec3i)blockPos.offset(enumFacing2)).add(0.5, 0.5, 0.5).add(new Vec3d(enumFacing2.getDirectionVec()).scale(0.5));
            if (!(mc.player.getPositionVector().add(0.0, (double)mc.player.getEyeHeight(), 0.0).distanceTo(add) < 69420.0)) continue;
            enumFacing = enumFacing2;
            vec3d = add;
        }
        if (enumFacing == null) {
            return null;
        }
        BlockPos offset = blockPos.offset(enumFacing);
        EnumFacing opposite = enumFacing.getOpposite();
        float[] method1946 = RotationManager.calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vec3d);
        if (rotate) {
            if (instant) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(method1946[0], method1946[1], mc.player.onGround));
                ((IEntityPlayerSP)mc.player).setLastReportedYaw(method1946[0]);
                ((IEntityPlayerSP)mc.player).setLastReportedPitch(method1946[1]);
            } else {
                Zenith.INSTANCE.rotationManager.setRotations(method1946[0], method1946[1]);
            }
        }
        return new Placement(offset, opposite, vec3d, method1946[0], method1946[1]);
    }

    public static boolean canPlaceBlock(BlockPos blockPos, boolean strictDirection, boolean checkEntities) {
        return canPlaceBlock(blockPos, strictDirection, false, checkEntities);
    }

    public static boolean canPlaceNormally() {
        return !Zenith.INSTANCE.rotationManager.isRotationsSet();
    }

    public static boolean canPlaceBlock(BlockPos blockPos, boolean strictDirection) {
        return canPlaceBlock(blockPos, strictDirection, true);
    }

    public static Placement preparePlacement(BlockPos blockPos, boolean rotate, boolean instant, boolean strictDirection) {
        return preparePlacement(blockPos, rotate, instant, strictDirection, false);
    }

    public static void rightClickBlock(BlockPos blockPos, Vec3d vec3d, EnumHand enumHand, EnumFacing enumFacing, boolean packet, boolean swing) {
        if (packet) {
            float dX = (float)(vec3d.x - (double)blockPos.getX());
            float dY = (float)(vec3d.y - (double)blockPos.getY());
            float dZ = (float)(vec3d.z - (double)blockPos.getZ());
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(blockPos, enumFacing, enumHand, dX, dY, dZ));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, blockPos, enumFacing, vec3d, enumHand);
        }
        if (swing) {
            mc.player.connection.sendPacket(new CPacketAnimation(enumHand));
        }
        ((IMinecraft)mc).setRightClickDelayTimer(4);
    }

    public static boolean canPlaceBlock(BlockPos blockPos, boolean strictDirection, boolean rayTrace, boolean checkEntities) {
        Block block = mc.world.getBlockState(blockPos).getBlock();
        if (!(block instanceof BlockAir || block instanceof BlockLiquid || block instanceof BlockTallGrass || block instanceof BlockFire || block instanceof BlockDeadBush || block instanceof BlockSnow)) {
            return false;
        }
        if (checkEntities) {
            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos))) {
                if (entity instanceof EntityItem || entity instanceof EntityXPOrb) continue;
                return false;
            }
        }
        for (EnumFacing side : getPlacableFacings(blockPos, strictDirection, rayTrace)) {
            if (!canClick(blockPos.offset(side))) continue;
            return true;
        }
        return false;
    }

    public static ArrayList<EnumFacing> checkAxis(double diff, EnumFacing negativeSide, EnumFacing positiveSide, boolean bothIfInRange) {
        ArrayList<EnumFacing> arrayList = new ArrayList<EnumFacing>();
        if (diff < -0.5) {
            arrayList.add(negativeSide);
        }
        if (diff > 0.5) {
            arrayList.add(positiveSide);
        }
        if (bothIfInRange) {
            if (!arrayList.contains(negativeSide)) {
                arrayList.add(negativeSide);
            }
            if (!arrayList.contains(positiveSide)) {
                arrayList.add(positiveSide);
            }
        }
        return arrayList;
    }

    public static boolean canRayTrace(BlockPos blockPos) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ), new Vec3d((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), false, true, false) == null;
    }
}
