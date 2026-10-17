package com.zurrtum.create.infrastructure.neoforge;

import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.transfer.access.ItemAccess;

/** Native NeoForge fluid capability adapter for Create Fly's fluid inventory API. */
public class FluidHandlerInventory implements FluidInventory {
    protected final IFluidHandler handler;

    public FluidHandlerInventory(IFluidHandler handler) {
        this.handler = handler;
    }

    public static FluidStack fromNeoForge(net.neoforged.neoforge.fluids.FluidStack stack) {
        return stack.isEmpty() ? FluidStack.EMPTY : new FluidStack(stack.getFluid(), stack.getAmount(), stack.getComponentsPatch());
    }

    public static net.neoforged.neoforge.fluids.FluidStack toNeoForge(FluidStack stack) {
        return stack.isEmpty()
            ? net.neoforged.neoforge.fluids.FluidStack.EMPTY
            : new net.neoforged.neoforge.fluids.FluidStack(stack.getFluid(), stack.getAmount(), stack.getComponentChanges());
    }

    @Override
    public FluidStack getStack(int slot) {
        return slot < 0 || slot >= handler.getTanks() ? FluidStack.EMPTY : fromNeoForge(handler.getFluidInTank(slot));
    }

    @Override
    public void setStack(int slot, FluidStack stack) {
        if (slot < 0 || slot >= handler.getTanks()) {
            return;
        }
        net.neoforged.neoforge.fluids.FluidStack nativeStack = toNeoForge(stack);
        net.neoforged.neoforge.fluids.FluidStack current = handler.getFluidInTank(slot);
        if (!current.isEmpty()) {
            handler.drain(current, FluidAction.EXECUTE);
        }
        if (!nativeStack.isEmpty()) {
            handler.fill(nativeStack, FluidAction.EXECUTE);
        }
    }

    @Override
    public int size() {
        return handler.getTanks();
    }

    @Override
    public int getMaxAmount(FluidStack stack) {
        int amount = 0;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            if (handler.isFluidValid(tank, toNeoForge(stack))) {
                amount = Math.max(amount, handler.getTankCapacity(tank));
            }
        }
        return amount;
    }

    @Override
    public boolean isValid(int slot, FluidStack stack) {
        return slot >= 0 && slot < handler.getTanks() && handler.isFluidValid(slot, toNeoForge(stack));
    }

    /** Item capability variant; callers obtain the resulting item from getContainer(). */
    public static final class Item extends FluidHandlerInventory implements com.zurrtum.create.infrastructure.fluids.FluidItemInventory {
        private final ItemAccess access;

        public Item(ItemAccess access, IFluidHandler itemHandler) {
            super(itemHandler);
            this.access = access;
        }

        @Override
        public net.minecraft.world.item.ItemStack getContainer() {
            return access.getResource().toStack(access.getAmount());
        }

        @Override
        public void close() {
            // NeoForge item capabilities commit mutations eagerly.
        }
    }
}
