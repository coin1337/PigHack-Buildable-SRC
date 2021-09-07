package me.earth.phobos.features.modules.pvptools;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.ProcessRightClickBlockEvent;
import me.earth.phobos.features.Feature;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.BlockObsidian;
import net.minecraft.item.Item;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemSword;
import java.util.function.ToIntFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import org.lwjgl.input.Mouse;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

public class OffhandOld extends Module
{
    public Setting<Boolean> crystal;
    public Setting<Float> crystalHealth;
    public Setting<Float> crystalHoleHealth;
    public Setting<Boolean> gapple;
    public Setting<Boolean> armorCheck;
    public Setting<Integer> actions;
    public Mode2 currentMode;
    public int totems;
    public int crystals;
    public int gapples;
    public int lastTotemSlot;
    public int lastGappleSlot;
    public int lastCrystalSlot;
    public int lastObbySlot;
    public int lastWebSlot;
    public boolean holdingCrystal;
    public boolean holdingTotem;
    public boolean holdingGapple;
    public boolean didSwitchThisTick;
    private final Queue<InventoryUtil.Task> taskList;
    private static OffhandOld instance;
    private Timer timer;
    private Timer secondTimer;
    private boolean second;
    private boolean switchedForHealthReason;

    public OffhandOld() {
        super("Offhand", "Allows you to switch up your Offhand.", Category.PVP, true, false, false);
        this.crystal = (Setting<Boolean>)this.register(new Setting("Crystal", true));
        this.crystalHealth = (Setting<Float>)this.register(new Setting("CrystalHP", 13.0f, 0.1f, 36.0f));
        this.crystalHoleHealth = (Setting<Float>)this.register(new Setting("CrystalHoleHP", 3.5f, 0.1f, 36.0f));
        this.gapple = (Setting<Boolean>)this.register(new Setting("Gapple", true));
        this.armorCheck = (Setting<Boolean>)this.register(new Setting("ArmorCheck", true));
        this.actions = (Setting<Integer>)this.register(new Setting("Packets", 4, 1, 4));
        this.currentMode = Mode2.TOTEMS;
        this.totems = 0;
        this.crystals = 0;
        this.gapples = 0;
        this.lastTotemSlot = -1;
        this.lastGappleSlot = -1;
        this.lastCrystalSlot = -1;
        this.lastObbySlot = -1;
        this.lastWebSlot = -1;
        this.holdingCrystal = false;
        this.holdingTotem = false;
        this.holdingGapple = false;
        this.didSwitchThisTick = false;
        this.taskList = new ConcurrentLinkedQueue<InventoryUtil.Task>();
        this.timer = new Timer();
        this.secondTimer = new Timer();
        this.second = false;
        this.switchedForHealthReason = false;
        OffhandOld.instance = this;
    }

