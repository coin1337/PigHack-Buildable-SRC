package me.earth.phobos.manager;

import me.earth.phobos.features.modules.client.Cosmetics;
import me.earth.phobos.util.Util;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosmeticsManager
        implements Util {
    public Map<String, List<ModelBase>> cosmeticsUserMap = new HashMap<String, List<ModelBase>>();

    public CosmeticsManager() {
        this.cosmeticsUserMap.put("befd17ef-ba7f-4bae-b6a6-702cb87fd046", Arrays.asList(Cosmetics.INSTANCE.flag, Cosmetics.INSTANCE.squidLauncher));
        this.cosmeticsUserMap.put("c235c883-65a6-4a52-b807-68157757ec7f", Arrays.asList(Cosmetics.INSTANCE.flag, Cosmetics.INSTANCE.squidLauncher));
        this.cosmeticsUserMap.put("0169823e-5ac8-4d4c-85fc-d03075d06dbe", Arrays.asList(Cosmetics.INSTANCE.flag, Cosmetics.INSTANCE.squidLauncher));
        this.cosmeticsUserMap.put("ef203388-74c9-4812-8886-0d4bb5ad05f8", Arrays.asList(Cosmetics.INSTANCE.flag, Cosmetics.INSTANCE.squidLauncher));

    }

    public List<ModelBase> getRenderModels(EntityPlayer player) {
        return this.cosmeticsUserMap.get(player.getUniqueID().toString());
    }

    public boolean hasCosmetics(EntityPlayer player) {
        return this.cosmeticsUserMap.containsKey(player.getUniqueID().toString());
    }
}

