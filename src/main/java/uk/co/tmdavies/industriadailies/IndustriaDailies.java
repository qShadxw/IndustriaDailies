package uk.co.tmdavies.industriadailies;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import uk.co.tmdavies.industriadailies.commands.MainCommand;
import uk.co.tmdavies.industriadailies.objects.Manager;
import uk.co.tmdavies.industriadailies.files.ConfigFile;
import uk.co.tmdavies.industriadailies.objects.NeoNetworkIRS;

@Mod(IndustriaDailies.MODID)
public class IndustriaDailies {

    public static final String MODID = "industriadailies";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ConfigFile configFile;
    public static ConfigFile irsFile;
    public static Manager manager;
    public static NeoNetworkIRS neoNetworkIRS;

    public IndustriaDailies(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Setting up...");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Loading...");

        // Quests Config
        configFile = new ConfigFile("config");
        configFile.loadConfig();

        // NeoNetwork IRS Config
        irsFile = new ConfigFile("neonetworkirs");
        irsFile.loadConfig();

        neoNetworkIRS = new NeoNetworkIRS(irsFile.getElement("apikey").getAsString());

        manager = new Manager();
        manager.loadQuests();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        MainCommand.register(event.getDispatcher());
    }
}
