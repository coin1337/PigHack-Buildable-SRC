package me.earth.phobos.features.modules.pvptools;

import me.earth.phobos.PigHack;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.*;
import net.minecraft.util.EnumHand;
import net.minecraft.block.BlockEnderChest;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.BlockObsidian;

import java.util.Comparator;
import net.minecraft.util.math.Vec3d;
import java.util.List;
import net.minecraft.entity.Entity;
import java.util.HashMap;
import net.minecraft.util.math.BlockPos;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;

public class AutoTrapOld extends Module
{
    private final Setting<Integer> delay;
    private final Setting<Integer> blocksPerPlace;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> raytrace;
    private final Setting<Boolean> antiScaffold;
    private final Setting<Boolean> antiStep;

    private final Timer timer;
    private boolean didPlace;
    private boolean switchedItem;
    public EntityPlayer target;
    private boolean isSneaking;
    private int lastHotbarSlot;
    private int placements;
    public static boolean isPlacing;
    private boolean smartRotate;
    private final Map<BlockPos, Integer> retries;
    private final Timer retryTimer;
    private BlockPos startPos;

    public AutoTrapOld() {
        super("AutoTrap", "Traps other players", Category.PVP, true, false, false);
        this.delay = (Setting<Integer>)this.register(new Setting("Delay", 50, 0, 250));
        this.blocksPerPlace = (Setting<Integer>)this.register(new Setting("BlocksPerTick", 8, 1, 30));
        this.rotate = (Setting<Boolean>)this.register(new Setting("Rotate", true));
        this.raytrace = (Setting<Boolean>)this.register(new Setting("Raytrace", false));
        this.antiScaffold = (Setting<Boolean>)this.register(new Setting("AntiScaffold", false));
        this.antiStep = (Setting<Boolean>)this.register(new Setting("AntiStep", false));
        this.timer = new Timer();
        this.didPlace = false;
        this.placements = 0;
        this.smartRotate = false;
        this.retries = new HashMap<BlockPos, Integer>();
        this.retryTimer = new Timer();
        this.startPos = null;
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        this.startPos = EntityUtil.getRoundedBlockPos((Entity)AutoTrapOld.mc.player);
        this.lastHotbarSlot = AutoTrapOld.mc.player.inventory.currentItem;
        this.retries.clear();
    }

    @Override
    public void onTick() {
        if (fullNullCheck()) {
            return;
        }
        this.smartRotate = false;
        this.doTrap();
    }

    @Override
    public String getDisplayInfo() {
        if (this.target != null) {
            return this.target.getName();
        }
        return null;
    }

    @Override
    public void onDisable() {
        AutoTrapOld.isPlacing = false;
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
    }

    private void doTrap() {
        if (this.check()) {
            return;
        }
        this.doStaticTrap();
        if (this.didPlace) {
            this.timer.reset();
        }
    }

    private void doStaticTrap() {
        final List<Vec3d> placeTargets = EntityUtil.targets(this.target.getPositionVector(), this.antiScaffold.getValue(), this.antiStep.getValue(), false, false, false, this.raytrace.getValue());
        this.placeList(placeTargets);
    }

    private void placeList(final List<Vec3d> list) {
        list.sort((vec3d, vec3d2) -> Double.compare(AutoTrapOld.mc.player.getDistanceSq(vec3d2.x, vec3d2.y, vec3d2.z), AutoTrapOld.mc.player.getDistanceSq(vec3d.x, vec3d.y, vec3d.z)));
        list.sort(Comparator.comparingDouble(vec3d -> vec3d.y));
        for (final Vec3d vec3d3 : list) {
            final BlockPos position = new BlockPos(vec3d3);
            final int placeability = BlockUtil.isPositionPlaceable(position, this.raytrace.getValue());
            if (placeability == 1 && (this.retries.get(position) == null || this.retries.get(position) < 4)) {
                this.placeBlock(position);
                this.retries.put(position, (this.retries.get(position) == null) ? 1 : (this.retries.get(position) + 1));
                this.retryTimer.reset();
            }
            else {
                if (placeability != 3) {
                    continue;
                }
                this.placeBlock(position);
            }
        }
    }

