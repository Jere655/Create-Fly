package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.transfer.InventoryWrapper;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class CachedDirectionInventoryBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<CachedDirectionInventoryBehaviour<?>> TYPE = new BehaviourType<>();
    private final BiFunction<T, Direction, Container> factory;
    @SuppressWarnings("unchecked")
    private final ResourceHandler<ItemResource>[] sides = new ResourceHandler[7];

    public CachedDirectionInventoryBehaviour(T be, BiFunction<T, Direction, Container> factory) {
        super(be);
        this.factory = factory;
    }

    public static @Nullable <T extends SmartBlockEntity> ResourceHandler<ItemResource> get(
        T be,
        @Nullable Direction side
    ) {
        return be.getBehaviour(TYPE).get(side);
    }

    public @Nullable ResourceHandler<ItemResource> get(@Nullable Direction side) {
        int index = side == null ? 6 : side.get3DDataValue();
        ResourceHandler<ItemResource> handler = sides[index];
        if (handler == null) {
            Container inventory = factory.apply(blockEntity, side);
            if (inventory != null) {
                handler = inventory instanceof InventoryWrapper wrapper
                    ? wrapper.resourceHandler()
                    : VanillaContainerWrapper.of(inventory);
                sides[index] = handler;
            }
        }
        return handler;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
