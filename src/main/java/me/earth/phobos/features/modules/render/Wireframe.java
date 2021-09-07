package me.earth.phobos.features.modules.render;

import me.earth.phobos.PigHack;
import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;

public class Wireframe extends Module
{
    public Setting<RenderMode> mode;
    public Setting<RenderMode> cMode;
    public Setting<Boolean> players;
    public Setting<Boolean> playerModel;
    public Setting<Boolean> crystals;
    public Setting<Boolean> crystalModel;
    public final Setting<Float> palpha;
    public final Setting<Float> cAlpha;
    public final Setting<Float> lineWidth;
    public final Setting<Float> crystalLineWidth;
    private static Wireframe INSTANCE;
    public Setting<Integer> red;
    public Setting<Integer> green;
    public Setting<Integer> blue;
    public Setting<Integer> hoverAlpha;
    public Setting<Integer> topRed;
    public Setting<Integer> topGreen;
    public Setting<Integer> topBlue;
    public Setting<Integer> alpha;
    public Setting<Boolean> rainbow;
    public Setting<Boolean> cose;
    public Setting<Wireframe.rainbowMode> rainbowModeHud;
    public Setting<Wireframe.rainbowModeArray> rainbowModeA;
    public Setting<Integer> rainbowHue;
    public Setting<Float> rainbowBrightness;
    public Setting<Float> rainbowSaturation;

    public Wireframe() {
        super("Wireframe", "Draws a wireframe esp around other players.", Category.RENDER, false, false, false);
        this.mode = (Setting<RenderMode>)this.register(new Setting("PMode", RenderMode.SOLID));
        this.cMode = (Setting<RenderMode>)this.register(new Setting("CMode", RenderMode.SOLID));
        this.players = (Setting<Boolean>)this.register(new Setting("Players", Boolean.FALSE));
        this.playerModel = (Setting<Boolean>)this.register(new Setting("PlayerModel", Boolean.FALSE));
        this.crystals = (Setting<Boolean>)this.register(new Setting("Crystals", Boolean.FALSE));
        this.crystalModel = (Setting<Boolean>)this.register(new Setting("CrystalModel", Boolean.FALSE));
        this.palpha = (Setting<Float>)this.register(new Setting("PAlpha", 255.0f, 0.1f, 255.0f));
        this.cAlpha = (Setting<Float>)this.register(new Setting("CAlpha", 255.0f, 0.1f, 255.0f));
        this.lineWidth = (Setting<Float>)this.register(new Setting("PLineWidth", 1.0f, 0.1f, 3.0f));
        this.crystalLineWidth = (Setting<Float>)this.register(new Setting("CLineWidth", 1.0f, 0.1f, 3.0f));

        this.cose = (Setting<Boolean>)this.register(new Setting("Color Settings", false));

        this.red = (Setting<Integer>)this.register(new Setting("Red", 0, 0, 255, v -> this.cose.getValue()));
        this.green = (Setting<Integer>)this.register(new Setting("Green", 0, 0, 255, v -> this.cose.getValue()));
        this.blue = (Setting<Integer>)this.register(new Setting("Blue", 255, 0, 255, v -> this.cose.getValue()));
        this.hoverAlpha = (Setting<Integer>)this.register(new Setting("Alpha", 180, 0, 255, v -> this.cose.getValue()));
        this.topRed = (Setting<Integer>)this.register(new Setting("SecondRed", 0, 0, 255, v -> this.cose.getValue()));
        this.topGreen = (Setting<Integer>)this.register(new Setting("SecondGreen", 0, 0, 255, v -> this.cose.getValue()));
        this.topBlue = (Setting<Integer>)this.register(new Setting("SecondBlue", 150, 0, 255, v -> this.cose.getValue()));
        this.alpha = (Setting<Integer>)this.register(new Setting("HoverAlpha", 240, 0, 255, v -> this.cose.getValue()));
        this.rainbow = (Setting<Boolean>)this.register(new Setting("Rainbow", false, v -> this.cose.getValue()));
        this.rainbowModeHud = (Setting<Wireframe.rainbowMode>)this.register(new Setting("HRainbowMode", Wireframe.rainbowMode.Static, v -> this.cose.getValue()));
        this.rainbowModeA = (Setting<Wireframe.rainbowModeArray>)this.register(new Setting("ARainbowMode", Wireframe.rainbowModeArray.Static, v -> this.cose.getValue()));
        this.rainbowHue = (Setting<Integer>)this.register(new Setting("Delay", 240, 0, 600, v -> this.cose.getValue()));
        this.rainbowBrightness = (Setting<Float>)this.register(new Setting("Brightness ", 150.0f, 1.0f, 255.0f, v -> this.cose.getValue()));
        this.rainbowSaturation = (Setting<Float>)this.register(new Setting("Saturation", 150.0f, 1.0f, 255.0f, v -> this.cose.getValue()));

        this.setInstance();
    }

    private void setInstance() {
        Wireframe.INSTANCE = this;
    }

    @SubscribeEvent
    public void onRenderPlayerEvent(final RenderPlayerEvent.Pre event) {
        event.getEntityPlayer().hurtTime = 0;
    }

    public static Wireframe getINSTANCE() {
        if (Wireframe.INSTANCE == null) {
            Wireframe.INSTANCE = new Wireframe();
        }
        return Wireframe.INSTANCE;
    }

    static {
        Wireframe.INSTANCE = new Wireframe();
    }

    public enum RenderMode
    {
        SOLID,
        WIREFRAME;
    }

    public static Wireframe getInstance() {
        if (Wireframe.INSTANCE == null) {
            Wireframe.INSTANCE = new Wireframe();
        }
        return Wireframe.INSTANCE;
    }


    @SubscribeEvent
    public void onSettingChange(final ClientEvent event) {
        PigHack.colorManager.setColor(this.red.getPlannedValue(), this.green.getPlannedValue(), this.blue.getPlannedValue(), this.hoverAlpha.getPlannedValue());
    }


    @Override
    public void onLoad() {
        PigHack.colorManager.setColor(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.hoverAlpha.getValue());
    }


    static {
        Wireframe.INSTANCE = new Wireframe();
    }

    public enum rainbowMode
    {
        Static,
        Sideway;
    }

    public enum rainbowModeArray
    {
        Static,
        Up;
    }
}