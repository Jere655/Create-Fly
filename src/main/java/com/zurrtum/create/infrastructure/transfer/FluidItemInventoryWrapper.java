package com.zurrtum.create.infrastructure.transfer;

import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/** FluidInventory adapter that also exposes the item container backing its ItemAccess. */
public final class FluidItemInventoryWrapper extends FluidInventoryWrapper implements FluidItemInventory {
    private FluidItemContext context;

    private FluidItemInventoryWrapper(ResourceHandler<FluidResource> storage, FluidItemContext context) {
        super(storage);
        this.context = context;
    }

    public static FluidItemInventory of(ResourceHandler<FluidResource> storage, FluidItemContext context) {
        return new FluidItemInventoryWrapper(storage, context);
    }

    @Override
    public void close() {
        if (context != null) {
            context.close();
            context = null;
        }
        storage = null;
    }

    @Override
    public ItemStack getContainer() {
        return context == null ? ItemStack.EMPTY : context.getStack().copy();
    }

    @Override
    public boolean isEmpty() {
        return context == null || context.getStack().isEmpty();
    }

    @Override
    public int insert(FluidStack stack) {
        return context == null ? 0 : super.insert(stack);
    }

    @Override
    public int insert(FluidStack stack, int maxAmount) {
        return context == null ? 0 : super.insert(stack, maxAmount);
    }

    @Override
    public int extract(FluidStack stack) {
        return context == null ? 0 : super.extract(stack);
    }

    @Override
    public int extract(FluidStack stack, int maxAmount) {
        return context == null ? 0 : super.extract(stack, maxAmount);
    }
}
