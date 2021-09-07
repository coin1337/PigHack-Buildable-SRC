package me.earth.phobos;

import me.earth.phobos.event.FrameUtil;
import me.earth.phobos.event.HWIDUtil;
import me.earth.phobos.event.NetworkUtil;
import me.earth.phobos.event.NoStackTraceThrowable;
import me.earth.phobos.features.modules.misc.RPC;
import me.earth.phobos.manager.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.List;

@Mod(modid = PigHack.MODID, name = PigHack.MODNAME, version = PigHack.MODVER)
public class PigHack {
    public static final String MODID = "pighack-r";
    public static final String MODNAME = "PigHack";
    public static final String MODVER = "2.0";
    public static final Logger LOGGER = LogManager.getLogger("PigHack");
    public static ModuleManager moduleManager;
    public static SpeedManager speedManager;
    public static PositionManager positionManager;
    public static RotationManager rotationManager;
    public static CommandManager commandManager;
    public static EventManager eventManager;
    public static ConfigManager configManager;
    public static FileManager fileManager;
    public static FriendManager friendManager;
    public static TextManager textManager;
    public static ColorManager colorManager;
    public static ServerManager serverManager;
    public static PotionManager potionManager;
    public static InventoryManager inventoryManager;
    public static TimerManager timerManager;
    public static PacketManager packetManager;
    public static ReloadManager reloadManager;
    public static TotemPopManager totemPopManager;
    public static HoleManager holeManager;
    public static NotificationManager notificationManager;
    public static SafetyManager safetyManager;
    //public static GuiCustomMainScreen customMainScreen;
    public static CosmeticsManager cosmeticsManager;
    public static NoStopManager baritoneManager;
    @Mod.Instance
    public static PigHack INSTANCE;
    private static boolean unloaded;
    public static List<String> hwidList = new ArrayList<>();


    static {
        unloaded = false;
    }



    public static void load() {
        LOGGER.info("\n\nLoading PigHack" + MODVER);
        unloaded = false;
        if (reloadManager != null) {
            reloadManager.unload();
            reloadManager = null;
        }
        baritoneManager = new NoStopManager();
        totemPopManager = new TotemPopManager();
        timerManager = new TimerManager();
        packetManager = new PacketManager();
        serverManager = new ServerManager();
        colorManager = new ColorManager();
        textManager = new TextManager();
        moduleManager = new ModuleManager();
        speedManager = new SpeedManager();
        rotationManager = new RotationManager();
        positionManager = new PositionManager();
        commandManager = new CommandManager();
        eventManager = new EventManager();
        configManager = new ConfigManager();
        fileManager = new FileManager();
        friendManager = new FriendManager();
        potionManager = new PotionManager();
        inventoryManager = new InventoryManager();
        holeManager = new HoleManager();
        notificationManager = new NotificationManager();
        safetyManager = new SafetyManager();
        LOGGER.info("Initialized Managers");
        moduleManager.init();
        LOGGER.info("Modules loaded.");
        configManager.init();
        eventManager.init();
        LOGGER.info("EventManager loaded.");
        textManager.init(true);
        moduleManager.onLoad();
        totemPopManager.init();
        timerManager.init();
        if (moduleManager.getModuleByClass(RPC.class).isEnabled()) {
            DiscordRP.start();
        }
        cosmeticsManager = new CosmeticsManager();
        LOGGER.info("PigHack initialized!\n");
    }

    public static void unload(boolean unload) {
        LOGGER.info("\n\nUnloading PigHack " + MODVER);
        if (unload) {
            reloadManager = new ReloadManager();
            reloadManager.init(commandManager != null ? commandManager.getPrefix() : ".");
        }
        if (baritoneManager != null) {
            baritoneManager.stop();
        }
        PigHack.onUnload();
        eventManager = null;
        holeManager = null;
        timerManager = null;
        moduleManager = null;
        totemPopManager = null;
        serverManager = null;
        colorManager = null;
        textManager = null;
        speedManager = null;
        rotationManager = null;
        positionManager = null;
        commandManager = null;
        configManager = null;
        fileManager = null;
        friendManager = null;
        potionManager = null;
        inventoryManager = null;
        notificationManager = null;
        safetyManager = null;
        LOGGER.info("PigHack unloaded!\n");
    }

    public static void reload() {
        PigHack.unload(false);
        PigHack.load();
    }

    public static void onUnload() {
        if (!unloaded) {
            eventManager.onUnload();
            moduleManager.onUnload();
            configManager.saveConfig(PigHack.configManager.config.replaceFirst("pighack-r/", ""));
            moduleManager.onUnloadPost();
            timerManager.unload();
            unloaded = true;
        }
    }


    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        //customMainScreen = new GuiCustomMainScreen();
        Display.setTitle("PigHack " + MODVER + " | 1.12.2");
        load();
    }
    public static final String KEY = "pigteam";
    public static final String HWID_URL = "https://gist.githubusercontent.com/oyzipfile/03b1b2397d95e441505bddbde0f90131/raw/abd7aceced14498fbdede41d1ad0d6b1e7686df9/gistfile1.txt";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Verify();
    }
    public void Verify(){
        //Here we get the HWID List From URL
        hwidList = NetworkUtil.getHWIDList();

        //Check HWID
        if(!hwidList.contains(HWIDUtil.getEncryptedHWID(KEY))){
            //Shutdown client and display message
            FrameUtil.Display();
            throw new NoStackTraceThrowable("Verify HWID Failed!");
        }

    }

}

