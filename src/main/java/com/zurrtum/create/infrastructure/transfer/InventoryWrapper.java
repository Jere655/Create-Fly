package com.zurrtum.create.infrastructure.transfer;

import com.google.common.collect.MapMaker;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Presents a native NeoForge item resource handler as a vanilla/Create container.
 * Reads return detached ItemStacks; all writes use NeoForge transactions so native
 * sided rules, validity checks, component identity, and rollback are retained.
 */
public final class InventoryWrapper implements ItemInventory {
    private static final Map<ResourceHandler<ItemResource>, Container> WRAPPERS = new MapMaker()
        .weakKeys()
        .weakValues()
        .makeMap();

    private final ResourceHandler<ItemResource> storage;

    private InventoryWrapper(ResourceHandler<ItemResource> storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    public static Container of(ResourceHandler<ItemResource> storage) {
        return WRAPPERS.computeIfAbsent(storage, InventoryWrapper::new);
    }

    public ResourceHandler<ItemResource> resourceHandler() {
        return storage;
    }

    @Override
    public int getContainerSize() {
        return storage.size();
    }

    @Override
    public ItemStack getItem(int slot) {
        return storage.getResource(slot).toStack(storage.getAmountAsInt(slot));
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        try (Transaction transaction = Transaction.openRoot()) {
            if (replaceSlot(slot, stack, transaction)) {
                transaction.commit();
            }
        }
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemResource resource = storage.getResource(slot);
        int stored = storage.getAmountAsInt(slot);
        if (resource.isEmpty() || stored <= 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = storage.extract(slot, resource, Math.min(amount, stored), transaction);
            if (extracted > 0) {
                transaction.commit();
                return onExtract(resource.toStack(extracted));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemResource resource = storage.getResource(slot);
        int stored = storage.getAmountAsInt(slot);
        if (resource.isEmpty() || stored <= 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = storage.extract(slot, resource, stored, transaction);
            if (extracted > 0) {
                transaction.commit();
                return onExtract(resource.toStack(extracted));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getMaxStackSize() {
        int capacity = 0;
        for (int i = 0, size = storage.size(); i < size; i++) {
            capacity = Math.max(capacity, storage.getCapacityAsInt(i, ItemResource.EMPTY));
        }
        return capacity == 0 ? 64 : capacity;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemResource resource = ItemResource.of(stack);
        return storage.isValid(slot, resource) && simulateInsert(slot, resource, 1) > 0;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0, size = storage.size(); i < size; i++) {
            if (!storage.getResource(i).isEmpty() && storage.getAmountAsInt(i) > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clearContent() {
        try (Transaction transaction = Transaction.openRoot()) {
            for (int i = 0, size = storage.size(); i < size; i++) {
                ItemResource resource = storage.getResource(i);
                int amount = storage.getAmountAsInt(i);
                if (!resource.isEmpty() && amount > 0) {
                    storage.extract(i, resource, amount, transaction);
                }
            }
            transaction.commit();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void setChanged() {
        // Native resource handlers publish dirty notifications on root commit.
    }

    @Override
    public int count(ItemStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        ItemResource resource = ItemResource.of(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            return storage.extract(resource, maxAmount, transaction);
        }
    }

    @Override
    public int count(ItemStack stack) {
        return count(stack, Math.max(0, stack.getCount()));
    }

    @Override
    public int countSpace(ItemStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        ItemResource resource = ItemResource.of(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            return storage.insert(resource, maxAmount, transaction);
        }
    }

    @Override
    public int countSpace(ItemStack stack) {
        return countSpace(stack, Math.max(0, stack.getCount()));
    }

    @Override
    public int countSpace(ItemStack stack, int maxAmount, int start, int end) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        ItemResource resource = ItemResource.of(stack);
        int remaining = maxAmount;
        try (Transaction transaction = Transaction.openRoot()) {
            for (int i = Math.max(0, start), size = Math.min(end, storage.size() - 1); i <= size && remaining > 0; i++) {
                remaining -= storage.insert(i, resource, remaining, transaction);
            }
            return maxAmount - remaining;
        }
    }

    @Override
    public boolean countSpace(List<ItemStack> stacks) {
        try (Transaction transaction = Transaction.openRoot()) {
            for (ItemStack stack : stacks) {
                if (stack.isEmpty()) {
                    continue;
                }
                ItemResource resource = ItemResource.of(stack);
                if (storage.insert(resource, stack.getCount(), transaction) != stack.getCount()) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean countSpace(List<ItemStack> stacks, int start, int end) {
        int[] remaining = new int[stacks.size()];
        for (int i = 0; i < stacks.size(); i++) {
            remaining[i] = stacks.get(i).isEmpty() ? 0 : stacks.get(i).getCount();
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int last = Math.min(end, storage.size() - 1);
            for (int slot = Math.max(0, start); slot <= last; slot++) {
                ItemResource current = storage.getResource(slot);
                int currentAmount = storage.getAmountAsInt(slot);
                for (int index = 0; index < stacks.size(); index++) {
                    int requested = remaining[index];
                    if (requested <= 0) {
                        continue;
                    }
                    ItemStack stack = stacks.get(index);
                    ItemResource resource = ItemResource.of(stack);
                    if (!current.isEmpty() && currentAmount > 0 && !current.equals(resource)) {
                        continue;
                    }
                    int inserted = storage.insert(slot, resource, requested, transaction);
                    remaining[index] -= inserted;
                    if (inserted > 0) {
                        current = resource;
                        currentAmount += inserted;
                    }
                }
            }
            for (int amount : remaining) {
                if (amount > 0) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean matches(ItemStack stack, ItemStack otherStack) {
        return !stack.isEmpty() && !otherStack.isEmpty()
            && ItemResource.of(stack).equals(ItemResource.of(otherStack));
    }

    @Override
    public int extract(ItemStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        ItemResource resource = ItemResource.of(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = storage.extract(resource, maxAmount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    @Override
    public ItemStack extract(Predicate<ItemStack> predicate) {
        for (int i = 0, size = storage.size(); i < size; i++) {
            ItemResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0 || !predicate.test(resource.toStack(amount))) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = storage.extract(i, resource, amount, transaction);
                if (extracted > 0) {
                    transaction.commit();
                    return onExtract(resource.toStack(extracted));
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extract(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount <= 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = storage.size(); i < size; i++) {
            ItemResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0 || !predicate.test(resource.toStack(amount))) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = storage.extract(resource, maxAmount, transaction);
                if (extracted > 0) {
                    transaction.commit();
                    return onExtract(resource.toStack(extracted));
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> extract(List<ItemStack> stacks) {
        List<ItemStack> leftovers = new ArrayList<>();
        boolean changed = false;
        try (Transaction transaction = Transaction.openRoot()) {
            for (ItemStack stack : stacks) {
                if (stack.isEmpty()) {
                    continue;
                }
                ItemResource resource = ItemResource.of(stack);
                int requested = stack.getCount();
                int extracted = storage.extract(resource, requested, transaction);
                changed |= extracted > 0;
                if (extracted < requested) {
                    leftovers.add(resource.toStack(requested - extracted));
                }
            }
            if (changed) {
                transaction.commit();
            }
        }
        if (!changed) {
            return stacks;
        }
        return leftovers;
    }

    @Override
    public int extractAll(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount <= 0) {
            return 0;
        }
        int remaining = maxAmount;
        try (Transaction transaction = Transaction.openRoot()) {
            for (int i = 0, size = storage.size(); i < size && remaining > 0; i++) {
                ItemResource resource = storage.getResource(i);
                int amount = storage.getAmountAsInt(i);
                if (resource.isEmpty() || amount <= 0 || !predicate.test(resource.toStack(amount))) {
                    continue;
                }
                remaining -= storage.extract(i, resource, remaining, transaction);
            }
            transaction.commit();
        }
        return maxAmount - remaining;
    }

    @Override
    public ItemStack countAny(int maxAmount, Direction side) {
        // This handler is already side-scoped; countAny is a simulation, not an extraction.
        return countAny(maxAmount);
    }

    @Override
    public ItemStack extractAny() {
        for (int i = 0, size = storage.size(); i < size; i++) {
            ItemResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = storage.extract(i, resource, amount, transaction);
                if (extracted > 0) {
                    transaction.commit();
                    return onExtract(resource.toStack(extracted));
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractAny(int maxAmount) {
        if (maxAmount <= 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = storage.size(); i < size; i++) {
            ItemResource resource = storage.getResource(i);
            if (resource.isEmpty() || storage.getAmountAsInt(i) <= 0) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = storage.extract(resource, maxAmount, transaction);
                if (extracted > 0) {
                    transaction.commit();
                    return onExtract(resource.toStack(extracted));
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractAnyMax() {
        for (int i = 0, size = storage.size(); i < size; i++) {
            ItemResource resource = storage.getResource(i);
            if (resource.isEmpty() || storage.getAmountAsInt(i) <= 0) {
                continue;
            }
            int maxAmount = resource.toStack().getMaxStackSize();
            return extractResource(resource, maxAmount);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractMax(Predicate<ItemStack> predicate) {
        for (int i = 0, size = storage.size(); i < size; i++) {
            ItemResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0 || !predicate.test(resource.toStack(amount))) {
                continue;
            }
            return extractResource(resource, resource.toStack().getMaxStackSize());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean preciseExtract(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemResource resource = ItemResource.of(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            if (storage.extract(resource, stack.getCount(), transaction) != stack.getCount()) {
                return false;
            }
            transaction.commit();
            return true;
        }
    }

    @Override
    public ItemStack preciseExtract(Predicate<ItemStack> predicate, int maxAmount) {
        if (maxAmount <= 0) {
            return ItemStack.EMPTY;
        }
        for (int i = 0, size = storage.size(); i < size; i++) {
            ItemResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0 || !predicate.test(resource.toStack(amount))) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                if (storage.extract(resource, maxAmount, transaction) == maxAmount) {
                    transaction.commit();
                    return onExtract(resource.toStack(maxAmount));
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int insert(ItemStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        ItemResource resource = ItemResource.of(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = storage.insert(resource, maxAmount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public List<ItemStack> insert(List<ItemStack> stacks) {
        List<ItemStack> leftovers = new ArrayList<>();
        boolean changed = false;
        try (Transaction transaction = Transaction.openRoot()) {
            for (ItemStack stack : stacks) {
                if (stack.isEmpty()) {
                    continue;
                }
                ItemResource resource = ItemResource.of(stack);
                int requested = stack.getCount();
                int inserted = storage.insert(resource, requested, transaction);
                changed |= inserted > 0;
                if (inserted < requested) {
                    leftovers.add(resource.toStack(requested - inserted));
                }
            }
            if (changed) {
                transaction.commit();
            }
        }
        if (!changed) {
            return stacks;
        }
        return leftovers;
    }

    @Override
    public int insert(ItemStack stack, int maxAmount, int start, int end) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        ItemResource resource = ItemResource.of(stack);
        int remaining = maxAmount;
        try (Transaction transaction = Transaction.openRoot()) {
            for (int i = Math.max(0, start), last = Math.min(end, storage.size() - 1); i <= last && remaining > 0; i++) {
                remaining -= storage.insert(i, resource, remaining, transaction);
            }
            transaction.commit();
        }
        return maxAmount - remaining;
    }

    @Override
    public List<ItemStack> insert(List<ItemStack> stacks, int start, int end) {
        List<ItemStack> leftovers = new ArrayList<>();
        boolean changed = false;
        int last = Math.min(end, storage.size() - 1);
        try (Transaction transaction = Transaction.openRoot()) {
            for (ItemStack stack : stacks) {
                if (stack.isEmpty()) {
                    continue;
                }
                ItemResource resource = ItemResource.of(stack);
                int remaining = stack.getCount();
                for (int slot = Math.max(0, start); slot <= last && remaining > 0; slot++) {
                    int inserted = storage.insert(slot, resource, remaining, transaction);
                    remaining -= inserted;
                    changed |= inserted > 0;
                }
                if (remaining > 0) {
                    leftovers.add(resource.toStack(remaining));
                }
            }
            if (changed) {
                transaction.commit();
            }
        }
        if (!changed) {
            return stacks;
        }
        return leftovers;
    }

    @Override
    public int insertExist(ItemStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        ItemResource resource = ItemResource.of(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = storage.insert(resource, maxAmount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public boolean preciseInsert(ItemStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return maxAmount == 0;
        }
        ItemResource resource = ItemResource.of(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            if (storage.insert(resource, maxAmount, transaction) != maxAmount) {
                return false;
            }
            transaction.commit();
            return true;
        }
    }

    @Override
    public boolean preciseInsert(List<ItemStack> stacks) {
        try (Transaction transaction = Transaction.openRoot()) {
            for (ItemStack stack : stacks) {
                if (stack.isEmpty()) {
                    continue;
                }
                ItemResource resource = ItemResource.of(stack);
                if (storage.insert(resource, stack.getCount(), transaction) != stack.getCount()) {
                    return false;
                }
            }
            transaction.commit();
            return true;
        }
    }

    @Override
    public boolean update(Predicate<ItemStack> predicate, Function<ItemStack, ItemStack> update) {
        for (int i = 0, size = storage.size(); i < size; i++) {
            ItemResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0) {
                continue;
            }
            ItemStack current = resource.toStack(amount);
            if (!predicate.test(current)) {
                continue;
            }
            ItemStack replacement = Objects.requireNonNull(update.apply(current.copy()));
            try (Transaction transaction = Transaction.openRoot()) {
                if (replaceSlot(i, replacement, transaction)) {
                    transaction.commit();
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public int create$size() {
        return storage.size();
    }

    @Override
    public ItemStack create$getStack(int slot) {
        return getItem(slot);
    }

    @Override
    public void create$setStack(int slot, ItemStack stack) {
        setItem(slot, stack);
    }

    @Override
    public int create$getMaxCount(ItemStack stack) {
        return stack.isEmpty() ? getMaxStackSize() : maxCapacity(ItemResource.of(stack));
    }

    @Override
    public boolean create$isValid(int slot, ItemStack stack) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public void create$markDirty() {
        // All write paths in this adapter are committed directly through ResourceHandler transactions.
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return IntStream.range(0, storage.size()).mapToObj(this::getItem).iterator();
    }

    @Override
    public Stream<ItemStack> stream() {
        return IntStream.range(0, storage.size()).mapToObj(this::getItem);
    }

    private ItemStack extractResource(ItemResource resource, int maxAmount) {
        if (maxAmount <= 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = storage.extract(resource, maxAmount, transaction);
            if (extracted > 0) {
                transaction.commit();
                return onExtract(resource.toStack(extracted));
            }
        }
        return ItemStack.EMPTY;
    }

    private int maxCapacity(ItemResource resource) {
        int capacity = 0;
        for (int i = 0, size = storage.size(); i < size; i++) {
            capacity = Math.max(capacity, storage.getCapacityAsInt(i, resource));
        }
        return capacity;
    }

    private int simulateInsert(int slot, ItemResource resource, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            return storage.insert(slot, resource, amount, transaction);
        }
    }

    private boolean replaceSlot(int slot, ItemStack stack, Transaction transaction) {
        ItemResource current = storage.getResource(slot);
        int currentAmount = storage.getAmountAsInt(slot);
        ItemResource replacement = ItemResource.of(stack);
        int replacementAmount = stack.isEmpty() ? 0 : stack.getCount();

        if (current.equals(replacement)) {
            if (replacementAmount == currentAmount) {
                return true;
            }
            if (replacementAmount < currentAmount) {
                return storage.extract(slot, current, currentAmount - replacementAmount, transaction)
                    == currentAmount - replacementAmount;
            }
            return storage.insert(slot, replacement, replacementAmount - currentAmount, transaction)
                == replacementAmount - currentAmount;
        }

        if (!current.isEmpty() && currentAmount > 0
            && storage.extract(slot, current, currentAmount, transaction) != currentAmount) {
            return false;
        }
        if (replacement.isEmpty() || replacementAmount <= 0) {
            return true;
        }
        return storage.insert(slot, replacement, replacementAmount, transaction) == replacementAmount;
    }

}
