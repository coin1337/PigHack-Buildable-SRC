package me.earth.phobos.features.modules.movement;

import me.earth.phobos.PigHack;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.PlayerUtil;
import me.earth.phobos.util.Timer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class YPort extends Module {

    private Timer timer = new Timer();
    private final Setting<Double> yPortSpeed = register(new Setting("YPort Speed", 0.06, 0.01, 0.15));

    public YPort() {
        super("YPort", "ooga booga", Category.MOVEMENT, true, false, false);
    }

    public void onDisable() {
        timer.reset();
        PigHack.timerManager.reset();
    }

    @SubscribeEvent
    public void onTickEvent(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }
            handleYPortSpeed();
    }

    private void handleYPortSpeed() {
        if (!PlayerUtil.isMoving(mc.player) || mc.player.isInWater() && mc.player.isInLava() || mc.player.collidedHorizontally) {
            return;
        }

        if (mc.player.onGround) {
            PigHack.timerManager.setTimer(1.15f);
            mc.player.jump();
            PlayerUtil.setSpeed(mc.player, PlayerUtil.getBaseMoveSpeed() + yPortSpeed.getValue());
        }
        else {
            mc.player.motionY = -1;
            PigHack.timerManager.reset();
        }
    }

}
