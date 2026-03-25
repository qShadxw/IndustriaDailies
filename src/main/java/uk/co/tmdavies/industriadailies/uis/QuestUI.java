package uk.co.tmdavies.industriadailies.uis;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.core.layout.LoggerFields;
import uk.co.tmdavies.industriadailies.IndustriaDailies;
import uk.co.tmdavies.industriadailies.objects.DefinedPositions;
import uk.co.tmdavies.industriadailies.objects.Quest;
import uk.co.tmdavies.industriadailies.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.co.tmdavies.industriadailies.IndustriaDailies.manager;

public class QuestUI extends ChestMenu {

    private Container cont;

    private ArrayList<ItemStack> ISS;

    private final int exitPos = 49;
    private final int nextPos = 53;
    private final int prevPos = 45;

    private final int dailyQuestPos = 24;
    private final int setQuestPos = 20;

    private final int questsPerPage = 9;

    private int pageNum = 0;

    private String menuId;

    public QuestUI(MenuType<?> type, int containerId, Inventory playerInventory, Container container, int rows) {
        super(type, containerId, playerInventory, container, rows);

        this.cont = container;
        ISS = new ArrayList<>();


        drawMainMenu(playerInventory.player);
    }

    public void drawMainMenu(Player player)
    {
        this.cont.clearContent();
        menuId = "main";

        ItemStack itemStack = createItem("minecraft:clock", "Daily Quests", "daily", "Show current daily quests");
        this.cont.setItem(dailyQuestPos, itemStack);

        itemStack = createItem("minecraft:writable_book", "Main Quests", "set", "Show all main quests");
        this.cont.setItem(setQuestPos, itemStack);

        menuBar(true, false);
    }

    public void menuBar(boolean exit, boolean navigation)
    {
        ItemStack itemStack = null;
        if (exit)
        {
            itemStack = createItem("minecraft:blue_wool", "&cExit", "Exit", "");
            cont.setItem(exitPos, itemStack);
        }
        if (navigation)
        {
            itemStack = createItem("minecraft:green_wool", "&fNext", "Next", "");
            cont.setItem(nextPos, itemStack);
            itemStack = createItem("minecraft:red_wool", "&fPrev", "Prev", "");
            cont.setItem(prevPos, itemStack);
        }
    }

    public void drawDailies(Player player)
    {
        this.cont.clearContent();
        menuId = "daily";

        List<Quest> playerQuests = IndustriaDailies.manager.getPersonalQuests(player);

        if (!playerQuests.isEmpty()) {
            AtomicInteger count = new AtomicInteger(1);

            playerQuests.forEach(quest -> {
                String rec = quest.getRewardItemId();
                if (rec == "irs") {
                    rec = "¢" + quest.getRewardItemAmount();
                }
                else
                {
                    rec = rec.replace("_", " ");
                }
                ItemStack itemStack = createItem(quest.getItemNeeded(), quest.getObjective() + ", to receive " + rec, "null", "");
                this.cont.setItem(18 + (count.getAndIncrement() * 2), itemStack);
            });

            menuBar(true, false);
            return;
        }

        ItemStack itemStack = createItem("minecraft:barrier", "&cNO CURRENT QUESTS", "null", "Visit Major Questicles in the Information Centre for new quests each day");
        cont.setItem(22, itemStack);

        menuBar(true, false);
    }

