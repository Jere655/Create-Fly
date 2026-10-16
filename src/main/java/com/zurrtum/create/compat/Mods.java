package com.zurrtum.create.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.LoadingModList;

import java.util.Locale;

/**
 * For compatibility with and without another mod present, we have to define load conditions of the specific code
 */
public enum Mods {
    COMPUTERCRAFT, TRINKETS, PACKETFIXER, MODERNUI;

    private final String id;
    private final boolean loaded;

    Mods() {
        id = name().toLowerCase(Locale.ROOT);
        loaded = LoadingModList.get().getModFileById(id) != null;
    }

    /**
     * @return the mod id
     */
    public String id() {
        return id;
    }

    public Identifier identifier(String name) {
        return Identifier.fromNamespaceAndPath(id, name);
    }

    public Block getBlock(String id) {
        return BuiltInRegistries.BLOCK.getValue(identifier(id));
    }

    public Item getItem(String id) {
        return BuiltInRegistries.ITEM.getValue(identifier(id));
    }

    /**
     * @return a boolean of whether the mod is loaded or not based on mod id
     */
    public boolean isLoaded() {
        return loaded;
    }
}
