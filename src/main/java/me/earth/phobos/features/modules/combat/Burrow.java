package me.earth.phobos.features.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.movement.LongJump;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Burrow extends Module
{
    private final Setting<Boolean> startjump;
    private final Setting<Boolean> jump;
    private final Setting<Boolean> jumpmo;

    public Burrow() {   
        super("Burrow", "SelfFills yourself in a hole.", Category.COMBAT, true, false, true);
        this.jump = (Setting<Boolean>)this.register(new Setting("jump", Boolean.FALSE));
        this.startjump = (Setting<Boolean>)this.register(new Setting("startjump", Boolean.FALSE));
        this.jumpmo = (Setting<Boolean>)this.register(new Setting("motion", Boolean.FALSE));

    }
    public final Setting<Float> ticks = this.register(new Setting<Float>("Tick", 0f, 0f, 12f));
    private BlockPos playerPos;

    @Override
    public void onUpdate() {
        if (Burrow.mc.player.onGround && this.jumpmo.getValue()) {
            Burrow.mc.player.motionY = 1.5f;
        }
        if (Burrow.mc.player.onGround && this.startjump.getValue()) {
            Burrow.mc.player.jump();
            Burrow.mc.player.jump();
        }
        if (Burrow.mc.player == null) {
            return;
        }
        Burrow.mc.player.connection.sendPacket(new CPacketPlayer.Rotation());
        Vec3d vec3d = EntityUtil.getInterpolatedPos((Entity)Burrow.mc.player, this.ticks.getValue());
        BlockPos blockPos = new BlockPos(vec3d).down();
        BlockPos belowBlockPos = blockPos.down();
        if (Burrow.mc.player.onGround && this.jump.getValue()) {
            Burrow.mc.player.jump();
            Burrow.mc.player.jump();
        }
        if (!Wrapper.getWorld().getBlockState(blockPos).getMaterial().isReplaceable()) {
            return;
        }
        int newSlot = -1;
        for (int i = 0; i < 9; ++i) {
            Block block;
            ItemStack stack = Wrapper.getPlayer().inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock) || BlockInteraction.blackList.contains((Object)(block = ((ItemBlock)stack.getItem()).getBlock())) || block instanceof BlockContainer || !Block.getBlockFromItem((Item)stack.getItem()).getDefaultState().isFullBlock() || ((ItemBlock)stack.getItem()).getBlock() instanceof BlockFalling && Wrapper.getWorld().getBlockState(belowBlockPos).getMaterial().isReplaceable()) continue;
            newSlot = i;
            break;
        }
        if (newSlot == -1) {
            return;
        }
        int oldSlot = Wrapper.getPlayer().inventory.currentItem;
        Wrapper.getPlayer().inventory.currentItem = newSlot;
        if (!BlockInteraction.checkForNeighbours(blockPos)) {
            return;
        }
        BlockInteraction.placeBlockScaffold(blockPos);
        Wrapper.getPlayer().inventory.currentItem = oldSlot;
        Command.sendMessage("" + (Object)ChatFormatting.GRAY + "> " + (Object)ChatFormatting.RESET + "finished glitching, disabling.");
        this.disable();
    }

}