package com.zurrtum.create.infrastructure.neoforge;

import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Publishes Create Fly's fluid inventories through NeoForge's transactional fluid capability. */
public final class FluidInventoryHandler implements ResourceHandler<FluidResource> {
    private final FluidInventory inventory;
    private final @Nullable Direction side;
    private final SnapshotJournal<List<FluidStack>> journal = new SnapshotJournal<>() {
        @Override
        protected List<FluidStack> createSnapshot() {
            List<FluidStack> snapshot = new ArrayList<>(inventory.size());
            for (int slot = 0; slot < inventory.size(); slot++) {
                snapshot.add(inventory.getStack(slot).copy());
            }
            return snapshot;
        }

        @Override
        protected void revertToSnapshot(List<FluidStack> snapshot) {
            for (int slot = 0; slot < snapshot.size(); slot++) {
                inventory.setStack(slot, snapshot.get(slot));
            }
        }

        @Override
        protected void onRootCommit(List<FluidStack> originalState) {
            inventory.markDirty();
        }
    };

    public FluidInventoryHandler(FluidInventory inventory, @Nullable Direction side) {
        this.inventory = inventory;
        this.side = side;
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public FluidResource getResource(int slot) {
        return slot < 0 || slot >= inventory.size()
            ? FluidResource.EMPTY
            : FluidResource.of(toNeoForge(inventory.getStack(slot)));
    }

    @Override
    public long getAmountAsLong(int slot) {
        return slot < 0 || slot >= inventory.size() ? 0 : inventory.getStack(slot).getAmount();
    }

    @Override
    public long getCapacityAsLong(int slot, FluidResource resource) {
        if (slot < 0 || slot >= inventory.size()) {
            return 0;
        }
        FluidStack stack = resource.isEmpty() ? FluidStack.EMPTY : fromNeoForge(resource.toStack(1));
        return stack.isEmpty() ? inventory.getMaxAmountPerStack() : inventory.getMaxAmount(stack);
    }

    @Override
    public boolean isValid(int slot, FluidResource resource) {
        if (slot < 0 || slot >= inventory.size() || resource.isEmpty()) {
            return false;
        }
        FluidStack stack = fromNeoForge(resource.toStack(1));
        return inventory.isValid(slot, stack) && (!(inventory instanceof com.zurrtum.create.infrastructure.fluids.SidedFluidInventory sided)
            || sided.canInsert(slot, stack, side));
    }

    @Override
    public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
        if (slot < 0 || slot >= inventory.size() || resource.isEmpty() || amount <= 0 || !isValid(slot, resource)) {
            return 0;
        }
        FluidStack inserted = fromNeoForge(resource.toStack(amount));
        FluidStack current = inventory.getStack(slot);
        if (!current.isEmpty() && !inventory.matches(current, inserted)) {
            return 0;
        }
        int accepted = Math.min(amount, (int) getCapacityAsLong(slot, resource) - current.getAmount());
        if (accepted <= 0) {
            return 0;
        }
        journal.updateSnapshots(transaction);
        inventory.setStack(slot, inserted.directCopy(current.getAmount() + accepted));
        return accepted;
    }

    @Override
    public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
        if (slot < 0 || slot >= inventory.size() || resource.isEmpty() || amount <= 0) {
            return 0;
        }
        FluidStack current = inventory.getStack(slot);
        if (current.isEmpty() || !getResource(slot).equals(resource)) {
            return 0;
        }
        if (inventory instanceof com.zurrtum.create.infrastructure.fluids.SidedFluidInventory sided
            && !sided.canExtract(slot, current, side)) {
            return 0;
        }
        int extracted = Math.min(amount, current.getAmount());
        journal.updateSnapshots(transaction);
        inventory.setStack(slot, extracted == current.getAmount() ? FluidStack.EMPTY : current.directCopy(current.getAmount() - extracted));
        return extracted;
    }

    private static FluidStack fromNeoForge(net.neoforged.neoforge.fluids.FluidStack stack) {
        return stack.isEmpty() ? FluidStack.EMPTY : new FluidStack(stack.getFluid(), stack.getAmount(), stack.getComponentsPatch());
    }

    private static net.neoforged.neoforge.fluids.FluidStack toNeoForge(FluidStack stack) {
        return stack.isEmpty()
            ? net.neoforged.neoforge.fluids.FluidStack.EMPTY
            : new net.neoforged.neoforge.fluids.FluidStack(stack.getFluid(), stack.getAmount(), stack.getComponentChanges());
    }
}
