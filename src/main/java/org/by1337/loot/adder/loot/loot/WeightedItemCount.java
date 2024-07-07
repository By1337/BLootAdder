package org.by1337.loot.adder.loot.loot;


import org.by1337.blib.random.WeightedItem;

public class WeightedItemCount implements WeightedItem<WeightedItemCount> {
    private final double chance;
    private final int count;

    public WeightedItemCount(double chance, int count) {
        this.chance = chance;
        this.count = count;
    }

    @Override
    public double weight() {
        return chance;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "WeightedItemCount{" +
                "chance=" + chance +
                ", count=" + count +
                '}';
    }

    /**
     * Gets the value of the item.
     *
     * @return the value of the item
     */
    @Override
    public WeightedItemCount value() {
        return this;
    }
}
