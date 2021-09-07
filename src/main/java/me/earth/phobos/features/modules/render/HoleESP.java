package me.earth.phobos.features.modules.render;

import me.earth.phobos.PigHack;
import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.RenderUtil;
import me.earth.phobos.util.RotationUtil;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class HoleESP
        extends Module {
    private static HoleESP INSTANCE = new HoleESP();
    public Setting<Boolean> box = this.register(new Setting<Boolean>("Box", true));
    public Setting<Boolean> outline = this.register(new Setting<Boolean>("Outline", true));
    public Setting<Double> height = this.register(new Setting<Double>("Height", 0.0, -2.0, 2.0));
    public Setting<Boolean> safeColor = this.register(new Setting<Boolean>("SafeColor", false));
    public Setting<Boolean> customOutline = this.register(new Setting<Object>("CustomLine", Boolean.valueOf(false), v -> this.outline.getValue()));
    private final Setting<Integer> holes = this.register(new Setting<Integer>("Holes", 3, 1, 500));
    private final Setting<Integer> red = this.register(new Setting<Integer>("Red", 0, 0, 255));
    private final Setting<Integer> green = this.register(new Setting<Integer>("Green", 255, 0, 255));
    private final Setting<Integer> blue = this.register(new Setting<Integer>("Blue", 0, 0, 255));
    private final Setting<Integer> alpha = this.register(new Setting<Integer>("Alpha", 255, 0, 255));
    private final Setting<Integer> boxAlpha = this.register(new Setting<Object>("BoxAlpha", Integer.valueOf(125), Integer.valueOf(0), Integer.valueOf(255), v -> this.box.getValue()));
    private final Setting<Float> lineWidth = this.register(new Setting<Object>("LineWidth", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f), v -> this.outline.getValue()));
    private final Setting<Integer> safeRed = this.register(new Setting<Object>("SafeRed", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.safeColor.getValue()));
    private final Setting<Integer> safeGreen = this.register(new Setting<Object>("SafeGreen", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.safeColor.getValue()));
    private final Setting<Integer> safeBlue = this.register(new Setting<Object>("SafeBlue", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.safeColor.getValue()));
    private final Setting<Integer> safeAlpha = this.register(new Setting<Object>("SafeAlpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.safeColor.getValue()));
    private final Setting<Integer> cRed = this.register(new Setting<Object>("OL-Red", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.customOutline.getValue() != false && this.outline.getValue() != false));
    private final Setting<Integer> cGreen = this.register(new Setting<Object>("OL-Green", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.customOutline.getValue() != false && this.outline.getValue() != false));
    private final Setting<Integer> cBlue = this.register(new Setting<Object>("OL-Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.customOutline.getValue() != false && this.outline.getValue() != false));
    private final Setting<Integer> cAlpha = this.register(new Setting<Object>("OL-Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.customOutline.getValue() != false && this.outline.getValue() != false));
    private final Setting<Integer> safecRed = this.register(new Setting<Object>("OL-SafeRed", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.customOutline.getValue() != false && this.outline.getValue() != false && this.safeColor.getValue() != false));
    private final Setting<Integer> safecGreen = this.register(new Setting<Object>("OL-SafeGreen", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.customOutline.getValue() != false && this.outline.getValue() != false && this.safeColor.getValue() != false));
    private final Setting<Integer> safecBlue = this.register(new Setting<Object>("OL-SafeBlue", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.customOutline.getValue() != false && this.outline.getValue() != false && this.safeColor.getValue() != false));
    private final Setting<Integer> safecAlpha = this.register(new Setting<Object>("OL-SafeAlpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.customOutline.getValue() != false && this.outline.getValue() != false && this.safeColor.getValue() != false));

    public HoleESP() {
        super("HoleESP", "Shows safe spots.", Module.Category.RENDER, false, false, false);
        this.setInstance();
    }

    public static HoleESP getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HoleESP();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        int drawnHoles = 0;
        for (BlockPos pos : PigHack.holeManager.getSortedHoles()) {
            if (drawnHoles >= this.holes.getValue()) break;
            if (pos.equals(new BlockPos(HoleESP.mc.player.posX, HoleESP.mc.player.posY, HoleESP.mc.player.posZ)) || !RotationUtil.isInFov(pos))
                continue;
            if (this.safeColor.getValue().booleanValue() && PigHack.holeManager.isSafe(pos)) {
                RenderUtil.drawBoxESP(pos, new Color(this.safeRed.getValue(), this.safeGreen.getValue(), this.safeBlue.getValue(), this.safeAlpha.getValue()), this.customOutline.getValue(), new Color(this.safecRed.getValue(), this.safecGreen.getValue(), this.safecBlue.getValue(), this.safecAlpha.getValue()), this.lineWidth.getValue().floatValue(), this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), true, this.height.getValue());
            } else {
                RenderUtil.drawBoxESP(pos, new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()), this.customOutline.getValue(), new Color(this.cRed.getValue(), this.cGreen.getValue(), this.cBlue.getValue(), this.cAlpha.getValue()), this.lineWidth.getValue().floatValue(), this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), true, this.height.getValue());
            }
            ++drawnHoles;
        }
    }
}

