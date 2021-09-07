package me.earth.phobos.features.modules.misc;

import me.earth.phobos.DiscordRP;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;

public class RPC
        extends Module {
    public static RPC INSTANCE;
    public Setting<Boolean> showIP = this.register(new Setting<Boolean>("ShowIP", Boolean.valueOf(true), "Shows the server IP in your discord presence."));
    public Setting<String> state = this.register(new Setting<String>("State", "Stop!", "Sets the state of the DiscordRPC."));

    public RPC() {
        super("RPC", "Discord rich presence", Module.Category.MISC, false, false, false);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        DiscordRP.start();
    }

    @Override
    public void onDisable() {
        DiscordRP.stop();
    }
}

