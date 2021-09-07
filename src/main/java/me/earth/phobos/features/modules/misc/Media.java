package me.earth.phobos.features.modules.misc;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.soon.ServerModule;
import me.earth.phobos.features.setting.Setting;

public class Media
        extends Module {
    private static Media instance;
    public final Setting<Boolean> changeOwn = this.register(new Setting<Boolean>("MyName", true));
    public final Setting<String> ownName = this.register(new Setting<Object>("Name", "Pig", v -> this.changeOwn.getValue()));

    public Media() {
        super("NameChanger", "Helps with creating Media", Module.Category.MISC, false, false, false);
        instance = this;
    }

    public static Media getInstance() {
        if (instance == null) {
            instance = new Media();
        }
        return instance;
    }

    public static String getPlayerName() {
        if (Media.fullNullCheck() || !ServerModule.getInstance().isConnected()) {
            return mc.getSession().getUsername();
        }
        String name = ServerModule.getInstance().getPlayerName();
        if (name == null || name.isEmpty()) {
            return mc.getSession().getUsername();
        }
        return name;
    }
}

