package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class CachedDirectionInventoryBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<CachedDirectionInventoryBehaviour<?>> TYPE = new BehaviourType<>();
    private final BiFunction<T, Direction, Container> factory;
    @SuppressWarnings("unchecked")
    Container[] sides = new Container[7];

    public CachedDirectionInventoryBehaviour(T be, BiFunction<T, Direction, Container> factory) {
        super(be);
        this.factory = factory;
    }

    public static @Nullable <T extends SmartBlockEntity> Container get(T be, @Nullable Direction side) {
        return be.getBehaviour(TYPE).get(side);
    }

    public Container get(Direction side) {
        int i = side == null ? 6 : side.get3DDataValue();
        Container sideStorage = sides[i];
        if (sideStorage == null) {
            Container inventory = factory.apply(blockEntity, side);
            if (inventory != null) {
                sideStorage = sides[i] = inventory;
            }
        }
        return sideStorage;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
