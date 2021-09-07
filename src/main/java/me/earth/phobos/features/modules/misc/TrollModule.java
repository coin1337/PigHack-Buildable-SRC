package me.earth.phobos.features.modules.misc;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;

public class TrollModule
        extends Module {
    private static me.earth.phobos.features.modules.player.MultiTask INSTANCE = new me.earth.phobos.features.modules.player.MultiTask();

    public TrollModule() {
        super("MacrosCommand" +
                "", "Allows you to eat while mining.", Module.Category.PVP, false, false, false);
    }

    public Setting<String> commands = this.register(new Setting<String>("Command", "kill", "Command"));

    public void onEnable(){
        TrollModule.mc.player.connection.sendPacket(new CPacketChatMessage(("/" + commands.getValue())));
        this.disable();
    }
}