    public static OffhandOld getInstance() {
        if (OffhandOld.instance == null) {
            OffhandOld.instance = new OffhandOld();
        }
        return OffhandOld.instance;
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(final ProcessRightClickBlockEvent event) {
        if (event.hand == EnumHand.MAIN_HAND && event.stack.getItem() == Items.END_CRYSTAL && OffhandOld.mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && OffhandOld.mc.objectMouseOver != null && event.pos == OffhandOld.mc.objectMouseOver.getBlockPos()) {
            event.setCanceled(true);
            OffhandOld.mc.player.setActiveHand(EnumHand.OFF_HAND);
            OffhandOld.mc.playerController.processRightClick((EntityPlayer)OffhandOld.mc.player, (World)OffhandOld.mc.world, EnumHand.OFF_HAND);
        }
    }

    @Override
    public void onUpdate() {
        if (this.timer.passedMs(50L)) {
            if (OffhandOld.mc.player != null && OffhandOld.mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && OffhandOld.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && Mouse.isButtonDown(1)) {
                OffhandOld.mc.player.setActiveHand(EnumHand.OFF_HAND);
                OffhandOld.mc.gameSettings.keyBindUseItem.pressed = Mouse.isButtonDown(1);
            }
        }
        else if (OffhandOld.mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && OffhandOld.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
            OffhandOld.mc.gameSettings.keyBindUseItem.pressed = false;
        }
        if (nullCheck()) {
            return;
        }
        this.doOffhand();
        if (this.secondTimer.passedMs(50L) && this.second) {
            this.second = false;
            this.timer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        if (!Feature.fullNullCheck() && OffhandOld.mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && OffhandOld.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && OffhandOld.mc.gameSettings.keyBindUseItem.isKeyDown()) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                final CPacketPlayerTryUseItemOnBlock packet = event.getPacket();
                if (packet.getHand() == EnumHand.MAIN_HAND) {
                    if (this.timer.passedMs(50L)) {
                        OffhandOld.mc.player.setActiveHand(EnumHand.OFF_HAND);
                        OffhandOld.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
                    }
                    event.setCanceled(true);
                }
            }
            else if (event.getPacket() instanceof CPacketPlayerTryUseItem) {
                final CPacketPlayerTryUseItem packet2 = event.getPacket();
                if (packet2.getHand() == EnumHand.OFF_HAND && !this.timer.passedMs(50L)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        if (OffhandOld.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            return "Crystals";
        }
        if (OffhandOld.mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            return "Totems";
        }
        if (OffhandOld.mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
            return "Gapples";
        }
        return null;
    }

    public void doOffhand() {
        this.didSwitchThisTick = false;
        this.holdingCrystal = (OffhandOld.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL);
        this.holdingTotem = (OffhandOld.mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING);
        this.holdingGapple = (OffhandOld.mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE);
        this.totems = OffhandOld.mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        if (this.holdingTotem) {
            this.totems += OffhandOld.mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        }
        this.crystals = OffhandOld.mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).mapToInt(ItemStack::getCount).sum();
        if (this.holdingCrystal) {
            this.crystals += OffhandOld.mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).mapToInt(ItemStack::getCount).sum();
        }
        this.gapples = OffhandOld.mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.GOLDEN_APPLE).mapToInt(ItemStack::getCount).sum();
        if (this.holdingGapple) {
            this.gapples += OffhandOld.mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == Items.GOLDEN_APPLE).mapToInt(ItemStack::getCount).sum();
        }
        this.doSwitch();
    }

    public void doSwitch() {
        this.currentMode = Mode2.TOTEMS;
        if (this.gapple.getValue() && OffhandOld.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && OffhandOld.mc.gameSettings.keyBindUseItem.isKeyDown()) {
            this.currentMode = Mode2.GAPPLES;
        }
        else if (this.currentMode != Mode2.CRYSTALS && this.crystal.getValue() && ((EntityUtil.isSafe((Entity)OffhandOld.mc.player) && EntityUtil.getHealth((Entity)OffhandOld.mc.player, true) > this.crystalHoleHealth.getValue()) || EntityUtil.getHealth((Entity)OffhandOld.mc.player, true) > this.crystalHealth.getValue())) {
            this.currentMode = Mode2.CRYSTALS;
        }
        if (this.currentMode == Mode2.CRYSTALS && this.crystals == 0) {
            this.setMode(Mode2.TOTEMS);
        }
        if (this.currentMode == Mode2.CRYSTALS && ((!EntityUtil.isSafe((Entity)OffhandOld.mc.player) && EntityUtil.getHealth((Entity)OffhandOld.mc.player, true) <= this.crystalHealth.getValue()) || EntityUtil.getHealth((Entity)OffhandOld.mc.player, true) <= this.crystalHoleHealth.getValue())) {
            if (this.currentMode == Mode2.CRYSTALS) {
                this.switchedForHealthReason = true;
            }
            this.setMode(Mode2.TOTEMS);
        }
        if (this.switchedForHealthReason && ((EntityUtil.isSafe((Entity)OffhandOld.mc.player) && EntityUtil.getHealth((Entity)OffhandOld.mc.player, true) > this.crystalHoleHealth.getValue()) || EntityUtil.getHealth((Entity)OffhandOld.mc.player, true) > this.crystalHealth.getValue())) {
            this.setMode(Mode2.CRYSTALS);
            this.switchedForHealthReason = false;
        }
        if (this.currentMode == Mode2.CRYSTALS && this.armorCheck.getValue() && (OffhandOld.mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.AIR || OffhandOld.mc.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Items.AIR || OffhandOld.mc.player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == Items.AIR || OffhandOld.mc.player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == Items.AIR)) {
            this.setMode(Mode2.TOTEMS);
        }
        if (OffhandOld.mc.currentScreen instanceof GuiContainer && !(OffhandOld.mc.currentScreen instanceof GuiInventory)) {
            return;
        }
        final Item currentOffhandItem = OffhandOld.mc.player.getHeldItemOffhand().getItem();
        switch (this.currentMode) {
            case TOTEMS: {
                if (this.totems > 0 && !this.holdingTotem) {
                    this.lastTotemSlot = InventoryUtil.findItemInventorySlot(Items.TOTEM_OF_UNDYING, false);
                    final int lastSlot = this.getLastSlot(currentOffhandItem, this.lastTotemSlot);
                    this.putItemInOffhand(this.lastTotemSlot, lastSlot);
                    break;
                }
                break;
            }
            case GAPPLES: {
                if (this.gapples > 0 && !this.holdingGapple) {
                    this.lastGappleSlot = InventoryUtil.findItemInventorySlot(Items.GOLDEN_APPLE, false);
                    final int lastSlot = this.getLastSlot(currentOffhandItem, this.lastGappleSlot);
                    this.putItemInOffhand(this.lastGappleSlot, lastSlot);
                    break;
                }
                break;
            }
            default: {
                if (this.crystals > 0 && !this.holdingCrystal) {
                    this.lastCrystalSlot = InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL, false);
                    final int lastSlot = this.getLastSlot(currentOffhandItem, this.lastCrystalSlot);
                    this.putItemInOffhand(this.lastCrystalSlot, lastSlot);
                    break;
                }
                break;
            }
        }
        for (int i = 0; i < this.actions.getValue(); ++i) {
            final InventoryUtil.Task task = this.taskList.poll();
            if (task != null) {
                task.run();
                if (task.isSwitching()) {
                    this.didSwitchThisTick = true;
                }
            }
        }
    }

    private int getLastSlot(final Item item, final int slotIn) {
        if (item == Items.END_CRYSTAL) {
            return this.lastCrystalSlot;
        }
        if (item == Items.GOLDEN_APPLE) {
            return this.lastGappleSlot;
        }
        if (item == Items.TOTEM_OF_UNDYING) {
            return this.lastTotemSlot;
        }
        if (InventoryUtil.isBlock(item, BlockObsidian.class)) {
            return this.lastObbySlot;
        }
        if (InventoryUtil.isBlock(item, BlockWeb.class)) {
            return this.lastWebSlot;
        }
        if (item == Items.AIR) {
            return -1;
        }
        return slotIn;
    }

    private void putItemInOffhand(final int slotIn, final int slotOut) {
        if (slotIn != -1 && this.taskList.isEmpty()) {
            this.taskList.add(new InventoryUtil.Task(slotIn));
            this.taskList.add(new InventoryUtil.Task(45));
            this.taskList.add(new InventoryUtil.Task(slotOut));
            this.taskList.add(new InventoryUtil.Task());
        }
    }

    public void setMode(final Mode2 mode) {
        if (this.currentMode == mode) {
            this.currentMode = Mode2.TOTEMS;
        }
        else {
            this.currentMode = mode;
        }
    }

    public enum Mode2
    {
        TOTEMS,
        GAPPLES,
        CRYSTALS;
    }
}