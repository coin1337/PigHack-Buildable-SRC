package me.earth.phobos.features.modules.client;

import me.earth.phobos.features.modules.Module;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class Capes
        extends Module {
    public static final ResourceLocation THREEVT_CAPE = new ResourceLocation("textures/3vt2.png");
    public static Map<String, String[]> UUIDs = new HashMap<String, String[]>();
    private static Capes instance;

    public Capes() {
        super("Capes", "Renders the client's capes", Module.Category.CLIENT, false, false, false);

        //PigTeam

        //zipfile
        UUIDs.put("PigHax", new String[]{"befd17ef-ba7f-4bae-b6a6-702cb87fd046"});

        //hazo
        UUIDs.put("7544", new String[]{"c235c883-65a6-4a52-b807-68157757ec7f"});

        //myrh
        UUIDs.put("Routten", new String[]{"0169823e-5ac8-4d4c-85fc-d03075d06dbe"});

        //entire
        UUIDs.put("FutureFag", new String[]{"ef203388-74c9-4812-8886-0d4bb5ad05f8"});


        instance = this;
    }

    public static Capes getInstance() {
        if (instance == null) {
            instance = new Capes();
        }
        return instance;
    }

    public static ResourceLocation getCapeResource(AbstractClientPlayer player) {
        for (String name : UUIDs.keySet()) {
            for (String uuid : UUIDs.get(name)) {
                if (name.equalsIgnoreCase("PigHax") && player.getUniqueID().toString().equals(uuid)) {
                    return THREEVT_CAPE;
                }
                if (name.equalsIgnoreCase("Hazo") && player.getUniqueID().toString().equals(uuid)) {
                    return THREEVT_CAPE;
                }
            }
        }
        return null;
    }

    public static boolean hasCape(UUID uuid) {
        Iterator<String> iterator = UUIDs.keySet().iterator();
        if (iterator.hasNext()) {
            String name = iterator.next();
            return Arrays.asList((Object[]) UUIDs.get(name)).contains(uuid.toString());
        }
        return false;
    }
}

