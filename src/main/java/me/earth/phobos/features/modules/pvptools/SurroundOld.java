package me.earth.phobos.features.modules.pvptools;

import me.earth.phobos.PigHack;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.util.EnumHand;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.Entity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.util.math.Vec3d;
import java.util.Set;
import net.minecraft.util.math.BlockPos;

public class SurroundOld extends Module
{
    private final Setting<Integer> blocksPerTick;
    private final Setting<Integer> delay;
    private final Setting<Boolean> noGhost;
    private final Setting<Boolean> center;
    private final Setting<Boolean> rotate;
    private final Timer timer;
    private final Timer retryTimer;
    private int isSafe;
    private BlockPos startPos;
    private boolean didPlace;
    private boolean switchedItem;
    private int lastHotbarSlot;
    private boolean isSneaking;
    private int placements;
    private final Set<Vec3d> extendingBlocks;
    private int extenders;
    public static boolean isPlacing;
    private int obbySlot;
    private boolean offHand;
    private final Map<BlockPos, Integer> retries;

    public SurroundOld() {
        super("Surround", "Surrounds you with Obsidian", Category.PVP, true, false, false);
        this.blocksPerTick = (Setting<Integer>)this.register(new Setting("BlocksPerTick", 12, 1, 20));
        this.delay = (Setting<Integer>)this.register(new Setting("Delay", 0, 0, 250));
        this.noGhost = (Setting<Boolean>)this.register(new Setting("PacketPlace", false));
        this.center = (Setting<Boolean>)this.register(new Setting("TPCenter", false));
        this.rotate = (Setting<Boolean>)this.register(new Setting("Rotate", true));
        this.timer = new Timer();
        this.retryTimer = new Timer();
        this.didPlace = false;
        this.placements = 0;
        this.extendingBlocks = new HashSet<Vec3d>();
        this.extenders = 1;
        this.obbySlot = -1;
        this.offHand = false;
        this.retries = new HashMap<BlockPos, Integer>();
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            this.disable();
        }
        this.lastHotbarSlot = SurroundOld.mc.player.inventory.currentItem;
        this.startPos = EntityUtil.getRoundedBlockPos((Entity)SurroundOld.mc.player);
        if (this.center.getValue()) {
            PigHack.positionManager.setPositionPacket(this.startPos.getX() + 0.5, this.startPos.getY(), this.startPos.getZ() + 0.5, true, true, true);
        }
        this.retries.clear();
        this.retryTimer.reset();
    }

    @Override
    public void onTick() {
        this.doFeetPlace();
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }
        SurroundOld.isPlacing = false;
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
    }

    @Override
    public String getDisplayInfo() {
        switch (this.isSafe) {
            case 0: {
                return ChatFormatting.RED + "Unsafe";
            }
            case 1: {
                return ChatFormatting.YELLOW + "Safe";
            }
            default: {
                return ChatFormatting.GREEN + "Safe";
            }
        }
    }

    private void doFeetPlace() {
        if (this.check()) {
            return;
        }
        if (!EntityUtil.isSafe((Entity)SurroundOld.mc.player, 0, true)) {
            this.isSafe = 0;
            this.placeBlocks(SurroundOld.mc.player.getPositionVector(), EntityUtil.getUnsafeBlockArray((Entity)SurroundOld.mc.player, 0, true), true, false, false);
        }
        else if (!EntityUtil.isSafe((Entity)SurroundOld.mc.player, -1, false)) {
            this.isSafe = 1;
            this.placeBlocks(SurroundOld.mc.player.getPositionVector(), EntityUtil.getUnsafeBlockArray((Entity)SurroundOld.mc.player, -1, false), false, false, true);
        }
        else {
            this.isSafe = 2;
        }
        this.processExtendingBlocks();
        if (this.didPlace) {
            this.timer.reset();
        }
    }

    private void processExtendingBlocks() {
        if (this.extendingBlocks.size() == 2 && this.extenders < 1) {
            final Vec3d[] array = new Vec3d[2];
            int i = 0;
            for (final Vec3d vec3d : this.extendingBlocks) {
                array[i] = vec3d;
                ++i;
            }
            final int placementsBefore = this.placements;
            if (this.areClose(array) != null) {
                this.placeBlocks(this.areClose(array), EntityUtil.getUnsafeBlockArrayFromVec3d(this.areClose(array), 0, true), true, false, true);
            }
            if (placementsBefore < this.placements) {
                this.extendingBlocks.clear();
            }
        }
        else if (this.extendingBlocks.size() > 2 || this.extenders >= 1) {
            this.extendingBlocks.clear();
        }
    }

    private Vec3d areClose(final Vec3d[] vec3ds) {
        int matches = 0;
        for (final Vec3d vec3d : vec3ds) {
            for (final Vec3d pos : EntityUtil.getUnsafeBlockArray((Entity)SurroundOld.mc.player, 0, true)) {
                if (vec3d.equals((Object)pos)) {
                    ++matches;
                }
            }
        }
        if (matches == 2) {
            return SurroundOld.mc.player.getPositionVector().add(vec3ds[0].add(vec3ds[1]));
        }
        return null;
    }

    private boolean placeBlocks(final Vec3d pos, final Vec3d[] vec3ds, final boolean hasHelpingBlocks, final boolean isHelping, final boolean isExtending) {
        boolean gotHelp = true;
        for (final Vec3d vec3d : vec3ds) {
            gotHelp = true;
            final BlockPos position = new BlockPos(pos).add(vec3d.x, vec3d.y, vec3d.z);
            Label_0304: {
                switch (BlockUtil.isPositionPlaceable(position, false)) {
                    case 1: {
                        if (this.retries.get(position) == null || this.retries.get(position) < 4) {
                            this.placeBlock(position);
                            this.retries.put(position, (this.retries.get(position) == null) ? 1 : (this.retries.get(position) + 1));
                            this.retryTimer.reset();
                            break;
                        }
                        if (PigHack.speedManager.getSpeedKpH() == 0.0 && !isExtending && this.extenders < 1) {
                            this.placeBlocks(SurroundOld.mc.player.getPositionVector().add(vec3d), EntityUtil.getUnsafeBlockArrayFromVec3d(SurroundOld.mc.player.getPositionVector().add(vec3d), 0, true), hasHelpingBlocks, false, true);
                            this.extendingBlocks.add(vec3d);
                            ++this.extenders;
                            break;
                        }
                        break;
                    }
                    case 2: {
                        if (hasHelpingBlocks) {
                            gotHelp = this.placeBlocks(pos, BlockUtil.getHelpingBlocks(vec3d), false, true, true);
                            break Label_0304;
                        }
                        break;
                    }
                    case 3: {
                        if (gotHelp) {
                            this.placeBlock(position);
                        }
                        if (isHelping) {
                            return true;
                        }
                        break;
                    }
                }
            }
        }
        return false;
    }

    private boolean check() {
        if (nullCheck()) {
            return true;
        }
        final int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        final int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
        if (obbySlot == -1 && eChestSot == -1) {
            this.toggle();
        }
        this.offHand = InventoryUtil.isBlock(SurroundOld.mc.player.getHeldItemOffhand().getItem(), BlockObsidian.class);
        SurroundOld.isPlacing = false;
        this.didPlace = false;
        this.extenders = 1;
        this.placements = 0;
        this.obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        final int echestSlot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
        if (this.isOff()) {
            return true;
        }
        if (this.retryTimer.passedMs(2500L)) {
            this.retries.clear();
            this.retryTimer.reset();
        }
        if (this.obbySlot == -1 && !this.offHand && echestSlot == -1) {
            Command.sendMessage("<" + this.getDisplayName() + "> " + ChatFormatting.RED + "No Obsidian in hotbar disabling...");
            this.disable();
            return true;
        }
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
        if (SurroundOld.mc.player.inventory.currentItem != this.lastHotbarSlot && SurroundOld.mc.player.inventory.currentItem != this.obbySlot && SurroundOld.mc.player.inventory.currentItem != echestSlot) {
            this.lastHotbarSlot = SurroundOld.mc.player.inventory.currentItem;
        }
        if (!this.startPos.equals((Object)EntityUtil.getRoundedBlockPos((Entity)SurroundOld.mc.player))) {
            this.disable();
            return true;
        }
        return !this.timer.passedMs(this.delay.getValue());
    }

    private void placeBlock(final BlockPos pos) {
        if (this.placements < this.blocksPerTick.getValue()) {
            final int originalSlot = SurroundOld.mc.player.inventory.currentItem;
            final int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
            final int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
            if (obbySlot == -1 && eChestSot == -1) {
                this.toggle();
            }
            SurroundOld.isPlacing = true;
            SurroundOld.mc.player.inventory.currentItem = ((obbySlot == -1) ? eChestSot : obbySlot);
            SurroundOld.mc.playerController.updateController();
            this.isSneaking = BlockUtil.placeBlock(pos, this.offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, this.rotate.getValue(), this.noGhost.getValue(), this.isSneaking);
            SurroundOld.mc.player.inventory.currentItem = originalSlot;
            SurroundOld.mc.playerController.updateController();
            this.didPlace = true;
            ++this.placements;
        }
    }

    static {
        SurroundOld.isPlacing = false;
    }
}