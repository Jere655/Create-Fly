package com.zurrtum.create.infrastructure.items;

import net.minecraft.world.Container;

/**
 * Compile-time bridge for the BaseInventory methods mixed into vanilla
 * containers. Native handler wrappers implement ItemInventory directly.
 */
public final class InventoryUtil {
    private InventoryUtil() {
    }

    public static BaseInventory of(Container inventory) {
        return (BaseInventory) inventory;
    }
}
