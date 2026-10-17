package com.zurrtum.create.infrastructure.fluids;

import net.minecraft.world.item.BucketItem;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Supplier;

public class FluidEntry {
    public final Supplier<FluidType> fluidType;
    public FlowableFluid flowing = null;
    public FlowableFluid still = null;
    public BucketItem bucket = null;
    public FluidBlock block = null;

    public FluidEntry(Supplier<FluidType> fluidType) {
        this.fluidType = fluidType;
    }
}
