package me.earth.phobos.features.modules.render;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class NoRender extends Module {
    private static NoRender INSTANCE = new NoRender();
    public Setting<Boolean> fire = register(new Setting("Fire", Boolean.valueOf(false), "Removes the portal overlay."));
    public Setting<Boolean> portal = register(new Setting("Portal", Boolean.valueOf(false), "Removes the portal overlay."));
    public Setting<Boolean> pumpkin = register(new Setting("Pumpkin", Boolean.valueOf(false), "Removes the pumpkin overlay."));
    public Setting<Boolean> totemPops = register(new Setting("TotemPop", Boolean.valueOf(false), "Removes the Totem overlay."));
    public Setting<Boolean> items = register(new Setting("Items", Boolean.valueOf(false), "Removes items on the ground."));
    public Setting<Boolean> nausea = register(new Setting("Nausea", Boolean.valueOf(false), "Removes Portal Nausea."));
    public Setting<Boolean> hurtcam = register(new Setting("HurtCam", Boolean.valueOf(false), "Removes shaking after taking damage."));
    public Setting<Fog> fog = register(new Setting("Fog", Fog.NONE, "Removes Fog."));
    public Setting<Boolean> noWeather = register(new Setting("Weather", Boolean.valueOf(false), "AntiWeather"));
    public Setting<Boss> boss = register(new Setting("BossBars", Boss.NONE, "Modifies the bossbars."));
    public Setting<Float> scale = register(new Setting("Scale", Float.valueOf(0.0F), Float.valueOf(0.5F), Float.valueOf(1.0F), v -> (this.boss.getValue() == Boss.MINIMIZE || this.boss.getValue() != Boss.STACK), "Scale of the bars."));
    public Setting<Boolean> bats = register(new Setting("Bats", Boolean.valueOf(false), "Removes bats."));
    public Setting<NoArmor> noArmor = register(new Setting("NoArmor", NoArmor.NONE, "Doesnt Render Armor on players."));
    public Setting<Skylight> skylight = register(new Setting("Skylight", Skylight.NONE));
    public Setting<Boolean> barriers = register(new Setting("Barriers", Boolean.valueOf(false), "Barriers"));
    public Setting<Boolean> blocks = register(new Setting("Blocks", Boolean.valueOf(false), "Blocks"));
    public Setting<Boolean> advancements = register(new Setting("Advancements", Boolean.valueOf(false)));

    public NoRender() {
        super("NoRender", "Allows you to stop rendering stuff", Module.Category.RENDER, true, false, false);
        setInstance();
    }

    public static NoRender getInstance() {
        if (INSTANCE == null)
            INSTANCE = new NoRender();
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public void onUpdate() {
        if (this.items.getValue().booleanValue())
            mc.world.loadedEntityList.stream().filter(EntityItem.class::isInstance).map(EntityItem.class::cast).forEach(Entity::setDead);
        if (this.noWeather.getValue().booleanValue() && mc.world.isRaining())
            mc.world.setRainStrength(0.0F);
    }

    public void doVoidFogParticles(int posX, int posY, int posZ) {
        int i = 32;
        Random random = new Random();
        ItemStack itemstack = mc.player.getHeldItemMainhand();
        boolean flag = (!this.barriers.getValue().booleanValue() || (mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER)));
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        for (int j = 0; j < 667; j++) {
            showBarrierParticles(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
            showBarrierParticles(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
        }
    }

    public void showBarrierParticles(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos) {
        int i = x + mc.world.rand.nextInt(offset) - mc.world.rand.nextInt(offset);
        int j = y + mc.world.rand.nextInt(offset) - mc.world.rand.nextInt(offset);
        int k = z + mc.world.rand.nextInt(offset) - mc.world.rand.nextInt(offset);
        pos.setPos(i, j, k);
        IBlockState iblockstate = mc.world.getBlockState(pos);
        iblockstate.getBlock().randomDisplayTick(iblockstate, mc.world, pos, random);
        if (!holdingBarrier && iblockstate.getBlock() == Blocks.BARRIER)
            mc.world.spawnParticle(EnumParticleTypes.BARRIER, (i + 0.5F), (j + 0.5F), (k + 0.5F), 0.0D, 0.0D, 0.0D);
    }

    @SubscribeEvent
    public void onRenderPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && this.boss.getValue() != Boss.NONE)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderPost(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && this.boss.getValue() != Boss.NONE)
            if (this.boss.getValue() == Boss.MINIMIZE) {
                Map<UUID, BossInfoClient> map = (mc.ingameGUI.getBossOverlay()).mapBossInfos;
                if (map == null)
                    return;
                ScaledResolution scaledresolution = new ScaledResolution(mc);
                int i = scaledresolution.getScaledWidth();
                int j = 12;
                for (Map.Entry<UUID, BossInfoClient> entry : map.entrySet()) {
                    BossInfoClient info = entry.getValue();
                    String text = info.getName().getFormattedText();
                    int k = (int) (i / this.scale.getValue().floatValue() / 2.0F - 91.0F);
                    GL11.glScaled(this.scale.getValue().floatValue(), this.scale.getValue().floatValue(), 1.0D);
                    if (!event.isCanceled()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        mc.getTextureManager().bindTexture(GuiBossOverlay.GUI_BARS_TEXTURES);
                        mc.ingameGUI.getBossOverlay().render(k, j, info);
                        mc.fontRenderer.drawStringWithShadow(text, i / this.scale.getValue().floatValue() / 2.0F - (mc.fontRenderer.getStringWidth(text) / 2), (j - 9), 16777215);
                    }
                    GL11.glScaled(1.0D / this.scale.getValue().floatValue(), 1.0D / this.scale.getValue().floatValue(), 1.0D);
                    j += 10 + mc.fontRenderer.FONT_HEIGHT;
                }
            } else if (this.boss.getValue() == Boss.STACK) {
                Map<UUID, BossInfoClient> map = (mc.ingameGUI.getBossOverlay()).mapBossInfos;
                HashMap<String, Pair<BossInfoClient, Integer>> to = new HashMap<>();
                for (Map.Entry<UUID, BossInfoClient> entry : map.entrySet()) {
                    String s = entry.getValue().getName().getFormattedText();
                    if (to.containsKey(s)) {
                        Pair<BossInfoClient, Integer> pair = to.get(s);
                        pair = new Pair<>(pair.getKey(), Integer.valueOf(pair.getValue().intValue() + 1));
                        to.put(s, pair);
                        continue;
                    }
                    Pair<BossInfoClient, Integer> p = new Pair<>(entry.getValue(), Integer.valueOf(1));
                    to.put(s, p);
                }
                ScaledResolution scaledresolution = new ScaledResolution(mc);
                int i = scaledresolution.getScaledWidth();
                int j = 12;
                for (Map.Entry<String, Pair<BossInfoClient, Integer>> entry : to.entrySet()) {
                    String text = entry.getKey();
                    BossInfoClient info = (BossInfoClient) ((Pair) entry.getValue()).getKey();
                    int a = ((Integer) ((Pair) entry.getValue()).getValue()).intValue();
                    text = text + " x" + a;
                    int k = (int) (i / this.scale.getValue().floatValue() / 2.0F - 91.0F);
                    GL11.glScaled(this.scale.getValue().floatValue(), this.scale.getValue().floatValue(), 1.0D);
                    if (!event.isCanceled()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        mc.getTextureManager().bindTexture(GuiBossOverlay.GUI_BARS_TEXTURES);
                        mc.ingameGUI.getBossOverlay().render(k, j, info);
                        mc.fontRenderer.drawStringWithShadow(text, i / this.scale.getValue().floatValue() / 2.0F - (mc.fontRenderer.getStringWidth(text) / 2), (j - 9), 16777215);
                    }
                    GL11.glScaled(1.0D / this.scale.getValue().floatValue(), 1.0D / this.scale.getValue().floatValue(), 1.0D);
                    j += 10 + mc.fontRenderer.FONT_HEIGHT;
                }
            }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre<?> event) {
        if (this.bats.getValue().booleanValue() && event.getEntity() instanceof net.minecraft.entity.passive.EntityBat)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundAtEntityEvent event) {
        if ((this.bats.getValue().booleanValue() && event.getSound().equals(SoundEvents.ENTITY_BAT_AMBIENT)) || event
                .getSound().equals(SoundEvents.ENTITY_BAT_DEATH) || event
                .getSound().equals(SoundEvents.ENTITY_BAT_HURT) || event
                .getSound().equals(SoundEvents.ENTITY_BAT_LOOP) || event
                .getSound().equals(SoundEvents.ENTITY_BAT_TAKEOFF)) {
            event.setVolume(0.0F);
            event.setPitch(0.0F);
            event.setCanceled(true);
        }
    }

    public enum Skylight {
        NONE, WORLD, ENTITY, ALL
    }

    public enum Fog {
        NONE, AIR, NOFOG
    }

    public enum Boss {
        NONE, REMOVE, STACK, MINIMIZE
    }

    public enum NoArmor {
        NONE, ALL, HELMET
    }

    public static class Pair<T, S> {
        private T key;

        private S value;

        public Pair(T key, S value) {
            this.key = key;
            this.value = value;
        }

        public T getKey() {
            return this.key;
        }

        public void setKey(T key) {
            this.key = key;
        }

        public S getValue() {
            return this.value;
        }

        public void setValue(S value) {
            this.value = value;
        }
    }
}