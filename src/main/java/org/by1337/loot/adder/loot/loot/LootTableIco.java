package org.by1337.loot.adder.loot.loot;

import org.bukkit.Material;
import org.bukkit.loot.LootTables;
import org.by1337.blib.nbt.impl.CompoundTag;

public class LootTableIco {
    private final Material ico;
    private final String name;
    private final LootTables lootTables;

    public LootTableIco(Material ico, String name, LootTables lootTables) {
        this.ico = ico;
        this.name = name;
        this.lootTables = lootTables;
    }

    public LootTableIco(CompoundTag compoundTag, LootTables lootTables) {
        this.lootTables = lootTables;
        ico = Material.valueOf(compoundTag.getAsString("ico"));
        name = "&7" + compoundTag.getAsString("name");
        if (!compoundTag.getAsString("id").equals(lootTables.getKey().getKey())){
            throw new IllegalStateException(compoundTag.toString());
        }
    }

    public Material getIco() {
        return ico;
    }

    public String getName() {
        return name;
    }

    public LootTables getLootTables() {
        return lootTables;
    }
}
