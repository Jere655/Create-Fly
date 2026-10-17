package com.zurrtum.create.infrastructure.transfer;

import com.google.common.collect.MapMaker;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Presents a native NeoForge fluid resource handler through Create's fluid inventory API.
 * All mutations are performed transactionally, including simulated queries.
 */
public class FluidInventoryWrapper implements FluidInventory {
    private static final Map<ResourceHandler<FluidResource>, FluidInventory> WRAPPERS = new MapMaker()
        .weakKeys()
        .weakValues()
        .makeMap();

    protected ResourceHandler<FluidResource> storage;

    protected FluidInventoryWrapper(ResourceHandler<FluidResource> storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    public static FluidInventory of(ResourceHandler<FluidResource> storage) {
        return WRAPPERS.computeIfAbsent(storage, FluidInventoryWrapper::new);
    }

    @Override
    public void clearContent() {
        try (Transaction transaction = Transaction.openRoot()) {
            for (int i = 0, size = storage.size(); i < size; i++) {
                FluidResource resource = storage.getResource(i);
                int amount = storage.getAmountAsInt(i);
                if (!resource.isEmpty() && amount > 0) {
                    storage.extract(i, resource, amount, transaction);
                }
            }
            transaction.commit();
        }
    }

    @Override
    public int count(FluidStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        FluidResource resource = FluidInventoryStorage.toResource(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            return storage.extract(resource, maxAmount, transaction);
        }
    }

    @Override
    public int count(FluidStack stack) {
        return count(stack, Math.max(0, stack.getAmount()));
    }

    @Override
    public FluidStack count(Predicate<FluidStack> predicate) {
        for (int i = 0, size = storage.size(); i < size; i++) {
            FluidResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0) {
                continue;
            }
            FluidStack candidate = FluidInventoryStorage.toStack(resource, amount);
            if (predicate.test(candidate)) {
                return candidate;
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack count(Predicate<FluidStack> predicate, int maxAmount) {
        if (maxAmount <= 0) {
            return FluidStack.EMPTY;
        }
        for (int i = 0, size = storage.size(); i < size; i++) {
            FluidResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0) {
                continue;
            }
            if (predicate.test(FluidInventoryStorage.toStack(resource, amount))) {
                try (Transaction transaction = Transaction.openRoot()) {
                    int counted = storage.extract(resource, maxAmount, transaction);
                    return FluidInventoryStorage.toStack(resource, counted);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack countAny() {
        for (int i = 0, size = storage.size(); i < size; i++) {
            FluidResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (!resource.isEmpty() && amount > 0) {
                return FluidInventoryStorage.toStack(resource, amount);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack countAny(int maxAmount) {
        if (maxAmount <= 0) {
            return FluidStack.EMPTY;
        }
        for (int i = 0, size = storage.size(); i < size; i++) {
            FluidResource resource = storage.getResource(i);
            if (resource.isEmpty() || storage.getAmountAsInt(i) <= 0) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int counted = storage.extract(resource, maxAmount, transaction);
                if (counted > 0) {
                    return FluidInventoryStorage.toStack(resource, counted);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack countAny(int maxAmount, Direction side) {
        // The wrapped handler is already side-scoped; this query must remain simulated.
        return countAny(maxAmount);
    }

    @Override
    public int countSpace(FluidStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        FluidResource resource = FluidInventoryStorage.toResource(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            return storage.insert(resource, maxAmount, transaction);
        }
    }

    @Override
    public int countSpace(FluidStack stack) {
        return countSpace(stack, Math.max(0, stack.getAmount()));
    }

    @Override
    public boolean countSpace(List<FluidStack> stacks) {
        try (Transaction transaction = Transaction.openRoot()) {
            for (FluidStack stack : stacks) {
                if (stack.isEmpty()) {
                    continue;
                }
                FluidResource resource = FluidInventoryStorage.toResource(stack);
                if (storage.insert(resource, stack.getAmount(), transaction) != stack.getAmount()) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public FluidStack extract(Predicate<FluidStack> predicate) {
        for (int i = 0, size = storage.size(); i < size; i++) {
            FluidResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0) {
                continue;
            }
            FluidStack candidate = FluidInventoryStorage.toStack(resource, amount);
            if (!predicate.test(candidate)) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = storage.extract(i, resource, amount, transaction);
                if (extracted > 0) {
                    transaction.commit();
                    return FluidInventoryStorage.toStack(resource, extracted);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack extract(Predicate<FluidStack> predicate, int maxAmount) {
        if (maxAmount <= 0) {
            return FluidStack.EMPTY;
        }
        for (int i = 0, size = storage.size(); i < size; i++) {
            FluidResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0
                || !predicate.test(FluidInventoryStorage.toStack(resource, amount))) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = storage.extract(resource, maxAmount, transaction);
                if (extracted > 0) {
                    transaction.commit();
                    return FluidInventoryStorage.toStack(resource, extracted);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int extract(FluidStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        FluidResource resource = FluidInventoryStorage.toResource(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = storage.extract(resource, maxAmount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    @Override
    public int extract(FluidStack stack) {
        return extract(stack, Math.max(0, stack.getAmount()));
    }

    @Override
    public FluidStack extractAny() {
        for (int i = 0, size = storage.size(); i < size; i++) {
            FluidResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = storage.extract(i, resource, amount, transaction);
                if (extracted > 0) {
                    transaction.commit();
                    return FluidInventoryStorage.toStack(resource, extracted);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack extractAny(int maxAmount) {
        if (maxAmount <= 0) {
            return FluidStack.EMPTY;
        }
        for (int i = 0, size = storage.size(); i < size; i++) {
            FluidResource resource = storage.getResource(i);
            if (resource.isEmpty() || storage.getAmountAsInt(i) <= 0) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = storage.extract(resource, maxAmount, transaction);
                if (extracted > 0) {
                    transaction.commit();
                    return FluidInventoryStorage.toStack(resource, extracted);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int insert(FluidStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        FluidResource resource = FluidInventoryStorage.toResource(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = storage.insert(resource, maxAmount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public int insert(FluidStack stack) {
        return insert(stack, Math.max(0, stack.getAmount()));
    }

    @Override
    public List<FluidStack> insert(List<FluidStack> stacks) {
        List<FluidStack> leftovers = new ArrayList<>();
        boolean changed = false;
        try (Transaction transaction = Transaction.openRoot()) {
            for (FluidStack stack : stacks) {
                if (stack.isEmpty()) {
                    continue;
                }
                FluidResource resource = FluidInventoryStorage.toResource(stack);
                int requested = stack.getAmount();
                int inserted = storage.insert(resource, requested, transaction);
                changed |= inserted > 0;
                if (inserted < requested) {
                    leftovers.add(FluidInventoryStorage.toStack(resource, requested - inserted));
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
    public int insertExist(FluidStack stack) {
        return insertExist(stack, Math.max(0, stack.getAmount()));
    }

    public int insertExist(FluidStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return 0;
        }
        FluidResource resource = FluidInventoryStorage.toResource(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = storage.insert(resource, maxAmount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public boolean preciseExtract(FluidStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        FluidResource resource = FluidInventoryStorage.toResource(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            if (storage.extract(resource, stack.getAmount(), transaction) != stack.getAmount()) {
                return false;
            }
            transaction.commit();
            return true;
        }
    }

    @Override
    public FluidStack preciseExtract(Predicate<FluidStack> predicate, int maxAmount) {
        if (maxAmount <= 0) {
            return FluidStack.EMPTY;
        }
        for (int i = 0, size = storage.size(); i < size; i++) {
            FluidResource resource = storage.getResource(i);
            int amount = storage.getAmountAsInt(i);
            if (resource.isEmpty() || amount <= 0
                || !predicate.test(FluidInventoryStorage.toStack(resource, amount))) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                if (storage.extract(resource, maxAmount, transaction) == maxAmount) {
                    transaction.commit();
                    return FluidInventoryStorage.toStack(resource, maxAmount);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean preciseInsert(FluidStack stack) {
        return preciseInsert(stack, Math.max(0, stack.getAmount()));
    }

    @Override
    public boolean preciseInsert(FluidStack stack, int maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) {
            return maxAmount == 0;
        }
        FluidResource resource = FluidInventoryStorage.toResource(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            if (storage.insert(resource, maxAmount, transaction) != maxAmount) {
                return false;
            }
            transaction.commit();
            return true;
        }
    }

    @Override
    public boolean preciseInsert(List<FluidStack> stacks) {
        try (Transaction transaction = Transaction.openRoot()) {
            for (FluidStack stack : stacks) {
                if (stack.isEmpty()) {
                    continue;
                }
                FluidResource resource = FluidInventoryStorage.toResource(stack);
                if (storage.insert(resource, stack.getAmount(), transaction) != stack.getAmount()) {
                    return false;
                }
            }
            transaction.commit();
            return true;
        }
    }

    @Override
    public FluidStack removeStack(int slot) {
        FluidResource resource = storage.getResource(slot);
        int amount = storage.getAmountAsInt(slot);
        if (resource.isEmpty() || amount <= 0) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = storage.extract(slot, resource, amount, transaction);
            transaction.commit();
            return FluidInventoryStorage.toStack(resource, extracted);
        }
    }

    @Override
    public FluidStack removeStack(int slot, int amount) {
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        FluidResource resource = storage.getResource(slot);
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = storage.extract(slot, resource, amount, transaction);
            transaction.commit();
            return FluidInventoryStorage.toStack(resource, extracted);
        }
    }

    @Override
    public void setStack(int slot, FluidStack stack) {
        FluidResource resource = FluidInventoryStorage.toResource(stack);
        int amount = stack.isEmpty() ? 0 : stack.getAmount();
        try (Transaction transaction = Transaction.openRoot()) {
            if (replaceSlot(slot, resource, amount, transaction)) {
                transaction.commit();
            }
        }
    }

    @Override
    public FluidStack getStack(int slot) {
        return FluidInventoryStorage.toStack(storage.getResource(slot), storage.getAmountAsInt(slot));
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
    public int size() {
        return storage.size();
    }

    @Override
    public int getMaxAmountPerStack() {
        int capacity = 0;
        for (int i = 0, size = storage.size(); i < size; i++) {
            capacity = Math.max(capacity, storage.getCapacityAsInt(i, FluidResource.EMPTY));
        }
        return capacity;
    }

    @Override
    public int getMaxAmount(FluidStack stack) {
        if (stack.isEmpty()) {
            return getMaxAmountPerStack();
        }
        FluidResource resource = FluidInventoryStorage.toResource(stack);
        int capacity = 0;
        for (int i = 0, size = storage.size(); i < size; i++) {
            capacity = Math.max(capacity, storage.getCapacityAsInt(i, resource));
        }
        return Math.min(capacity, stack.getMaxAmount());
    }

    @Override
    public boolean isValid(int slot, FluidStack stack) {
        return !stack.isEmpty() && storage.isValid(slot, FluidInventoryStorage.toResource(stack));
    }

    @Override
    public boolean matches(FluidStack stack, FluidStack otherStack) {
        return FluidStack.areFluidsAndComponentsEqual(stack, otherStack);
    }

    @Override
    public void markDirty() {
        // NeoForge ResourceHandlers defer their irreversible dirty notification to root commit.
    }

    @Override
    public Stream<FluidStack> stream() {
        return FluidInventory.super.stream();
    }

    protected boolean replaceSlot(
        int slot,
        FluidResource replacement,
        int replacementAmount,
        Transaction transaction
    ) {
        FluidResource current = storage.getResource(slot);
        int currentAmount = storage.getAmountAsInt(slot);
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
