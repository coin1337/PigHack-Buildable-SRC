package me.earth.phobos.features.modules.player;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.PushEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.MathUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Freecam extends Module {
    private static Freecam INSTANCE = new Freecam();
    public Setting<Double> speed = register(new Setting("Speed", Double.valueOf(0.5D), Double.valueOf(0.1D), Double.valueOf(5.0D)));
    public Setting<Boolean> view = register(new Setting("3D", Boolean.valueOf(false)));
    public Setting<Boolean> packet = register(new Setting("Packet", Boolean.valueOf(true)));
    public Setting<Boolean> disable = register(new Setting("Logout/Off", Boolean.valueOf(true)));
    private AxisAlignedBB oldBoundingBox;

    private EntityOtherPlayerMP entity;

    private Vec3d position;

    private Entity riding;

    private float yaw;

    private float pitch;

    public Freecam() {
        super("Freecam", "Look around freely.", Module.Category.PLAYER, true, false, false);
        setInstance();
    }

    public static Freecam getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Freecam();
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public void onEnable() {
        if (!fullNullCheck()) {
            this.oldBoundingBox = mc.player.getEntityBoundingBox();
            mc.player.setEntityBoundingBox(new AxisAlignedBB(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.posX, mc.player.posY, mc.player.posZ));
            if (mc.player.getRidingEntity() != null) {
                this.riding = mc.player.getRidingEntity();
                mc.player.dismountRidingEntity();
            }
            this.entity = new EntityOtherPlayerMP(mc.world, mc.session.getProfile());
            this.entity.copyLocationAndAnglesFrom(mc.player);
            this.entity.rotationYaw = mc.player.rotationYaw;
            this.entity.rotationYawHead = mc.player.rotationYawHead;
            this.entity.inventory.copyInventory(mc.player.inventory);
            mc.world.addEntityToWorld(69420, this.entity);
            this.position = mc.player.getPositionVector();
            this.yaw = mc.player.rotationYaw;
            this.pitch = mc.player.rotationPitch;
            mc.player.noClip = true;
        }
    }

    public void onDisable() {
        if (!fullNullCheck()) {
            mc.player.setEntityBoundingBox(this.oldBoundingBox);
            if (this.riding != null)
                mc.player.startRiding(this.riding, true);
            if (this.entity != null)
                mc.world.removeEntity(this.entity);
            if (this.position != null)
                mc.player.setPosition(this.position.x, this.position.y, this.position.z);
            mc.player.rotationYaw = this.yaw;
            mc.player.rotationPitch = this.pitch;
            mc.player.noClip = false;
        }
    }

    public void onUpdate() {
        mc.player.noClip = true;
        mc.player.setVelocity(0.0D, 0.0D, 0.0D);
        mc.player.jumpMovementFactor = this.speed.getValue().floatValue();
        double[] dir = MathUtil.directionSpeed(this.speed.getValue().doubleValue());
        if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
            mc.player.motionX = dir[0];
            mc.player.motionZ = dir[1];
        } else {
            mc.player.motionX = 0.0D;
            mc.player.motionZ = 0.0D;
        }
        mc.player.setSprinting(false);
        if (this.view.getValue().booleanValue() && !mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown())
            mc.player.motionY = this.speed.getValue().doubleValue() * -MathUtil.degToRad(mc.player.rotationPitch) * mc.player.movementInput.moveForward;
        if (mc.gameSettings.keyBindJump.isKeyDown())
            mc.player.motionY += this.speed.getValue().doubleValue();
        if (mc.gameSettings.keyBindSneak.isKeyDown())
            mc.player.motionY -= this.speed.getValue().doubleValue();
    }

    public void onLogout() {
        if (this.disable.getValue().booleanValue())
            disable();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (this.packet.getValue().booleanValue()) {
            if (event.getPacket() instanceof net.minecraft.network.play.client.CPacketPlayer)
                event.setCanceled(true);
        } else if (!(event.getPacket() instanceof net.minecraft.network.play.client.CPacketUseEntity) && !(event.getPacket() instanceof net.minecraft.network.play.client.CPacketPlayerTryUseItem) && !(event.getPacket() instanceof net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock) && !(event.getPacket() instanceof net.minecraft.network.play.client.CPacketPlayer) && !(event.getPacket() instanceof net.minecraft.network.play.client.CPacketVehicleMove) && !(event.getPacket() instanceof net.minecraft.network.play.client.CPacketChatMessage) && !(event.getPacket() instanceof net.minecraft.network.play.client.CPacketKeepAlive)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSetPassengers) {
            SPacketSetPassengers packet = event.getPacket();
            Entity riding = mc.world.getEntityByID(packet.getEntityId());
            if (riding != null && riding == this.riding)
                this.riding = null;
        }
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = event.getPacket();
            if (this.packet.getValue().booleanValue()) {
                if (this.entity != null)
                    this.entity.setPositionAndRotation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
                this.position = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
                event.setCanceled(true);
            } else {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        if (event.getStage() == 1)
            event.setCanceled(true);
    }
}