    private boolean check() {
        AutoTrapOld.isPlacing = false;
        this.didPlace = false;
        this.placements = 0;
        final int obbySlot2 = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        if (obbySlot2 == -1) {
            this.toggle();
        }
        final int obbySlot3 = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        if (this.isOff()) {
            return true;
        }
        if (!this.startPos.equals((Object)EntityUtil.getRoundedBlockPos((Entity)AutoTrapOld.mc.player))) {
            this.disable();
            return true;
        }
        if (this.retryTimer.passedMs(2000L)) {
            this.retries.clear();
            this.retryTimer.reset();
        }
        if (obbySlot3 == -1) {
            Command.sendMessage("<" + this.getDisplayName() + "> " + ChatFormatting.RED + "No Obsidian in hotbar disabling...");
            this.disable();
            return true;
        }
        if (AutoTrapOld.mc.player.inventory.currentItem != this.lastHotbarSlot && AutoTrapOld.mc.player.inventory.currentItem != obbySlot3) {
            this.lastHotbarSlot = AutoTrapOld.mc.player.inventory.currentItem;
        }
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
        this.target = this.getTarget(10.0, true);
        return this.target == null || !this.timer.passedMs(this.delay.getValue());
    }

    private EntityPlayer getTarget(final double range, final boolean trapped) {
        EntityPlayer target = null;
        double distance = Math.pow(range, 2.0) + 1.0;
        for (final EntityPlayer player : AutoTrapOld.mc.world.playerEntities) {
            if (EntityUtil.isntValid((Entity)player, range)) {
                continue;
            }
            if (trapped && EntityUtil.isTrapped(player, this.antiScaffold.getValue(), this.antiStep.getValue(), false, false, false)) {
                continue;
            }
            if (PigHack.speedManager.getPlayerSpeed(player) > 10.0) {
                continue;
            }
            if (target == null) {
                target = player;
                distance = AutoTrapOld.mc.player.getDistanceSq((Entity)player);
            }
            else {
                if (AutoTrapOld.mc.player.getDistanceSq((Entity)player) >= distance) {
                    continue;
                }
                target = player;
                distance = AutoTrapOld.mc.player.getDistanceSq((Entity)player);
            }
        }
        return target;
    }

    private void placeBlock(final BlockPos pos) {
        if (this.placements < this.blocksPerPlace.getValue() && AutoTrapOld.mc.player.getDistanceSq(pos) <= MathUtil.square(5.0)) {
            AutoTrapOld.isPlacing = true;
            final int originalSlot = AutoTrapOld.mc.player.inventory.currentItem;
            final int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
            final int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
            if (obbySlot == -1 && eChestSot == -1) {
                this.toggle();
            }
            if (this.smartRotate) {
                AutoTrapOld.mc.player.inventory.currentItem = ((obbySlot == -1) ? eChestSot : obbySlot);
                AutoTrapOld.mc.playerController.updateController();
                this.isSneaking = BlockUtil.placeBlockSmartRotate(pos, EnumHand.MAIN_HAND, true, true, this.isSneaking);
                AutoTrapOld.mc.player.inventory.currentItem = originalSlot;
                AutoTrapOld.mc.playerController.updateController();
            }
            else {
                AutoTrapOld.mc.player.inventory.currentItem = ((obbySlot == -1) ? eChestSot : obbySlot);
                AutoTrapOld.mc.playerController.updateController();
                this.isSneaking = BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), true, this.isSneaking);
                AutoTrapOld.mc.player.inventory.currentItem = originalSlot;
                AutoTrapOld.mc.playerController.updateController();
            }
            this.didPlace = true;
            ++this.placements;
        }
    }

    static {
        AutoTrapOld.isPlacing = false;
    }
}