    public void drawSetQuest(Player player)
    {
        this.cont.clearContent();
        menuId = "set";
        pageNum = 0;
        List<Quest> quests = manager.getPlayerSetQuests(player);

        ItemStack itemStack = createItem("minecraft:lime_stained_glass_pane", "", "null", "");
        for (int i = 0; i < 9; i++)
        {

            cont.setItem(i, itemStack);
            cont.setItem(i + 36, itemStack);
        }

        for (int i = 0; i < questsPerPage; i++)
        {
            if (((questsPerPage * pageNum) + i) >= quests.size()) {
                break;
            }
            itemStack = createItem("minecraft:book", quests.get((questsPerPage * pageNum) + i).questName, "null", "");
            cont.setItem(i + 9, itemStack);

            String handInPos = "Location: " + quests.get((questsPerPage * pageNum) + i).handInPos.toString();
            double distToPos;


            if(quests.get(i).isCompleted())
            {
                handInPos = "";
            }
            else {
                for (int ii = 0; ii < DefinedPositions.posistions.size(); ii++) {

                    distToPos = quests.get((questsPerPage * pageNum) + i).handInPos.distanceTo(DefinedPositions.posistions.get(ii).pos);
                    if (distToPos < DefinedPositions.posistions.get(ii).maxDist) {
                        handInPos = DefinedPositions.posistions.get(ii).name + ", " + (int) distToPos + " blocks away";
                        break;
                    }
                }
                distToPos = player.getPosition(0).distanceTo(quests.get((questsPerPage * pageNum) + i).handInPos);
                if (distToPos <= 10)
                {
                    handInPos = "Nearby, " + (int)distToPos + " blocks away";
                }
            }
            itemStack = createItem("minecraft:compass", quests.get((questsPerPage * pageNum) + i).getObjective(), "null", handInPos);

            cont.setItem(i + 18, itemStack);

            if (quests.get((questsPerPage * pageNum) + i).isCompleted())
            {
                itemStack = createItem("minecraft:green_concrete", "Quest Completed!", "null", "");
            }
            else
            {
                itemStack = createItem("minecraft:red_concrete", "Quest in Progress...", "null", "");
            }
            cont.setItem(i + 27, itemStack);
        }

        if (quests.size() >= questsPerPage)
        {
            menuBar(true, true);
        }
        else
        {
            menuBar(true, false);
        }
    }

    private ItemStack createItem(String id, String name, String tag, String lore)
    {
        for (int i = 0; i < ISS.size(); i++)
        {
            if(ISS.get(i).getItem().builtInRegistryHolder().getKey().toString().equals(id))
            {
                if(ISS.get(i).get(DataComponents.ITEM_NAME).getString().equals(name))
                {
                    if (ISS.get(i).get(DataComponents.CUSTOM_DATA).copyTag().getString("res:tag").equals(tag))
                    {
                        if (ISS.get(i).get(DataComponents.LORE).lines().getFirst() == null && lore == "")
                        {
                            return ISS.get(i);
                        }
                        else if (ISS.get(i).get(DataComponents.LORE).lines().getFirst().getString().equals(lore))
                        {
                            return ISS.get(i);
                        }
                    }
                }
            }
        }

        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        ItemStack itemStack = new ItemStack(item);
        CompoundTag itemTag = new CompoundTag();
        itemTag.putString("res:tag", tag);
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(itemTag));

        if (lore != "") {
            ItemLore itemLore = new ItemLore(List.of(Utils.Chat("&f" + lore)));
            itemStack.set(DataComponents.LORE, itemLore);
        }

        itemStack.set(DataComponents.ITEM_NAME, Utils.Chat("&f" + name));
        ISS.add(itemStack);
        return ISS.getLast();

    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (this.cont.getItem(slotId).get(DataComponents.CUSTOM_DATA) == null) return;
        String tag = this.cont.getItem(slotId).get(DataComponents.CUSTOM_DATA).copyTag().getString("res:tag");

        if (tag.equals("Exit"))
        {
            if ("set".equals(menuId) || "daily".equals(menuId))
            {
                drawMainMenu(player);
            }
            else {
                ISS.clear();
                player.closeContainer();
            }
        }
        else if (tag.equals("Prev"))
        {

        }
        else if (tag.equals("Next"))
        {

        }
        else if (tag.equals("set"))
        {
            drawSetQuest(player);
        }
        else if (tag.equals("daily"))
        {
            drawDailies(player);
        }
        else
        {

        }


        return;
    }

}
