package org.by1337.loot.adder.loot.loot;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.inventory.ItemStack;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.blib.random.WeightedItem;
import org.by1337.loot.adder.util.Validate;


import java.util.Objects;

public class InventoryItem extends Placeholder implements WeightedItem<InventoryItem> {
    private final ItemStack itemStack;
    private int chance;

    public InventoryItem(ItemStack itemStack, int chance) {
        this.itemStack = itemStack;
        this.chance = chance;
        registerPlaceholder("{chance}", this::getChance);
    }

    public static InventoryItem load(CompoundTag compoundTag) {
        int chance = Validate.invokeAndHandleException(() -> compoundTag.getAsInt("chance"),
                NullPointerException.class,
                IllegalArgumentException::new,
                "Не удалось загрузить предмет так как параметр `chance` отсутствует");
        ItemStack itemStack = BLib.getApi().getParseCompoundTag().create(
                (CompoundTag) Validate.invokeAndHandleException(() -> compoundTag.getOrThrow("item"),
                        NullPointerException.class,
                        IllegalArgumentException::new,
                        "Не удалось загрузить предмет так как параметр `item` отсутствует")
        );
        return new InventoryItem(itemStack, chance);
    }

    @CanIgnoreReturnValue
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putTag("item", BLib.getApi().getParseCompoundTag().copy(itemStack));
        compoundTag.putInt("chance", chance);
        return compoundTag;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    /**
     * Gets the value of the item.
     *
     * @return the value of the item
     */
    @Override
    public InventoryItem value() {
        return this;
    }

    @Override
    public double weight() {
        return getChance();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem that = (InventoryItem) o;
        return chance == that.chance && Objects.equals(itemStack, that.itemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemStack, chance);
    }
}
