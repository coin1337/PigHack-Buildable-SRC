package me.earth.phobos.mixin.mixins;

import me.earth.phobos.event.events.PushEvent;
import me.earth.phobos.features.modules.soon.BetterPortals;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={Entity.class})
public abstract class MixinEntity {
    public MixinEntity(World worldIn) {
    }

    @Shadow
    public abstract int getMaxInPortalTime();

    @Redirect(method={"onEntityUpdate"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;getMaxInPortalTime()I"))
    private int getMaxInPortalTimeHook(Entity entity) {
        int time = this.getMaxInPortalTime();
        if (BetterPortals.getInstance().isOn() && BetterPortals.getInstance().fastPortal.getValue().booleanValue()) {
            time = BetterPortals.getInstance().time.getValue();
        }
        return time;
    }

    @Redirect(method={"applyEntityCollision"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void addVelocityHook(Entity entity, double x, double y, double z) {
        PushEvent event = new PushEvent(entity, x, y, z, true);
        MinecraftForge.EVENT_BUS.post((Event)event);
        if (!event.isCanceled()) {
            entity.motionX += event.x;
            entity.motionY += event.y;
            entity.motionZ += event.z;
            entity.isAirBorne = event.airbone;
        }
    }
}

