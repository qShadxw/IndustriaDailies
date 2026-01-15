package uk.co.tmdavies.industriadailies.objects;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import uk.co.tmdavies.industriadailies.IndustriaDailies;

public class Quest {

    private final String id;
    private final String objective;
    private final String itemNeeded;
    private final int amountNeeded;
    private final String rewardItemId;
    private final int rewardItemAmount;
    private final int weight;
    private boolean completed;

    public Quest(String id, String objective, String itemNeeded, int amountNeeded, String rewardItemId, int rewardItemAmount, int weight, boolean completed) {
        this.id = id;
        this.objective = objective;
        this.itemNeeded = itemNeeded;
        this.amountNeeded = amountNeeded;
        this.rewardItemId = rewardItemId;
        this.rewardItemAmount = rewardItemAmount;
        this.weight = weight;
        this.completed = false;
    }

    public String getId() {
        return id;
    }

    public String getObjective() {
        return objective;
    }

    public String getItemNeeded() {
        return itemNeeded;
    }

    public int getAmountNeeded() {
        return amountNeeded;
    }

    public String getRewardItemId() {
        return rewardItemId;
    }

    public int getRewardItemAmount() {
        return rewardItemAmount;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isCompleted() {
        return completed;
    }

    public ItemStack getReward() {
        String[] itemInfo = rewardItemId.split(":");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(itemInfo[0], itemInfo[1]);
        Item item = BuiltInRegistries.ITEM.get(id);
        ItemStack itemStack = new ItemStack(item);

        itemStack.setCount(this.rewardItemAmount);
        return itemStack;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void verboseQuest() {
        IndustriaDailies.LOGGER.info("Id: {}, Objective: {}, ItemNeeded: {}, AmountNeeded: {}, RewardItemId: {}, RewardItemAmount: {}. Weight: {}, Completed: {}", id, objective, itemNeeded, amountNeeded, rewardItemId, rewardItemAmount, weight, completed);
    }

    @Override
    public String toString() {
        return String.format("Quest[id='%s', objective='%s', itemNeeded='%s', amountNeeded='%s', rewardItemId='%s', rewardItemAmount='%s', weight='%s', completed='%s']", id, objective, itemNeeded, amountNeeded, rewardItemId, rewardItemAmount, weight, completed);
    }
}
