package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.EntityUtil;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ReverseStep extends Module {
    private final double[] oneblockPositions = new double[]{0.42, 0.75};
    private int packets;
    private boolean jumped = false;

    public ReverseStep() {
        super("ReverseStep", "Teleports you in a hole.", Module.Category.MOVEMENT, true, false, false);
    }


    @Override
    public void onUpdate() {
        if (fullNullCheck() || mc.player.isInLava() || mc.player.isInWater() || mc.player.isJumping) return;
        if (mc.player.onGround) {
            --mc.player.motionY;
        }
    }
}