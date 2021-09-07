package me.earth.phobos.features.modules.misc;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.util.PlayerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MobOwner extends Module {
    private final Map<Entity, String> owners = new HashMap<>();

    private final Map<Entity, UUID> toLookUp = new ConcurrentHashMap<>();

    private final List<Entity> lookedUp = new ArrayList<>();

    public MobOwner() {
        super("MobOwner", "Shows you who owns mobs.", Module.Category.MISC, false, false, false);
    }

    public void onUpdate() {
        if (fullNullCheck())
            return;
        if (PlayerUtil.timer.passedS(5.0D))
            for (Map.Entry<Entity, UUID> entry : this.toLookUp.entrySet()) {
                Entity entity = entry.getKey();
                UUID uuid = entry.getValue();
                if (uuid != null) {
                    EntityPlayer owner = mc.world.getPlayerEntityByUUID(uuid);
                    if (owner != null) {
                        this.owners.put(entity, owner.getName());
                        this.lookedUp.add(entity);
                        continue;
                    }
                    try {
                        String name = PlayerUtil.getNameFromUUID(uuid);
                        if (name != null) {
                            this.owners.put(entity, name);
                            this.lookedUp.add(entity);
                        }
                    } catch (Exception e) {
                        this.lookedUp.add(entity);
                        this.toLookUp.remove(entry);
                    }
                    PlayerUtil.timer.reset();
                    break;
                }
                this.lookedUp.add(entity);
                this.toLookUp.remove(entry);
            }
        for (Entity entity : mc.world.getLoadedEntityList()) {
            if (!entity.getAlwaysRenderNameTag()) {
                if (entity instanceof EntityTameable) {
                    EntityTameable tameableEntity = (EntityTameable) entity;
                    if (tameableEntity.isTamed() && tameableEntity.getOwnerId() != null) {
                        if (this.owners.get(tameableEntity) != null) {
                            tameableEntity.setAlwaysRenderNameTag(true);
                            tameableEntity.setCustomNameTag(this.owners.get(tameableEntity));
                            continue;
                        }
                        if (!this.lookedUp.contains(entity))
                            this.toLookUp.put(tameableEntity, tameableEntity.getOwnerId());
                    }
                    continue;
                }
                if (entity instanceof AbstractHorse) {
                    AbstractHorse tameableEntity = (AbstractHorse) entity;
                    if (tameableEntity.isTame() && tameableEntity.getOwnerUniqueId() != null) {
                        if (this.owners.get(tameableEntity) != null) {
                            tameableEntity.setAlwaysRenderNameTag(true);
                            tameableEntity.setCustomNameTag(this.owners.get(tameableEntity));
                            continue;
                        }
                        if (!this.lookedUp.contains(entity))
                            this.toLookUp.put(tameableEntity, tameableEntity.getOwnerUniqueId());
                    }
                }
            }
        }
    }

    public void onDisable() {
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityTameable || entity instanceof AbstractHorse)
                try {
                    entity.setAlwaysRenderNameTag(false);
                } catch (Exception exception) {
                }
        }
    }
}