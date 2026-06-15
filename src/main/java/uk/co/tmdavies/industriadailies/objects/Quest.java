package uk.co.tmdavies.industriadailies.objects;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import uk.co.tmdavies.industriadailies.IndustriaDailies;

import java.util.Objects;
import java.util.UUID;

import static com.mojang.text2speech.Narrator.LOGGER;


public class Quest {

    private final String id;
    private final String objective;
    private final String itemNeeded;
    private final int amountNeeded;
    private final String rewardItemId;
    private final int rewardItemAmount;
    private final int weight;
    private boolean completed;
    private String[] required;
    private final String talkTo;
    private final String handIn;
    public boolean hasTalkedTo;
    public final String questName;
    public Vec3 handInPos;

    public Quest(String id, String objective, String itemNeeded, int amountNeeded, String rewardItemId, int rewardItemAmount, int weight, boolean completed) {
        this.id = id;
        this.objective = objective;
        this.itemNeeded = itemNeeded;
        this.amountNeeded = amountNeeded;
        this.rewardItemId = rewardItemId;
        this.rewardItemAmount = rewardItemAmount;
        this.weight = weight;
        this.completed = false;
        this.required = null;
        this.talkTo = null;
        this.handIn = null;
        this.hasTalkedTo = false;
        this.questName = "";
        this.handInPos = null;
    }
    public Quest(String name, String id, String objective, String itemNeeded, int amountNeeded, String rewardItemId, int rewardItemAmount, int weight, Vec3 handInPos, String[] required, String talkTo, String handIn) {
        this.id = id;
        this.objective = objective;
        this.itemNeeded = itemNeeded;
        this.amountNeeded = amountNeeded;
        this.rewardItemId = rewardItemId;
        this.rewardItemAmount = rewardItemAmount;
        this.weight = weight;
        this.completed = false;
        this.required = required;
        this.talkTo = talkTo;
        this.handIn = handIn;
        this.hasTalkedTo = false;
        this.questName = name;
        this.handInPos  = handInPos;
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

    public ItemStack getItemNeededAsItemstack() {
        String[] itemInfo = itemNeeded.split(":");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(itemInfo[0], itemInfo[1]);
        Item item = BuiltInRegistries.ITEM.get(id);

        return new ItemStack(item);
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

    public String[] getRequired() { return required; }

    public boolean checkTalkTo(String uuid) { return (Objects.equals(uuid, this.talkTo)); }

    public boolean checkHandIn(String uuid) { return (Objects.equals(uuid, this.handIn)); }

    public Quest copy() {
        return new Quest(this.questName, this.id, this.objective, this.itemNeeded, this.amountNeeded, this.rewardItemId, this.rewardItemAmount, this.weight, this.handInPos, this.required, this.talkTo, this.handIn);
    }

    public String getHandIn() { return handIn; }
    public String getTalkTo() { return talkTo; }

    public void verboseQuest() {
        IndustriaDailies.LOGGER.info("Id: {}, Objective: {}, ItemNeeded: {}, AmountNeeded: {}, RewardItemId: {}, RewardItemAmount: {}. Weight: {}, Completed: {}", id, objective, itemNeeded, amountNeeded, rewardItemId, rewardItemAmount, weight, completed);
    }

    @Override
    public String toString() {
        return String.format("Quest[id='%s', objective='%s', itemNeeded='%s', amountNeeded='%s', rewardItemId='%s', rewardItemAmount='%s', weight='%s', completed='%s']", id, objective, itemNeeded, amountNeeded, rewardItemId, rewardItemAmount, weight, completed);
    }
}
