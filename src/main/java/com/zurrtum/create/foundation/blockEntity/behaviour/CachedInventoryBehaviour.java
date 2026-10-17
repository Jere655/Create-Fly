package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.transfer.InventoryWrapper;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CachedInventoryBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<CachedInventoryBehaviour<?>> TYPE = new BehaviourType<>();
    private final Function<T, Container> factory;
    private Function<Direction, ResourceHandler<ItemResource>> getter;

    public CachedInventoryBehaviour(T be, Function<T, Container> factory) {
        super(be);
        this.factory = factory;
        reset();
    }

    public static @Nullable <T extends SmartBlockEntity> ResourceHandler<ItemResource> get(
        T be,
        @Nullable Direction side
    ) {
        return be.getBehaviour(TYPE).get(side);
    }

    public @Nullable ResourceHandler<ItemResource> get(@Nullable Direction side) {
        return getter.apply(side);
    }

    @SuppressWarnings("unchecked")
    private @Nullable ResourceHandler<ItemResource> firstGet(Direction direction) {
        Container inventory = factory.apply(blockEntity);
        if (inventory == null) {
            return null;
        }

        ResourceHandler<ItemResource> allSlots = inventory instanceof InventoryWrapper wrapper
            ? wrapper.resourceHandler()
            : VanillaContainerWrapper.of(inventory);
        if (inventory instanceof WorldlyContainer worldly) {
            ResourceHandler<ItemResource>[] sides = new ResourceHandler[6];
            getter = side -> {
                if (side == null) {
                    return allSlots;
                }
                int index = side.get3DDataValue();
                ResourceHandler<ItemResource> handler = sides[index];
                if (handler == null) {
                    handler = sides[index] = new WorldlyContainerWrapper(worldly, side);
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
