package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CachedFluidInventoryBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<CachedFluidInventoryBehaviour<?>> TYPE = new BehaviourType<>();
    private final Function<T, FluidInventory> factory;
    private Function<Direction, FluidInventory> getter;

    public CachedFluidInventoryBehaviour(T be, Function<T, FluidInventory> factory) {
        super(be);
        this.factory = factory;
        reset();
    }

    public static @Nullable <T extends SmartBlockEntity> FluidInventory get(T be, @Nullable Direction side) {
        return be.getBehaviour(TYPE).get(side);
    }

    public FluidInventory get(Direction side) {
        return getter.apply(side);
    }

    private FluidInventory firstGet(Direction direction) {
        FluidInventory inventory = factory.apply(blockEntity);
        getter = side -> inventory;
        return inventory;
    }

    public void reset() {
        getter = this::firstGet;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
