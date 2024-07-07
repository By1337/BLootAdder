package org.by1337.loot.adder;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.configuration.YamlConfig;
import org.by1337.blib.random.WeightedItemSelector;
import org.by1337.loot.adder.loot.loot.CustomLootManager;
import org.by1337.loot.adder.menu.impl.SelectLootTableMenu;
import org.by1337.loot.adder.loot.loot.WeightedItemCount;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class Main extends JavaPlugin {
    private Message message;
    private Main instance;
    private CustomLootManager customLootManager;
    private YamlConfig cfg;
    private Command<CommandSender> command;

    @Override
    public void onLoad() {
        instance = this;
        message = new Message(getLogger());
        getDataFolder().mkdir();
    }

    @Override
    public void onEnable() {
        try {
            cfg = new YamlConfig(trySave("config.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }


        Map<Integer, Double> map = cfg.getMap("rolls", Double.class, Integer.class);
        List<WeightedItemCount> list = new ArrayList<>();
        map.forEach((k, v) -> list.add(new WeightedItemCount(v, k)));
        WeightedItemSelector<WeightedItemCount> dropChances = new WeightedItemSelector<>(list);
        customLootManager = new CustomLootManager(this, false, dropChances);
        command = new Command<CommandSender>("bloot")
                .requires(new RequiresPermission<>("bloot.use"))
                .requires(sender -> sender instanceof Player)
                .executor(((sender, args) -> {
                    Player player = (Player) sender;
                    SelectLootTableMenu selectLootTableMenu = new SelectLootTableMenu(player, customLootManager, this);
                    player.openInventory(selectLootTableMenu.getInventory());
                }));
    }

    @CanIgnoreReturnValue
    public File trySave(String path) {
        path = path.replace('\\', '/');
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        File f = new File(getDataFolder(), path);
        if (!f.exists()) {
            saveResource(path, false);
        }
        return f;
    }

    public Message getMessage() {
        return message;
    }

    public CustomLootManager getCustomLootManager() {
        return customLootManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, @NotNull String[] args) {
        try {
            command.process(sender, args);
        } catch (CommandException e) {
            message.sendMsg(sender, e.getMessage());
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String alias, @NotNull String[] args) {
        return command.getTabCompleter(sender, args);
    }
}
