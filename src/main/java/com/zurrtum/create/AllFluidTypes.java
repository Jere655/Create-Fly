package com.zurrtum.create;

import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class AllFluidTypes {
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(
        NeoForgeRegistries.FLUID_TYPES,
        Create.MOD_ID
    );

    public static final DeferredHolder<FluidType, FluidType> POTION = register("potion");
    public static final DeferredHolder<FluidType, FluidType> TEA = register("tea");
    public static final DeferredHolder<FluidType, FluidType> MILK = register("milk");
    public static final DeferredHolder<FluidType, FluidType> HONEY = register("honey");
    public static final DeferredHolder<FluidType, FluidType> CHOCOLATE = register("chocolate");

    private AllFluidTypes() {
    }

    private static DeferredHolder<FluidType, FluidType> register(String name) {
        return FLUID_TYPES.register(name, () -> new FluidType(FluidType.Properties.create()
            .descriptionId("fluid.create." + name)
            .fallDistanceModifier(0)
            .canExtinguish(true)
            .canConvertToSource(false)
            .supportsBoating(true)
            .pathType(PathType.WATER)
            .adjacentPathType(PathType.WATER_BORDER)
            .canHydrate(true)
            .isWaterLike(true)));
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
    }
}
