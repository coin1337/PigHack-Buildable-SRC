package me.earth.phobos.features.modules.pvptools;

import me.earth.phobos.PigHack;
import me.earth.phobos.features.Feature;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.InventoryUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import java.util.HashMap;
import java.util.Iterator;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.inventory.GuiContainer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.Queue;

public class AutoArmorOld extends Module
{
    private final Setting<Integer> delay;
    private final Setting<Boolean> curse;
    private final Setting<Boolean> mendingTakeOff;
    private final Setting<Integer> closestEnemy;
    private final Setting<Integer> repair;
    private final Setting<Integer> actions;
    private final Timer timer;
    private final Queue<InventoryUtil.Task> taskList;
    private final List<Integer> doneSlots;
    boolean flag;

    public AutoArmorOld() {
        super("AutoArmor", "Puts Armor on for you.", Category.PVP, true, false, false);
        this.delay = (Setting<Integer>)this.register(new Setting("Delay", 50, 0, 500));
        this.curse = (Setting<Boolean>)this.register(new Setting("Vanishing", false));
        this.mendingTakeOff = (Setting<Boolean>)this.register(new Setting("AutoMend", false));
        this.closestEnemy = (Setting<Integer>)this.register(new Setting("Enemy", 8, 1, 20, v -> this.mendingTakeOff.getValue()));
        this.repair = (Setting<Integer>)this.register(new Setting("Repair%", 80, 1, 100, v -> this.mendingTakeOff.getValue()));
        this.actions = (Setting<Integer>)this.register(new Setting("Packets", 3, 1, 12));
        this.timer = new Timer();
        this.taskList = new ConcurrentLinkedQueue<InventoryUtil.Task>();
        this.doneSlots = new ArrayList<Integer>();
    }

    @Override
    public void onLogin() {
        this.timer.reset();
    }

    @Override
    public void onDisable() {
        this.taskList.clear();
        this.doneSlots.clear();
        this.flag = false;
    }

    @Override
    public void onLogout() {
        this.taskList.clear();
        this.doneSlots.clear();
    }

