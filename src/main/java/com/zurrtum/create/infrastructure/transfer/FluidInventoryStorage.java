package com.zurrtum.create.infrastructure.transfer;

import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Native NeoForge resource-handler view of Create's mutable fluid inventory API.
 */
public interface FluidInventoryStorage extends ResourceHandler<FluidResource> {
    static FluidInventoryStorage of(FluidInventory inventory) {
        return of(inventory, null);
    }

    static FluidInventoryStorage of(FluidInventory inventory, @Nullable Direction direction) {
        Objects.requireNonNull(inventory, "Null fluid inventory is not supported.");
        return FluidInventoryStorageImpl.of(inventory, direction);
    }

    static FluidResource toResource(FluidStack stack) {
        return stack.isEmpty()
            ? FluidResource.EMPTY
            : FluidResource.of(stack.getFluid(), stack.getComponentChanges());
    }

    static FluidStack toStack(FluidResource resource, int amount) {
        return resource.isEmpty() || amount <= 0
            ? FluidStack.EMPTY
            : new FluidStack(resource.getFluid(), amount, resource.getComponentsPatch());
    }
}
