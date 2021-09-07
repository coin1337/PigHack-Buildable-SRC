package me.earth.phobos;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.earth.phobos.features.modules.misc.RPC;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;

public class DiscordRP {
    private static final DiscordRPC rpc;
    public static DiscordRichPresence presence;
    private static Thread thread;

    static {
        rpc = DiscordRPC.INSTANCE;
        presence = new DiscordRichPresence();
    }

    int waitCounter;
    Setting delay;


    public static void start() {
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        rpc.Discord_Initialize("785625955330162689", handlers, true, "");
        DiscordRP.presence.startTimestamp = System.currentTimeMillis() / 1000L;
        DiscordRP.presence.details = Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu ? "in da main menu." : "playin' " + (Minecraft.getMinecraft().currentServerData != null ? (RPC.INSTANCE.showIP.getValue().booleanValue() ? "on " + Minecraft.getMinecraft().currentServerData.serverIP + "." : " multiplayer.") : " singleplayer.");
        DiscordRP.presence.state = RPC.INSTANCE.state.getValue();
        DiscordRP.presence.largeImageKey = "pigteam2";
        DiscordRP.presence.largeImageText = "PigHack " + PigHack.MODVER;
        DiscordRP.presence.partyId = "ae488379-351d-4a4f-ad32-2b9b01c91657";
        DiscordRP.presence.joinSecret = "MTI4NzM0OjFpMmhuZToxMjMxMjM= ";

        rpc.Discord_UpdatePresence(presence);
        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                rpc.Discord_RunCallbacks();
                DiscordRP.presence.details = "owning " + (Minecraft.getMinecraft().currentServerData != null ? (RPC.INSTANCE.showIP.getValue().booleanValue() ? "on " + Minecraft.getMinecraft().currentServerData.serverIP + "." : " multiplayer.") : " singleplayer.");
                DiscordRP.presence.state = RPC.INSTANCE.state.getValue();
                rpc.Discord_UpdatePresence(presence);
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException interruptedException) {
                }
            }
        }, "RPC-Callback-Handler");
        thread.start();
    }
   

    public static void stop() {
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        rpc.Discord_Shutdown();
    }
}

