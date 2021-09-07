package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.PushEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity extends Module {
    private static Velocity INSTANCE = new Velocity();
    public Setting<Boolean> noPush = register(new Setting("NoPush", Boolean.valueOf(true)));
    public Setting<Float> horizontal = register(new Setting("Horizontal", Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(100.0F)));
    public Setting<Float> vertical = register(new Setting("Vertical", Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(100.0F)));
    public Setting<Boolean> explosions = register(new Setting("Explosions", Boolean.valueOf(true)));
    public Setting<Boolean> bobbers = register(new Setting("Bobbers", Boolean.valueOf(true)));
    public Setting<Boolean> water = register(new Setting("Water", Boolean.valueOf(false)));
    public Setting<Boolean> blocks = register(new Setting("Blocks", Boolean.valueOf(false)));
    public Setting<Boolean> ice = register(new Setting("Ice", Boolean.valueOf(false)));

    public Velocity() {
        super("Velocity", "Allows you to control your velocity", Module.Category.MOVEMENT, true, false, false);
        setInstance();
    }

    public static Velocity getINSTANCE() {
        if (INSTANCE == null)
            INSTANCE = new Velocity();
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }



    @SubscribeEvent
    public void onPacketReceived(PacketEvent.Receive event) {
        if (event.getStage() == 0 && mc.player != null) {
            if (event.getPacket() instanceof SPacketEntityVelocity) {
                SPacketEntityVelocity velocity = event.getPacket();
                if (velocity.getEntityID() == mc.player.entityId) {
                    if (this.horizontal.getValue().floatValue() == 0.0F && this.vertical.getValue().floatValue() == 0.0F) {
                        event.setCanceled(true);
                        return;
                    }
                    velocity.motionX = (int) (velocity.motionX * this.horizontal.getValue().floatValue());
                    velocity.motionY = (int) (velocity.motionY * this.vertical.getValue().floatValue());
                    velocity.motionZ = (int) (velocity.motionZ * this.horizontal.getValue().floatValue());
                }
            }
            if (event.getPacket() instanceof SPacketEntityStatus && this.bobbers.getValue().booleanValue()) {
                SPacketEntityStatus packet = event.getPacket();
                if (packet.getOpCode() == 31) {
                    Entity entity = packet.getEntity(mc.world);
                    if (entity instanceof EntityFishHook) {
                        EntityFishHook fishHook = (EntityFishHook) entity;
                        if (fishHook.caughtEntity == mc.player)
                            event.setCanceled(true);
                    }
                }
            }
            if (this.explosions.getValue().booleanValue() && event.getPacket() instanceof SPacketExplosion) {
                SPacketExplosion velocity = event.getPacket();
                velocity.motionX *= this.horizontal.getValue().floatValue();
                velocity.motionY *= this.vertical.getValue().floatValue();
                velocity.motionZ *= this.horizontal.getValue().floatValue();
            }
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        if (event.getStage() == 0 && this.noPush.getValue().booleanValue() && event.entity.equals(mc.player)) {
            if (this.horizontal.getValue().floatValue() == 0.0F && this.vertical.getValue().floatValue() == 0.0F) {
                event.setCanceled(true);
                return;
            }
            event.x = -event.x * this.horizontal.getValue().floatValue();
            event.y = -event.y * this.vertical.getValue().floatValue();
            event.z = -event.z * this.horizontal.getValue().floatValue();
        } else if (event.getStage() == 1 && this.blocks.getValue().booleanValue()) {
            event.setCanceled(true);
        } else if (event.getStage() == 2 && this.water.getValue().booleanValue() && mc.player != null && mc.player.equals(event.entity)) {
            event.setCanceled(true);
        }
    }
}