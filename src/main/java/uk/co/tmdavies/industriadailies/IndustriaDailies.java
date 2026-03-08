package uk.co.tmdavies.industriadailies;

import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import de.markusbordihn.easynpc.entity.easynpc.event.EasyNPCEventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreAccess;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import uk.co.tmdavies.industriadailies.commands.MainCommand;
import uk.co.tmdavies.industriadailies.commands.PlayerCommand;
import uk.co.tmdavies.industriadailies.objects.DefinedPositions;
import uk.co.tmdavies.industriadailies.objects.Manager;
import uk.co.tmdavies.industriadailies.files.ConfigFile;
import uk.co.tmdavies.industriadailies.objects.NeoNetworkIRS;
import uk.co.tmdavies.industriadailies.objects.Quest;
import uk.co.tmdavies.industriadailies.savedata.TargetDataStorage;
import uk.co.tmdavies.industriadailies.utils.Utils;

import java.util.List;

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

        DefinedPositions.init(event.getServer());

        manager = new Manager();
        manager.loadQuests();
        manager.initSaveData(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event)
    {
        manager.saveSaveData(event.getServer());
    }

    @SubscribeEvent
    public void onPlayerInteractEvent(PlayerInteractEvent.EntityInteractSpecific event)
    {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        if (!manager.playerSetQuests.containsKey(player.getStringUUID())) return;


        Entity target = event.getTarget();

        if (target instanceof EasyNPC npc) {
            List<Quest> quests = manager.playerSetQuests.get(player.getStringUUID());
            for (int i = 0; i < quests.size(); i++)
            {
                if (quests.get(i).isCompleted()) continue;
                if ((quests.get(i).getTalkTo().isEmpty() || quests.get(i).getTalkTo() == null) && (quests.get(i).getHandIn().isEmpty() || quests.get(i).getHandIn() == null)) return;

                if (quests.get(i).checkTalkTo(target.getUUID().toString()))
                {
                    if (quests.get(i).hasTalkedTo) continue;
                    manager.playerSetQuests.get(player.getStringUUID()).get(i).hasTalkedTo = true;
                    ServerScoreboard scoreboard = player.getServer().getScoreboard();
                    Objective objective = scoreboard.getObjective(quests.get(i).getId());
                    if (objective != null)
                    {
                        ScoreAccess score = scoreboard.getOrCreatePlayerScore(player, objective);
                        score.add(1);
                    }
                    Utils.playSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP);
                    Utils.displayTitle(player, "Quest Advanced", ChatFormatting.GOLD);
                    return;
                }
                if (quests.get(i).checkHandIn(target.getUUID().toString()))
                {
                    if (!quests.get(i).getHandIn().isEmpty())
                    {
                        if (quests.get(i).hasTalkedTo)
                        {
                            if (manager.fullQuestCheckComplete(player, quests.get(i), target)) Utils.displayTitle(player, "Quest Completed!", ChatFormatting.GOLD);
                            Utils.playSound(player, SoundEvents.PLAYER_LEVELUP);
                            return;
                        }
                    }
                    else
                    {
                        if (manager.fullQuestCheckComplete(player, quests.get(i), target)) Utils.displayTitle(player, "Quest Completed!", ChatFormatting.GOLD);
                        Utils.playSound(player, SoundEvents.PLAYER_LEVELUP);
                        return;
                    }

                }

            }
        }
    }



    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {

        MainCommand.register(event.getDispatcher());
        PlayerCommand.register(event.getDispatcher());
    }

}
