package com.zurrtum.create;

import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllFluids {
    public static final FlowableFluid POTION = register("potion", AllFluidTypes.POTION);
    public static final FlowableFluid TEA = register("tea", AllFluidTypes.TEA);
    public static final FlowableFluid MILK = register("milk", AllFluidTypes.MILK);
    public static final FlowableFluid HONEY = register("honey", AllFluidTypes.HONEY);
    public static final FlowableFluid CHOCOLATE = register("chocolate", AllFluidTypes.CHOCOLATE);

    private static FlowableFluid register(String name, Supplier<FluidType> fluidType) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        ResourceKey<Fluid> stillKey = ResourceKey.create(Registries.FLUID, id);
        ResourceKey<Fluid> flowingKey = ResourceKey.create(Registries.FLUID, id.withPrefix("flowing_"));
        FluidEntry entry = new FluidEntry(fluidType);
        entry.still = new FlowableFluid.Still(entry);
        entry.flowing = new FlowableFluid.Flowing(entry);
        Registry.register(BuiltInRegistries.FLUID, stillKey, entry.still);
        Registry.register(BuiltInRegistries.FLUID, flowingKey, entry.flowing);
        return entry.still;
    }

    public static void register() {
    }
}
