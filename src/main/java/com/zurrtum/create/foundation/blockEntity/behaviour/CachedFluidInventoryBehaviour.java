package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import com.zurrtum.create.infrastructure.transfer.FluidInventoryStorage;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CachedFluidInventoryBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<CachedFluidInventoryBehaviour<?>> TYPE = new BehaviourType<>();
    private final Function<T, FluidInventory> factory;
    private Function<Direction, ResourceHandler<FluidResource>> getter;

    public CachedFluidInventoryBehaviour(T be, Function<T, FluidInventory> factory) {
        super(be);
        this.factory = factory;
        reset();
    }

    public static @Nullable <T extends SmartBlockEntity> ResourceHandler<FluidResource> get(
        T be,
        @Nullable Direction side
    ) {
        return be.getBehaviour(TYPE).get(side);
    }

    public @Nullable ResourceHandler<FluidResource> get(@Nullable Direction side) {
        return getter.apply(side);
    }

    @SuppressWarnings("unchecked")
    private @Nullable ResourceHandler<FluidResource> firstGet(Direction direction) {
        FluidInventory inventory = factory.apply(blockEntity);
        if (inventory == null) {
            return null;
        }

        ResourceHandler<FluidResource> allSlots = FluidInventoryStorage.of(inventory, null);
        if (inventory instanceof SidedFluidInventory) {
            ResourceHandler<FluidResource>[] sides = new ResourceHandler[6];
            getter = side -> {
                if (side == null) {
                    return allSlots;
                }
                int index = side.get3DDataValue();
                ResourceHandler<FluidResource> handler = sides[index];
                if (handler == null) {
                    handler = sides[index] = FluidInventoryStorage.of(inventory, side);
                }
                return handler;
            };
        } else {
            getter = side -> allSlots;
        }
        return getter.apply(direction);
    }

    public void reset() {
        getter = this::firstGet;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
