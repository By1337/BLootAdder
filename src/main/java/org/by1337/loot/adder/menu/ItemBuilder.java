package org.by1337.loot.adder.menu;

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class ItemBuilder {
    private List<String> lore = new ArrayList<>();
    private String name;
    private String material;
    private List<Pair<NamespacedKey, String>> nbts = new ArrayList<>();
    private int modelData = 0;
    private List<ItemFlag> flags = new ArrayList<>();
    private List<Consumer<ItemMeta>> consumers = new ArrayList<>();

    public ItemBuilder lore(String... lore) {
        return lore(new ArrayList<>(List.of(lore)));
    }

    public ItemBuilder lore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder replaceLore(UnaryOperator<String> operator) {
        this.lore.replaceAll(operator);
        return this;
    }

    public ItemBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder putNbt(NamespacedKey name, String value) {
        nbts.add(Pair.of(name, value));
        return this;
    }

    public ItemBuilder material(String material) {
        this.material = material;
        return this;
    }

    public ItemBuilder material(Material material) {
        this.material = material.name();
        return this;
    }

    public ItemBuilder modelData(int modelData) {
        this.modelData = modelData;
        return this;
    }
    public ItemBuilder putItemFlags(ItemFlag... flags) {
        this.flags.addAll(Arrays.stream(flags).toList());
        return this;
    }
    public ItemBuilder addConsumer(Consumer<ItemMeta> consumer) {
        consumers.add(consumer);
        return this;
    }


    public ItemStack build(Message message) {
        return build(new ItemStack(Material.valueOf(material)), message);
    }

    public ItemStack build(ItemStack itemStack, Message message) {
        var meta = itemStack.getItemMeta();
        if (name != null)
            meta.displayName(message.componentBuilder(name).decoration(TextDecoration.ITALIC, false));
        List<String> lore0 = new ArrayList<>();
        if (meta.getLore() != null) {
            lore0.addAll(meta.getLore());
        }
        lore0.addAll(lore);
        meta.lore(lore0.stream().map(s -> message.componentBuilder(s).decoration(TextDecoration.ITALIC, false)).toList());
        if (!nbts.isEmpty()) {
            var pdc = meta.getPersistentDataContainer();
            for (Pair<NamespacedKey, String> nbt : nbts) {
                pdc.set(nbt.getLeft(), PersistentDataType.STRING, nbt.getRight());
            }
        }
        if (modelData != 0){
            meta.setCustomModelData(modelData);
        }
        for (Consumer<ItemMeta> consumer : consumers) {
            consumer.accept(meta);
        }
        for (ItemFlag flag : flags) {
            meta.addItemFlags(flag);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
