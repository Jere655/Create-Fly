package com.zurrtum.create.infrastructure.transfer;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Mutable, single-item context for querying NeoForge item fluid capabilities.
 * The one-slot container allows capabilities (such as buckets) to exchange the
 * item's identity transactionally, unlike ItemAccess.forStack, which is intended
 * only for items whose type cannot change.
 */
public final class FluidItemContext implements AutoCloseable {
    private final SimpleContainer container;
    private final ItemAccess access;

    private FluidItemContext(ItemStack stack) {
        container = new SimpleContainer(stack.copy());
        ResourceHandler<ItemResource> items = VanillaContainerWrapper.of(container);
        access = ItemAccess.forHandlerIndexStrict(items, 0).oneByOne();
    }

    public static FluidItemContext of(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Expected a non-empty item stack.");
        }
        return new FluidItemContext(stack);
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler() {
        return access.getCapability(Capabilities.Fluid.ITEM);
    }

    public ItemStack getStack() {
        return container.getItem(0);
    }

    @Override
    public void close() {
        // Do not mutate the stored stack here: getContainer() callers may have
        // retained it as the result of a simulated or committed item transfer.
        container.setItem(0, ItemStack.EMPTY, true);
    }
}
