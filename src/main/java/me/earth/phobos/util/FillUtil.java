package me.earth.phobos.util;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;

import java.util.Arrays;
import java.util.List;

public class FillUtil
{
    private static final Minecraft mc;
    public static List<Block> emptyBlocks;
    public static List<Block> rightclickableBlocks;

    public static boolean canSeeBlock(final BlockPos p_Pos) {
        return FillUtil.mc.player != null && FillUtil.mc.world.rayTraceBlocks(new Vec3d(FillUtil.mc.player.posX, FillUtil.mc.player.posY + FillUtil.mc.player.getEyeHeight(), FillUtil.mc.player.posZ), new Vec3d((double)p_Pos.getX(), (double)p_Pos.getY(), (double)p_Pos.getZ()), false, true, false) == null;
    }

    public static void placeCrystalOnBlock(final BlockPos pos, final EnumHand hand) {
        final RayTraceResult result = FillUtil.mc.world.rayTraceBlocks(new Vec3d(FillUtil.mc.player.posX, FillUtil.mc.player.posY + FillUtil.mc.player.getEyeHeight(), FillUtil.mc.player.posZ), new Vec3d(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5));
        final EnumFacing facing = (result == null || result.sideHit == null) ? EnumFacing.UP : result.sideHit;
        FillUtil.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock(pos, facing, hand, 0.0f, 0.0f, 0.0f));
    }

    public static boolean rayTracePlaceCheck(final BlockPos pos, final boolean shouldCheck, final float height) {
        return !shouldCheck || FillUtil.mc.world.rayTraceBlocks(new Vec3d(FillUtil.mc.player.posX, FillUtil.mc.player.posY + FillUtil.mc.player.getEyeHeight(), FillUtil.mc.player.posZ), new Vec3d((double)pos.getX(), (double)(pos.getY() + height), (double)pos.getZ()), false, true, false) == null;
    }

    public static boolean rayTracePlaceCheck(final BlockPos pos, final boolean shouldCheck) {
        return rayTracePlaceCheck(pos, shouldCheck, 1.0f);
    }

    public static void openBlock(final BlockPos pos) {
        final EnumFacing[] values;
        final EnumFacing[] facings = values = EnumFacing.values();
        for (final EnumFacing f : values) {
            final Block neighborBlock = FillUtil.mc.world.getBlockState(pos.offset(f)).getBlock();
            if (FillUtil.emptyBlocks.contains(neighborBlock)) {
                FillUtil.mc.playerController.processRightClickBlock(FillUtil.mc.player, FillUtil.mc.world, pos, f.getOpposite(), new Vec3d((Vec3i)pos), EnumHand.MAIN_HAND);
                return;
            }
        }
    }

    public static boolean placeBlock(final BlockPos pos) {
        if (isBlockEmpty(pos)) {
            final EnumFacing[] values;
            final EnumFacing[] facings = values = EnumFacing.values();
            for (final EnumFacing f : values) {
                final Block neighborBlock = FillUtil.mc.world.getBlockState(pos.offset(f)).getBlock();
                final Vec3d vec = new Vec3d(pos.getX() + 0.5 + f.getXOffset() * 0.5, pos.getY() + 0.5 + f.getYOffset() * 0.5, pos.getZ() + 0.5 + f.getZOffset() * 0.5);
                if (!FillUtil.emptyBlocks.contains(neighborBlock) && FillUtil.mc.player.getPositionEyes(FillUtil.mc.getRenderPartialTicks()).distanceTo(vec) <= 4.25) {
                    final float[] rot = { FillUtil.mc.player.rotationYaw, FillUtil.mc.player.rotationPitch };
                    if (FillUtil.rightclickableBlocks.contains(neighborBlock)) {
                        FillUtil.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)FillUtil.mc.player, CPacketEntityAction.Action.START_SNEAKING));
                    }
                    FillUtil.mc.playerController.processRightClickBlock(FillUtil.mc.player, FillUtil.mc.world, pos.offset(f), f.getOpposite(), new Vec3d((Vec3i)pos), EnumHand.MAIN_HAND);
                    if (FillUtil.rightclickableBlocks.contains(neighborBlock)) {
                        FillUtil.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)FillUtil.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                    }
                    FillUtil.mc.player.swingArm(EnumHand.MAIN_HAND);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isBlockEmpty(final BlockPos pos) {
        try {
            if (FillUtil.emptyBlocks.contains(FillUtil.mc.world.getBlockState(pos).getBlock())) {
                final AxisAlignedBB box = new AxisAlignedBB(pos);
                for (final Entity e : FillUtil.mc.world.loadedEntityList) {
                    if (e instanceof EntityLivingBase && box.intersects(e.getEntityBoundingBox())) {
                        return false;
                    }
                }
                return true;
            }
        }
        catch (Exception ex) {}
        return false;
    }

    public static boolean canPlaceBlock(final BlockPos pos) {
        if (isBlockEmpty(pos)) {
            final EnumFacing[] values;
            final EnumFacing[] facings = values = EnumFacing.values();
            for (final EnumFacing f : values) {
                if (!FillUtil.emptyBlocks.contains(FillUtil.mc.world.getBlockState(pos.offset(f)).getBlock()) && FillUtil.mc.player.getPositionEyes(FillUtil.mc.getRenderPartialTicks()).distanceTo(new Vec3d(pos.getX() + 0.5 + f.getXOffset() * 0.5, pos.getY() + 0.5 + f.getYOffset() * 0.5, pos.getZ() + 0.5 + f.getZOffset() * 0.5)) <= 4.25) {
                    return true;
                }
            }
        }
        return false;
    }

    static {
        mc = Minecraft.getMinecraft();
        FillUtil.emptyBlocks = Arrays.asList(Blocks.AIR, (Block)Blocks.FLOWING_LAVA, (Block)Blocks.LAVA, (Block)Blocks.FLOWING_WATER, (Block)Blocks.WATER, Blocks.VINE, Blocks.SNOW_LAYER, (Block)Blocks.TALLGRASS, (Block)Blocks.FIRE);
        FillUtil.rightclickableBlocks = Arrays.asList((Block)Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.ANVIL, Blocks.WOODEN_BUTTON, Blocks.STONE_BUTTON, (Block)Blocks.UNPOWERED_COMPARATOR, (Block)Blocks.UNPOWERED_REPEATER, (Block)Blocks.POWERED_REPEATER, (Block)Blocks.POWERED_COMPARATOR, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.ACACIA_FENCE_GATE, Blocks.BREWING_STAND, Blocks.DISPENSER, Blocks.DROPPER, Blocks.LEVER, Blocks.NOTEBLOCK, Blocks.JUKEBOX, (Block)Blocks.BEACON, Blocks.BED, Blocks.FURNACE, (Block)Blocks.OAK_DOOR, (Block)Blocks.SPRUCE_DOOR, (Block)Blocks.BIRCH_DOOR, (Block)Blocks.JUNGLE_DOOR, (Block)Blocks.ACACIA_DOOR, (Block)Blocks.DARK_OAK_DOOR, Blocks.CAKE, Blocks.ENCHANTING_TABLE, Blocks.DRAGON_EGG, (Block)Blocks.HOPPER, Blocks.REPEATING_COMMAND_BLOCK, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.CRAFTING_TABLE);
    }
}