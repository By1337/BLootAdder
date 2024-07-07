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


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MenuEditChance extends AsyncClickListener {
    private final int itemSlots = 45;
    private final Player player;
    private int page = 0;
    private final List<InventoryItem> itemsOnScreen = new ArrayList<>();

    private final Supplier<List<InventoryItem>> itemProvider;
    private final Runnable saveProvider;
    private final Main plugin;

    public MenuEditChance(Player player, Supplier<List<InventoryItem>> itemProvider, Runnable saveProvider, Main plugin) {
        super(player, false, plugin);
        this.player = player;
        this.itemProvider = itemProvider;
        this.saveProvider = saveProvider;
        this.plugin = plugin;
        createInventory(54, plugin.getMessage().messageBuilder("&7Редактирования шансов появления дропа"));
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
        itemsOnScreen.clear();
        for (int i = startPos, slot = 0; i < startPos + itemSlots; i++, slot++) {
            if (list.size() <= i) break;
            var item = list.get(i);
            var itemStack = item.getItemStack();
            itemsOnScreen.add(item);
            inventory.setItem(slot, new ItemBuilder()
                    .lore(
                            "&aШанс появления:&f {chance}",
                            "&aЛКМ&f - +1 к шансу появления",
                            "&aShift + ЛКМ&f - +10 к шансу появления",
                            "&aПКМ&f - -1 к шансу появления",
                            "&aShift + ПКМ&f - -10 к шансу появления"
                    )
                    .replaceLore(item::replace)
                    .build(itemStack, plugin.getMessage())
            );
        }
    }

    @Override
    protected void onClose(InventoryCloseEvent e) {
        saveProvider.run();
    }

    @Override
    protected void onClick(InventoryClickEvent e) {
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
        } else {
            if (e.getSlot() > itemsOnScreen.size() - 1 || e.getSlot() == -999) return;

            var item = itemsOnScreen.get(e.getSlot());
            if (item == null) return;
            switch (e.getClick()) {
                case LEFT -> {
                    int chance = item.getChance() + 1;
                    item.setChance(Math.min(chance, 100));
                    generate();
                }
                case SHIFT_LEFT -> {
                    int chance = item.getChance() + 10;
                    item.setChance(Math.min(chance, 100));
                    generate();
                }
                case RIGHT -> {
                    int chance = item.getChance() - 1;
                    item.setChance(Math.max(chance, 0));
                    generate();
                }
                case SHIFT_RIGHT -> {
                    int chance = item.getChance() - 10;
                    item.setChance(Math.max(chance, 0));
                    generate();
                }
            }

        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    protected void onClick(InventoryDragEvent e) {

    }
}
