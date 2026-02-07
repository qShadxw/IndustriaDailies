package uk.co.tmdavies.industriadailies.uis;

import com.mojang.serialization.Decoder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.NotNull;
import uk.co.tmdavies.industriadailies.IndustriaDailies;
import uk.co.tmdavies.industriadailies.objects.Manager;
import uk.co.tmdavies.industriadailies.objects.Quest;
import uk.co.tmdavies.industriadailies.utils.Utils;

import java.util.List;

public class ChestUIController {
    public static SimpleContainer createContainer(int size)
    {
        SimpleContainer container = new SimpleContainer(size) {
            @Override
            public boolean canPlaceItem(int slot, @NotNull ItemStack stack)
            {
                return false;
            }

        };

        return container;
    }

    public static void openMenu(Player player, int size, SimpleContainer cont)
    {

        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x6, id, inv, cont, 6), Component.literal("Daily Quests")));

    }

    public static void openDailies(Player player, int size)
    {
        SimpleContainer cont = createContainer(size);

        List<Quest> qs = IndustriaDailies.manager.getPersonalQuests(player);
        if (qs.isEmpty())
        {
            Item i = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", "barrier"));
            ItemStack is = new ItemStack(i);
            ItemLore il = new ItemLore(List.of(Utils.Chat("&fVisit the willy man at spawn for new quests each day")));
            is.set(DataComponents.LORE, il);
            is.set(DataComponents.ITEM_NAME, Utils.Chat("&fNO CURRENT QUESTS"));


            cont.setItem(22, is);
        }
        else
        {
            for (int i = 0; i < 3; i++)
            {
                System.out.print(i);
                ItemStack is = qs.get(i).getReward();
                System.out.print(is.getItem().getName(is));
                is.setCount(1);
                //ItemLore il = new ItemLore(List.of());
                //is.set(DataComponents.LORE, il);
                is.set(DataComponents.ITEM_NAME, Utils.Chat("&f" + qs.get(i).getObjective() + ", to receive " + qs.get(i).getRewardItemId()));
                cont.setItem(20 + (i * 2), is);
            }
        }



        openMenu(player, size, cont);
    }


}
