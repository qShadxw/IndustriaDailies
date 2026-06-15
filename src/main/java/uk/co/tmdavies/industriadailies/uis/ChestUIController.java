package uk.co.tmdavies.industriadailies.uis;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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

    public static void openMenu(Player player, SimpleContainer cont) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new QuestUI(MenuType.GENERIC_9x6, id, inv, cont, 6), Component.literal("Daily Quests")));
    }

    public static void openQuests(Player player, int size) {
        SimpleContainer container = createContainer(size);

        openMenu(player, container);
    }
}
