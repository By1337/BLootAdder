package org.by1337.loot.adder.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.by1337.loot.adder.Main;
import org.by1337.loot.adder.menu.AsyncClickListener;
import org.by1337.loot.adder.menu.ItemBuilder;

import java.util.Random;

public class SimulateMenu extends AsyncClickListener {
    private final Main plugin;
    private final LootTable lootTable;

    public SimulateMenu(Player player, Main plugin, LootTable lootTable) {
        super(player, false, plugin);
        this.plugin = plugin;
        this.lootTable = lootTable;
        createInventory(36, plugin.getMessage().messageBuilder("&7Генерация предметов"));
        generate();
    }

    private void generate() {
        inventory.clear();
        Inventory inventory1 = Bukkit.createInventory(null, 27);
        lootTable.fillInventory(
                inventory1,
                new Random(),
                new LootContext.Builder(viewer.getLocation())
                        .lootedEntity(viewer)
                        .build()
        );
        for (int i = 0; i < inventory1.getSize(); i++) {
            inventory.setItem(i, inventory1.getItem(i));
        }
        ItemStack gui = new ItemBuilder().material(Material.BLACK_STAINED_GLASS_PANE).name(" ").lore("").build(plugin.getMessage());
        for (int i = 27; i < 35; i++) {
            inventory.setItem(i, gui);
        }
        inventory.setItem(31, new ItemBuilder().material(Material.BARRIER).name("&cВЫЙТИ").lore("").build(plugin.getMessage()));
        inventory.setItem(32, new ItemBuilder().material(Material.CLOCK).name("&aРегенерировать").lore("").build(plugin.getMessage()));
    }

    @Override
    protected void onClose(InventoryCloseEvent e) {

    }

    @Override
    protected void onClick(InventoryClickEvent e) {
        if (e.getSlot() == 31) {
            SelectLootTableMenu selectLootTableMenu = new SelectLootTableMenu(viewer, plugin.getCustomLootManager(), plugin);
            viewer.openInventory(selectLootTableMenu.getInventory());
        } else if (e.getSlot() == 32) {
           generate();
        }
    }

    @Override
    protected void onClick(InventoryDragEvent e) {

    }
}
