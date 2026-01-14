package uk.co.tmdavies.industriadailies;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import uk.co.tmdavies.industriadailies.utils.ConfigFile;

@Mod(IndustriaDailies.MODID)
public class IndustriaDailies {

    public static final String MODID = "industriadailies";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ConfigFile configFile;

    public IndustriaDailies(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
        configFile = new ConfigFile("config");
        configFile.loadConfig();
    }
}
