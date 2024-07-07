package org.by1337.loot.adder.loot.loot;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.NBTParser;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.blib.nbt.impl.ListNBT;
import org.by1337.blib.random.WeightedItemSelector;
import org.by1337.loot.adder.Main;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomLootManager implements Listener {
    private Map<LootTables, WeightedItemSelector<InventoryItem>> randomItemSelectorMap;
    private Map<LootTables, List<InventoryItem>> lootMap;
    private final Main plugin;
    private final Map<LootTables, LootTableIco> lootTableIcoMap;
    private final boolean chestInfo;
    private final WeightedItemSelector<WeightedItemCount> dropChances;
    private final Map<NamespacedKey, LootTables> LOOKUP_LOOT_TABLE_BY_ID;

    public CustomLootManager(Main plugin, boolean chestInfo, WeightedItemSelector<WeightedItemCount> dropChances) {
        this.plugin = plugin;
        this.chestInfo = chestInfo;
        this.dropChances = dropChances;


        File file = plugin.trySave("tables.snbt");

        CompoundTag compoundTag;
        try {
            compoundTag = NBTParser.parseAsCompoundTag(Files.readString(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lootTableIcoMap = new HashMap<>();
        for (String s : compoundTag.getTags().keySet()) {
            var v = LootTables.valueOf(s);
            if (!v.getKey().getKey().startsWith("chests/")) continue;
            lootTableIcoMap.put(v, new LootTableIco(compoundTag.getAsCompoundTag(s), v));
        }
        LOOKUP_LOOT_TABLE_BY_ID = new HashMap<>();
        for (LootTables value : LootTables.values()) {
            LOOKUP_LOOT_TABLE_BY_ID.put(value.getKey(), value);
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
        load();
    }

    public void load() {
        lootMap = new HashMap<>();
        randomItemSelectorMap = new HashMap<>();
        try {
            File file = new File(plugin.getDataFolder(), "customItems.snbt");
            if (!file.exists()) {
                return;
            }
            String snbt = Files.readString(file.toPath());
            CompoundTag compoundTag = NBTParser.parseAsCompoundTag(snbt);

            for (String s : compoundTag.getTags().keySet()) {
                var v = LootTables.valueOf(s);

                ListNBT listNBT = (ListNBT) compoundTag.get(s);
                List<InventoryItem> list = new ArrayList<>();
                for (NBT nbt : listNBT) {
                    list.add(InventoryItem.load((CompoundTag) nbt));
                }
                lootMap.put(v, list);
            }

            rebuildItemSelectors();
        } catch (IOException e) {
            plugin.getMessage().error("failed to load items", e);
        }

    }

    public void save() {
        if (lootMap.isEmpty()) return;
        try {
            CompoundTag compoundTag = new CompoundTag();

            for (LootTables lootTables : lootMap.keySet()) {
                ListNBT listNBT = new ListNBT();

                for (InventoryItem inventoryItem : lootMap.get(lootTables)) {
                    listNBT.add(inventoryItem.save(new CompoundTag()));
                }
                compoundTag.putTag(lootTables.name(), listNBT);
            }

            File file = new File(plugin.getDataFolder(), "customItems.snbt");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            Files.writeString(file.toPath(), compoundTag.toString());

        } catch (IOException e) {
            plugin.getMessage().error("failed to save items", e);
        }
        rebuildItemSelectors();
    }
    @EventHandler
    public void onLootGetEvent(LootGenerateEvent e) {
        if (chestInfo){
            plugin.getMessage().sendAllOp("&7" + e.getLootTable().getKey().getKey());
        }
        var selector = randomItemSelectorMap.get(LOOKUP_LOOT_TABLE_BY_ID.get(e.getLootTable().getKey()));
        if (selector == null) return;

        int count = dropChances.getRandomItem().getCount();
        List<ItemStack> list = new ArrayList<>(e.getLoot());
        for (int i = 0; i < count; i++) {
            var item = selector.getRandomItem();
            if (item != null){
                list.add(item.getItemStack());
            }
        }
        e.setLoot(list);
    }

    public void rebuildItemSelectors(){
        randomItemSelectorMap.clear();
        for (LootTables lootTables : lootMap.keySet()) {
            randomItemSelectorMap.put(lootTables, new WeightedItemSelector<>(lootMap.get(lootTables)));
        }
    }

    public Map<LootTables, LootTableIco> getLootTableIcoMap() {
        return lootTableIcoMap;
    }

    public Map<LootTables, WeightedItemSelector<InventoryItem>> getRandomItemSelectorMap() {
        return randomItemSelectorMap;
    }

    public Map<LootTables, List<InventoryItem>> getLootMap() {
        return lootMap;
    }

    public WeightedItemSelector<WeightedItemCount> getDropChances() {
        return dropChances;
    }
}
