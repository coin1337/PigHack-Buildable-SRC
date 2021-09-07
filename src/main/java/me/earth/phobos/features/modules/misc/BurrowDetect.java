package me.earth.phobos.features.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.player.MultiTask;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BurrowDetect
        extends Module {
    private static MultiTask INSTANCE = new MultiTask();

    public BurrowDetect() {
        super("BurrowDetect", "Allows you to eat while mining.", Module.Category.PVP, false, false, false);
    }
    private final List<EntityPlayer> burrowedPlayers = new ArrayList<>();

    @Override
    public void onTick() {

        for (EntityPlayer entityPlayer : mc.world.playerEntities.stream().filter(entityPlayer -> entityPlayer != mc.player).collect(Collectors.toList())) {
            if (!burrowedPlayers.contains(entityPlayer) && isInBurrow(entityPlayer)) {
                Command.sendMessage(ChatFormatting.BLUE + entityPlayer.getDisplayNameString() + ChatFormatting.GREEN + " - has burrowed [NN] ");
                burrowedPlayers.add(entityPlayer);
            }
        }
    }
    private boolean isInBurrow(EntityPlayer entityPlayer){
        BlockPos playerPos = new BlockPos(getMiddlePosition(entityPlayer.posX), entityPlayer.posY, getMiddlePosition(entityPlayer.posZ));

        return mc.world.getBlockState(playerPos).getBlock() == Blocks.OBSIDIAN
                || mc.world.getBlockState(playerPos).getBlock() == Blocks.ENDER_CHEST
                || mc.world.getBlockState(playerPos).getBlock() == Blocks.ANVIL;
    }

    private double getMiddlePosition(double positionIn){
        double positionFinal = Math.round(positionIn);

        if (Math.round(positionIn) > positionIn){
            positionFinal -= 0.5;
        }
        else if (Math.round(positionIn) <= positionIn){
            positionFinal += 0.5;
        }

        return positionFinal;
    }
}
