// 
// Decompiled by Procyon v0.5.36
// 

package me.earth.phobos.manager;

import com.google.common.base.Strings;
import me.earth.phobos.PigHack;
import me.earth.phobos.event.events.*;
import me.earth.phobos.features.Feature;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.client.Managers;
import me.earth.phobos.util.Timer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


public class EventManager extends Feature {
    private final Timer switchTimer;
    private final Timer timer;
    private final Timer logoutTimer;
    private final AtomicBoolean tickOngoing;
    private boolean keyTimeout;

    public EventManager() {
        this.timer = new Timer();
        this.logoutTimer = new Timer();
        this.tickOngoing = new AtomicBoolean(false);
        this.switchTimer = new Timer();
    }

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onUnload() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onUpdate(final LivingEvent.LivingUpdateEvent event) {
        if (!Feature.fullNullCheck() && event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(EventManager.mc.player)) {
            PigHack.potionManager.update();
            PigHack.totemPopManager.onUpdate();
            PigHack.inventoryManager.update();
            PigHack.holeManager.update();
            PigHack.safetyManager.onUpdate();
            PigHack.moduleManager.onUpdate();
            PigHack.timerManager.update();
            if (this.timer.passedMs(Managers.getInstance().moduleListUpdates.getValue())) {
                PigHack.moduleManager.sortModules(true);
                PigHack.moduleManager.alphabeticallySortModules();
                this.timer.reset();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTickHighest(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            this.tickOngoing.set(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTickLowest(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            this.tickOngoing.set(false);
        }
    }

    public boolean ticksOngoing() {
        return this.tickOngoing.get();
    }

    @SubscribeEvent
    public void onClientConnect(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.logoutTimer.reset();
        PigHack.moduleManager.onLogin();
    }

    @SubscribeEvent
    public void onClientDisconnect(final FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        PigHack.moduleManager.onLogout();
        PigHack.totemPopManager.onLogout();
        PigHack.potionManager.onLogout();
    }

    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent event) {
        if (fullNullCheck()) {
            return;
        }
        PigHack.moduleManager.onTick();
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(final UpdateWalkingPlayerEvent event) {
        if (fullNullCheck()) {
            return;
        }
        if (event.getStage() == 0) {
            PigHack.baritoneManager.onUpdateWalkingPlayer();
            PigHack.speedManager.updateValues();
            PigHack.rotationManager.updateRotations();
            PigHack.positionManager.updatePosition();
        }
        if (event.getStage() == 1) {
            PigHack.rotationManager.restoreRotations();
            PigHack.positionManager.restorePosition();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketHeldItemChange) {
            this.switchTimer.reset();
        }
    }

    public boolean isOnSwitchCoolDown() {
        return !this.switchTimer.passedMs(500L);
    }

    @SubscribeEvent
    public void onPacketReceive(final PacketEvent.Receive event) {
        if (event.getStage() != 0) {
            return;
        }
        PigHack.serverManager.onPacketReceived();
        if (event.getPacket() instanceof SPacketEntityStatus) {
            final SPacketEntityStatus packet = event.getPacket();
            if (packet.getOpCode() == 35 && packet.getEntity(EventManager.mc.world) instanceof EntityPlayer) {
                final EntityPlayer player = (EntityPlayer) packet.getEntity(EventManager.mc.world);
                MinecraftForge.EVENT_BUS.post(new TotemPopEvent(player));
                PigHack.totemPopManager.onTotemPop(player);
                PigHack.potionManager.onTotemPop(player);
            }
        } else if (event.getPacket() instanceof SPacketPlayerListItem && !Feature.fullNullCheck() && this.logoutTimer.passedS(1.0)) {
            final SPacketPlayerListItem packet2 = event.getPacket();
            if (!SPacketPlayerListItem.Action.ADD_PLAYER.equals(packet2.getAction()) && !SPacketPlayerListItem.Action.REMOVE_PLAYER.equals(packet2.getAction())) {
                return;
            }
            packet2.getEntries().stream().filter(Objects::nonNull).filter(data -> !Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() != null).forEach(data -> {
                final UUID id;
                final SPacketPlayerListItem sPacketPlayerListItem = event.getPacket();
                final String name;
                final EntityPlayer entity;
                String logoutName;
                id = data.getProfile().getId();
                switch (sPacketPlayerListItem.getAction()) {
                    case ADD_PLAYER: {
                        name = data.getProfile().getName();
                        MinecraftForge.EVENT_BUS.post(new ConnectionEvent(0, id, name));
                        break;
                    }
                    case REMOVE_PLAYER: {
                        entity = EventManager.mc.world.getPlayerEntityByUUID(id);
                        if (entity != null) {
                            logoutName = entity.getName();
                            MinecraftForge.EVENT_BUS.post(new ConnectionEvent(1, entity, id, logoutName));
                            break;
                        } else {
                            MinecraftForge.EVENT_BUS.post(new ConnectionEvent(2, id, null));
                            break;
                        }
                    }
                }
            });
        } else if (event.getPacket() instanceof SPacketTimeUpdate) {
            PigHack.serverManager.update();
        }
    }

    @SubscribeEvent
    public void onWorldRender(final RenderWorldLastEvent event) {
        if (event.isCanceled()) {
            return;
        }
        EventManager.mc.profiler.startSection("pighack");
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1.0f);
        final Render3DEvent render3dEvent = new Render3DEvent(event.getPartialTicks());
        PigHack.moduleManager.onRender3D(render3dEvent);
        GlStateManager.glLineWidth(1.0f);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        EventManager.mc.profiler.endSection();
    }

    @SubscribeEvent
    public void renderHUD(final RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            PigHack.textManager.updateResolution();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Text event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            final ScaledResolution resolution = new ScaledResolution(EventManager.mc);
            final Render2DEvent render2DEvent = new Render2DEvent(event.getPartialTicks(), resolution);
            PigHack.moduleManager.onRender2D(render2DEvent);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatSent(final ClientChatEvent event) {
        if (event.getMessage().startsWith(Command.getCommandPrefix())) {
            event.setCanceled(true);
            try {
                EventManager.mc.ingameGUI.getChatGUI().addToSentMessages(event.getMessage());
                if (event.getMessage().length() > 1) {
                    PigHack.commandManager.executeCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
                } else {
                    Command.sendMessage("Please enter a command.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Command.sendMessage("§cAn error occurred while running this command. Check the log!");
            }
            event.setMessage("");
        }
    }
}