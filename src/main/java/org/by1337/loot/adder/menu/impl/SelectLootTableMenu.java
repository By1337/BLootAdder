package org.by1337.loot.adder.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.by1337.loot.adder.Main;
import org.by1337.loot.adder.loot.loot.CustomLootManager;
import org.by1337.loot.adder.menu.AsyncClickListener;
import org.by1337.loot.adder.menu.ItemBuilder;


import java.util.*;

public class SelectLootTableMenu extends AsyncClickListener {
    public static final NamespacedKey ITEM_KEY = new NamespacedKey(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("BLootAdder")), "select_loot_table");
    private int currentPage = 0;
    private List<ItemStack> itemStacks;
    private final CustomLootManager customLootManager;
    private final Main plugin;
    private final List<String> info;
    public SelectLootTableMenu(Player viewer, CustomLootManager customLootManager, Main plugin) {
        super(viewer, plugin);
        this.customLootManager = customLootManager;
        this.plugin = plugin;
        createInventory(54, plugin.getMessage().messageBuilder("&7Выбор таблицы лута"));
        itemStacks = new ArrayList<>();
        info = new ArrayList<>();

       // long l = System.nanoTime();
        int trials = 100_000;
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < trials; i++) {
            int x = customLootManager.getDropChances().getRandomItem().getCount();
            map.put(x, map.getOrDefault(x, 0) + 1);
        }
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int value = entry.getKey();
            int count = entry.getValue();
            double frequency = (double) trials / count;
            info.add(String.format("&r &f%d предметов каждые %.2f попыток", value, frequency));
        }
     //   System.out.println(TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - l)));

        for (LootTables value : LootTables.values()) {
            var info = customLootManager.getLootTableIcoMap().get(value);
            ItemBuilder builder = new ItemBuilder();
            if (info != null) {
                builder.material(info.getIco());
                builder.name(info.getName());
            } else if (!value.getKey().getKey().startsWith("chests/")) {
                continue;
            } else {
                builder.material(Material.JIGSAW);
                builder.name("&7" + value.getKey().getKey());
            }
            List<String> list = new ArrayList<>(
                    List.of(
                            "&r ",
                            "&r &fЛКМ - &aДобавить новые предметы",
                            "&r &fПКМ - &aРедактировать шансы появления предметов",
                            "&r &fСКМ - &aСимулировать"
                    )
            );
            boolean isEmpty = customLootManager.getLootMap().getOrDefault(value, new ArrayList<>()).isEmpty();
            if (isEmpty){
                list.add("&r &f0 предметов каждые 0 попыток");
            }else {
                list.addAll(this.info);
            }
            list.add("&r &fЕсть кастомные предметы: " + (isEmpty ? "&cнет" : "&aда"));
            list.add("&r ");

            builder.lore(list);

            builder.putNbt(ITEM_KEY, value.name());
            itemStacks.add(builder.build(plugin.getMessage()));

        }
        generate();
    }

    private void generate() {
        inventory.clear();
        int slot = 0;
        for (int x = currentPage * 45; x < itemStacks.size() && slot < 45; x++) {
            inventory.setItem(slot, itemStacks.get(x));
            slot++;
        }
        inventory.setItem(45, new ItemBuilder().material(Material.ARROW).name("&cНазад").lore("").build(plugin.getMessage()));
        inventory.setItem(53, new ItemBuilder().material(Material.ARROW).name("&aВперёд").lore("").build(plugin.getMessage()));
    }

    @Override
    protected void onClose(InventoryCloseEvent e) {
    }

    @Override
    protected void onClick(InventoryClickEvent e) {
        if (e.getSlot() == 45) {
            if (currentPage > 0) {
                currentPage--;
                generate();
            }
        } else if (e.getSlot() == 53) {
            currentPage++;
            generate();
        } else {
            var item = e.getCurrentItem();
            if (item == null || item.getItemMeta() == null) return;
            var meta = item.getItemMeta();
            var pdc = meta.getPersistentDataContainer();
            String s = pdc.get(ITEM_KEY, PersistentDataType.STRING);
            if (s == null) return;
            LootTables lootTables = LootTables.valueOf(s);
            if (e.getClick().isLeftClick()) {
                var m = new MenuAddItem(viewer, () ->
                        customLootManager.getLootMap().computeIfAbsent(lootTables, k -> new ArrayList<>()), customLootManager::save, plugin);
                syncUtil(() -> viewer.openInventory(m.getInventory()));
            } else if (e.getClick().isRightClick()) {
                var m = new MenuEditChance(viewer, () ->
                        customLootManager.getLootMap().computeIfAbsent(lootTables, k -> new ArrayList<>()), customLootManager::save, plugin);
                syncUtil(() -> viewer.openInventory(m.getInventory()));
            } else if (e.getClick() == ClickType.MIDDLE) {
                syncUtil(() -> {
                    SimulateMenu simulateMenu = new SimulateMenu(
                            viewer,
                            plugin,
                            lootTables.getLootTable()
                    );
                    viewer.openInventory(simulateMenu.getInventory());
                });
            }
        }
    }

    @Override
    protected void onClick(InventoryDragEvent e) {

    }
}