    @Override
    public void onTick() {
        if (Feature.fullNullCheck() || (AutoArmorOld.mc.currentScreen instanceof GuiContainer && !(AutoArmorOld.mc.currentScreen instanceof GuiInventory))) {
            return;
        }
        if (this.taskList.isEmpty()) {
            if (this.mendingTakeOff.getValue() && InventoryUtil.holdingItem(ItemExpBottle.class) && AutoArmorOld.mc.gameSettings.keyBindUseItem.isKeyDown() && AutoArmorOld.mc.world.playerEntities.stream().noneMatch(e -> e != AutoArmorOld.mc.player && !PigHack.friendManager.isFriend(((EntityPlayer)e).getName()) && AutoArmorOld.mc.player.getDistance((Entity)e) <= this.closestEnemy.getValue()) && !this.flag) {
                int takeOff = 0;
                for (final Map.Entry<Integer, ItemStack> armorSlot : this.getArmor().entrySet()) {
                    final ItemStack stack = armorSlot.getValue();
                    final float percent = this.repair.getValue() / 100.0f;
                    final int dam = Math.round(stack.getMaxDamage() * percent);
                    final int goods = stack.getMaxDamage() - stack.getItemDamage();
                    if (dam < goods) {
                        ++takeOff;
                    }
                }
                if (takeOff == 4) {
                    this.flag = true;
                }
                if (!this.flag) {
                    final ItemStack itemStack1 = AutoArmorOld.mc.player.inventoryContainer.getSlot(5).getStack();
                    if (!itemStack1.isEmpty) {
                        final float percent2 = this.repair.getValue() / 100.0f;
                        final int dam2 = Math.round(itemStack1.getMaxDamage() * percent2);
                        final int goods2 = itemStack1.getMaxDamage() - itemStack1.getItemDamage();
                        if (dam2 < goods2) {
                            this.takeOffSlot(5);
                        }
                    }
                    final ItemStack itemStack2 = AutoArmorOld.mc.player.inventoryContainer.getSlot(6).getStack();
                    if (!itemStack2.isEmpty) {
                        final float percent3 = this.repair.getValue() / 100.0f;
                        final int dam3 = Math.round(itemStack2.getMaxDamage() * percent3);
                        final int goods3 = itemStack2.getMaxDamage() - itemStack2.getItemDamage();
                        if (dam3 < goods3) {
                            this.takeOffSlot(6);
                        }
                    }
                    final ItemStack itemStack3 = AutoArmorOld.mc.player.inventoryContainer.getSlot(7).getStack();
                    if (!itemStack3.isEmpty) {
                        final float percent = this.repair.getValue() / 100.0f;
                        final int dam = Math.round(itemStack3.getMaxDamage() * percent);
                        final int goods = itemStack3.getMaxDamage() - itemStack3.getItemDamage();
                        if (dam < goods) {
                            this.takeOffSlot(7);
                        }
                    }
                    final ItemStack itemStack4 = AutoArmorOld.mc.player.inventoryContainer.getSlot(8).getStack();
                    if (!itemStack4.isEmpty) {
                        final float percent4 = this.repair.getValue() / 100.0f;
                        final int dam4 = Math.round(itemStack4.getMaxDamage() * percent4);
                        final int goods4 = itemStack4.getMaxDamage() - itemStack4.getItemDamage();
                        if (dam4 < goods4) {
                            this.takeOffSlot(8);
                        }
                    }
                }
                return;
            }
            this.flag = false;
            final ItemStack helm = AutoArmorOld.mc.player.inventoryContainer.getSlot(5).getStack();
            if (helm.getItem() == Items.AIR) {
                final int slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.HEAD, this.curse.getValue(), true);
                if (slot != -1) {
                    this.getSlotOn(5, slot);
                }
            }
            final ItemStack chest = AutoArmorOld.mc.player.inventoryContainer.getSlot(6).getStack();
            if (chest.getItem() == Items.AIR) {
                final int slot2 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.CHEST, this.curse.getValue(), true);
                if (slot2 != -1) {
                    this.getSlotOn(6, slot2);
                }
            }
            final ItemStack legging = AutoArmorOld.mc.player.inventoryContainer.getSlot(7).getStack();
            if (legging.getItem() == Items.AIR) {
                final int slot3 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.LEGS, this.curse.getValue(), true);
                if (slot3 != -1) {
                    this.getSlotOn(7, slot3);
                }
            }
            final ItemStack feet = AutoArmorOld.mc.player.inventoryContainer.getSlot(8).getStack();
            if (feet.getItem() == Items.AIR) {
                final int slot4 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.FEET, this.curse.getValue(), true);
                if (slot4 != -1) {
                    this.getSlotOn(8, slot4);
                }
            }
        }
        if (this.timer.passedMs((int)(this.delay.getValue() * PigHack.serverManager.getTpsFactor()))) {
            if (!this.taskList.isEmpty()) {
                for (int i = 0; i < this.actions.getValue(); ++i) {
                    final InventoryUtil.Task task = this.taskList.poll();
                    if (task != null) {
                        task.run();
                    }
                }
            }
            this.timer.reset();
        }
    }

    private void takeOffSlot(final int slot) {
        if (this.taskList.isEmpty()) {
            int target = -1;
            for (final int i : InventoryUtil.findEmptySlots(true)) {
                if (!this.doneSlots.contains(target)) {
                    target = i;
                    this.doneSlots.add(i);
                }
            }
            if (target != -1) {
                this.taskList.add(new InventoryUtil.Task(slot));
                this.taskList.add(new InventoryUtil.Task(target));
                this.taskList.add(new InventoryUtil.Task());
            }
        }
    }

    private void getSlotOn(final int slot, final int target) {
        if (this.taskList.isEmpty()) {
            this.doneSlots.remove((Object)target);
            this.taskList.add(new InventoryUtil.Task(target));
            this.taskList.add(new InventoryUtil.Task(slot));
            this.taskList.add(new InventoryUtil.Task());
        }
    }

    private Map<Integer, ItemStack> getArmor() {
        return this.getInventorySlots(5, 8);
    }

    private Map<Integer, ItemStack> getInventorySlots(int current, final int last) {
        final Map<Integer, ItemStack> fullInventorySlots = new HashMap<Integer, ItemStack>();
        while (current <= last) {
            fullInventorySlots.put(current, (ItemStack)AutoArmorOld.mc.player.inventoryContainer.getInventory().get(current));
            ++current;
        }
        return fullInventorySlots;
    }
}