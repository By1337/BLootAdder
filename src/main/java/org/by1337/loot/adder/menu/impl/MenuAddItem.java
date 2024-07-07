package org.by1337.loot.adder.menu.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.by1337.loot.adder.Main;
import org.by1337.loot.adder.loot.loot.InventoryItem;
import org.by1337.loot.adder.menu.AsyncClickListener;
import org.by1337.loot.adder.menu.ItemBuilder;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MenuAddItem extends AsyncClickListener {
    private final int itemSlots = 45;
    private final Player player;
    private int page = 0;
    private Map<Integer, InventoryItem> itemsOnScreen;
    private final Supplier<List<InventoryItem>> itemProvider;
    private final Runnable saveProvider;
    private final Main plugin;

    public MenuAddItem(Player player, Supplier<List<InventoryItem>> itemProvider, Runnable saveProvider, Main plugin) {
        super(player, false, plugin);
        this.player = player;
        this.itemProvider = itemProvider;
        this.saveProvider = saveProvider;
        this.plugin = plugin;
        createInventory(54, plugin.getMessage().messageBuilder("&7Добавление предметов"));
        generate();
    }


    private void generate() {
        inventory.clear();
        inventory.setItem(45, new ItemBuilder().material(Material.ARROW).name("&cНазад").lore("").build(plugin.getMessage()));
        inventory.setItem(53, new ItemBuilder().material(Material.ARROW).name("&aВперёд").lore("").build(plugin.getMessage()));
        ItemStack gui = new ItemBuilder().material(Material.BLACK_STAINED_GLASS_PANE).name(" ").lore("").build(plugin.getMessage());
        for (int i = 46; i < 53; i++) {
            inventory.setItem(i, gui);
        }
        inventory.setItem(49, new ItemBuilder().material(Material.BARRIER).name("&cВЫЙТИ").lore("").build(plugin.getMessage()));

        int startPos = itemSlots * page;
        var list = itemProvider.get();
        itemsOnScreen = new HashMap<>();
        for (int i = startPos, slot = 0; i < startPos + itemSlots; i++, slot++) {
            if (list.size() <= i) break;
            var item = list.get(i);
            var itemStack = item.getItemStack();
            itemsOnScreen.put(slot, item);
            inventory.setItem(slot, itemStack);
        }
    }

    private void update() {
        for (int i = 0; i < 45; i++) {
            var itemStack = inventory.getItem(i);
            InventoryItem item = itemsOnScreen.get(i);

            if (item == null) {
                if (itemStack != null) {
                    itemProvider.get().add(new InventoryItem(itemStack, 100));
                }
            } else {
                if (itemStack == null) {
                    itemProvider.get().remove(item);
                } else if (!item.getItemStack().equals(itemStack)) {
                    itemProvider.get().remove(item);
                    itemProvider.get().add(new InventoryItem(itemStack, 100));
                }
            }
        }
        generate();
    }


    @Override
    protected void onClose(InventoryCloseEvent e) {
        saveProvider.run();
    }

    @Override
    protected void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() != inventory) {
            e.setCancelled(false);
            syncUtil(this::update, 1);
            return;
        }
        if (e.getSlot() < 44) {
            syncUtil(this::update, 1);
            e.setCancelled(false);
            return;
        }
        if (e.getSlot() == 45) {
            if (page > 0) {
                page--;
                generate();
            }
        } else if (e.getSlot() == 53) {
            page++;
            generate();
        } else if (e.getSlot() == 49) {
            SelectLootTableMenu selectLootTableMenu = new SelectLootTableMenu(player, plugin.getCustomLootManager(), plugin);
            syncUtil(() -> player.openInventory(selectLootTableMenu.getInventory()));
        }
    }

    @Override
    protected void onClick(InventoryDragEvent e) {
        if (e.getRawSlots().stream().anyMatch(slot -> slot >= 45 && slot <= 53)) return;
        e.setCancelled(false);
        syncUtil(this::update, 1);
    }

    public Inventory getInventory() {
        return inventory;
    }
}
