package uk.co.tmdavies.industriadailies.uis;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.NotNull;
import uk.co.tmdavies.industriadailies.IndustriaDailies;
import uk.co.tmdavies.industriadailies.objects.Quest;
import uk.co.tmdavies.industriadailies.utils.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChestUIController {

    public static SimpleContainer createContainer(int size) {
        return new SimpleContainer(size) {
            @Override
            public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeItem(@NotNull Container target, int slot, @NotNull ItemStack stack) {
                return false;
            }
        };
    }

    public static void openMenu(Player player, int size, SimpleContainer cont) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x6, id, inv, cont, 6), Component.literal("Daily Quests")));
    }

    public static void openDailies(Player player, int size) {
        SimpleContainer container = createContainer(size);
        List<Quest> playerQuests = IndustriaDailies.manager.getPersonalQuests(player);

        if (!playerQuests.isEmpty()) {
            AtomicInteger count = new AtomicInteger(1);
            playerQuests.forEach(quest -> {
                ItemStack itemStack = quest.getReward();
                itemStack.setCount(1);
                itemStack.set(DataComponents.ITEM_NAME, Utils.Chat("&f" + quest.getObjective() + ", to receive " + quest.getRewardItemId()));
                container.setItem(18 + (count.getAndIncrement() * 2), itemStack);
            });

            openMenu(player, size, container);

            return;
        }

        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", "barrier"));
        ItemStack itemStack = new ItemStack(item);
        ItemLore itemLore = new ItemLore(List.of(Utils.Chat("&fVisit the willy man at spawn for new quests each day")));

        itemStack.set(DataComponents.LORE, itemLore);
        itemStack.set(DataComponents.ITEM_NAME, Utils.Chat("&fNO CURRENT QUESTS"));
        container.setItem(22, itemStack);

        openMenu(player, size, container);
    }


}
