package me.earth.phobos.event.events;

import me.earth.phobos.event.EventStage;
import net.minecraft.util.math.BlockPos;

public class DestroyBlockEvent extends EventStage {

    BlockPos blockPos;

    public DestroyBlockEvent(BlockPos blockPos) {
        super();
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }
}