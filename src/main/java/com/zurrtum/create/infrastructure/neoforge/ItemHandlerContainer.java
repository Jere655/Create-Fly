package com.zurrtum.create.infrastructure.neoforge;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/** Exposes a NeoForge item capability to Create Fly code that consumes Containers. */
public final class ItemHandlerContainer implements Container {
    private final IItemHandler handler;

    public ItemHandlerContainer(IItemHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getContainerSize() {
        return handler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot < 0 || slot >= handler.getSlots() ? ItemStack.EMPTY : handler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return slot < 0 || slot >= handler.getSlots() ? ItemStack.EMPTY : handler.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return slot < 0 || slot >= handler.getSlots()
            ? ItemStack.EMPTY
            : handler.extractItem(slot, handler.getStackInSlot(slot).getCount(), false);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= handler.getSlots()) {
            return;
        }
        if (handler instanceof IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(slot, stack);
            return;
        }
        ItemStack current = handler.getStackInSlot(slot);
        if (!current.isEmpty()) {
            handler.extractItem(slot, current.getCount(), false);
        }
        if (!stack.isEmpty()) {
            handler.insertItem(slot, stack, false);
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            setItem(slot, ItemStack.EMPTY);
        }
    }
}
