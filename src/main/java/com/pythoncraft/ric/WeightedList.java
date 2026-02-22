package com.pythoncraft.ric;

import java.util.HashMap;
import java.util.List;

import com.pythoncraft.gamelib.inventory.ItemSet;

public class WeightedList<T> {
    public HashMap<T, Integer> items = new HashMap<>();
    public int length = 0;

    public WeightedList(HashMap<T, Integer> items) {
        this.items = items;
        this.updateLength();
    }

    public WeightedList() {}

    public void add(T item, int weight) {
        this.items.put(item, weight);
        this.updateLength();
    }

    public int count() {return this.items.size();}
    public int getWeight(T item) {return this.items.getOrDefault(item, 0);}
    public int getLength() {return this.length;}

    private void updateLength() {
        this.length = items.values().stream().mapToInt(Integer::intValue).sum();
    }

    public T getRandom() {
        if (this.length == 0) {return null;}
        int randomWeight = (int) (Math.random() * this.length);
        int currentWeight = 0;

        for (T item : items.keySet()) {
            currentWeight += items.get(item);
            if (randomWeight < currentWeight) {
                return item;
            }
        }

        return null; // This should never happen if the list is not empty
    }

    public void clear() {
        this.items.clear();
        this.length = 0;
    }

    public static WeightedList<ItemSet> fromItemSets(List<ItemSet> itemSets) {
        WeightedList<ItemSet> weightedList = new WeightedList<>();
        for (ItemSet itemSet : itemSets) {
            weightedList.add(itemSet, itemSet.getMetadataInt("probability", PluginMain.getInstance().defaultProbability));
        }
        return weightedList;
    }
}
