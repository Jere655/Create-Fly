package com.zurrtum.create;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedDirectionInventoryBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedFluidInventoryBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedInventoryBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.neoforge.FluidHandlerInventory;
import com.zurrtum.create.infrastructure.neoforge.FluidInventoryHandler;
import com.zurrtum.create.infrastructure.neoforge.ItemHandlerContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AllTransfer {
    private static final List<Consumer<RegisterCapabilitiesEvent>> CAPABILITY_REGISTRATIONS = new ArrayList<>();
    private static boolean registered;

    public static Supplier<Container> getCacheInventory(
        ServerLevel world,
        BlockPos pos,
        Direction direction,
        BiPredicate<BlockEntity, Direction> filter
    ) {
        return () -> {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity == null || (filter != null && !filter.test(blockEntity, direction))) {
                return null;
            }
            return getInventory(world, pos, world.getBlockState(pos), blockEntity, direction);
        };
    }

    public static Container getInventory(
        Level world,
        BlockPos pos,
        @Nullable BlockState state,
        @Nullable BlockEntity blockEntity,
        Direction direction
    ) {
        ResourceHandler<ItemResource> inventory = world.getCapability(Capabilities.Item.BLOCK, pos, direction);
        if (inventory == null) {
            return null;
        }
        return new ItemHandlerContainer(inventory);
    }

    public static boolean hasFluidInventory(
        Level world,
        BlockPos pos,
        @Nullable BlockState state,
        @Nullable BlockEntity blockEntity,
        Direction direction
    ) {
        return world.getCapability(Capabilities.Fluid.BLOCK, pos, direction) != null;
    }

    public static Supplier<FluidInventory> getCacheFluidInventory(
        ServerLevel world,
        BlockPos pos,
        Direction direction
    ) {
        return () -> {
            return getFluidInventory(world, pos, world.getBlockState(pos), world.getBlockEntity(pos), direction);
        };
    }

    public static FluidInventory getFluidInventory(
        Level world,
        BlockPos pos,
        @Nullable BlockState state,
        @Nullable BlockEntity blockEntity,
        Direction direction
    ) {
        ResourceHandler<FluidResource> inventory = world.getCapability(Capabilities.Fluid.BLOCK, pos, direction);
        if (inventory == null) {
            return null;
        }
        return new FluidHandlerInventory(IFluidHandler.of(inventory));
    }

    public static boolean hasFluidInventory(ItemStack stack) {
        return !stack.isEmpty() && ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM) != null;
    }

    public static FluidItemInventory getFluidInventory(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ItemAccess access = ItemAccess.forStack(stack);
        ResourceHandler<FluidResource> inventory = access.getCapability(Capabilities.Fluid.ITEM);
        if (inventory == null) {
            return null;
        }
        return new FluidHandlerInventory.Item(access, IFluidHandler.of(inventory));
    }

    private static <T extends SmartBlockEntity> void registerItemSide(
        BlockEntityType<T> type,
        Function<T, Container> factory
    ) {
        BlockEntityBehaviour.add(type, (T be) -> new CachedInventoryBehaviour<>(be, factory));
        CAPABILITY_REGISTRATIONS.add(event -> event.registerBlockEntity(
            Capabilities.Item.BLOCK,
            type,
            (be, side) -> {
                Container inventory = CachedInventoryBehaviour.get(be, side);
                if (inventory == null) {
                    return null;
                }
                if (inventory instanceof net.minecraft.world.WorldlyContainer sided && side != null) {
                    return new WorldlyContainerWrapper(sided, side);
                }
                return VanillaContainerWrapper.of(inventory);
            }
        ));
    }

    private static <T extends SmartBlockEntity> void registerItemSide(
        BlockEntityType<T> type,
        BiFunction<T, Direction, Container> factory
    ) {
        BlockEntityBehaviour.add(type, (T be) -> new CachedDirectionInventoryBehaviour<>(be, factory));
        CAPABILITY_REGISTRATIONS.add(event -> event.registerBlockEntity(
            Capabilities.Item.BLOCK,
            type,
            (be, side) -> {
                Container inventory = CachedDirectionInventoryBehaviour.get(be, side);
                if (inventory == null) {
                    return null;
                }
                if (inventory instanceof net.minecraft.world.WorldlyContainer sided && side != null) {
                    return new WorldlyContainerWrapper(sided, side);
                }
                return VanillaContainerWrapper.of(inventory);
            }
        ));
    }

    private static <T extends SmartBlockEntity> void registerFluidSide(
        BlockEntityType<T> type,
        Function<T, FluidInventory> factory
    ) {
        BlockEntityBehaviour.add(type, (T be) -> new CachedFluidInventoryBehaviour<>(be, factory));
        CAPABILITY_REGISTRATIONS.add(event -> event.registerBlockEntity(
            Capabilities.Fluid.BLOCK,
            type,
            (be, side) -> {
                FluidInventory inventory = CachedFluidInventoryBehaviour.get(be, side);
                return inventory == null ? null : new FluidInventoryHandler(inventory, side);
            }
        ));
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        registerItemSide(AllBlockEntityTypes.DEPOT, be -> be.depotBehaviour.itemHandler);
        registerItemSide(AllBlockEntityTypes.WEIGHTED_EJECTOR, be -> be.depotBehaviour.itemHandler);
        registerItemSide(
            AllBlockEntityTypes.BELT, be -> {
                if (!BeltBlock.canTransportObjects(be.getBlockState())) {
                    return null;
                }
                if (!be.isRemoved() && be.itemHandler == null) {
                    be.initializeItemHandler();
                }
                return be.itemHandler;
            }
        );
        registerItemSide(AllBlockEntityTypes.MILLSTONE, be -> be.capability);
        registerItemSide(AllBlockEntityTypes.SAW, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.BASIN, be -> be.itemCapability);
        registerItemSide(
            AllBlockEntityTypes.ANDESITE_TUNNEL, be -> {
                if (be.cap == null) {
                    Level world = be.getLevel();
                    BlockPos pos = be.getBlockPos();
                    BlockState state = world.getBlockState(pos.below());
                    if (state.is(AllBlocks.BELT)) {
                        BlockEntity beBelow = world.getBlockEntity(pos.below());
                        if (beBelow != null) {
                            Container capBelow = ItemHelper.getInventory(
                                world,
                                pos.below(),
                                state,
                                beBelow,
                                Direction.UP
                            );
                            if (capBelow != null) {
                                be.cap = capBelow;
                            }
                        }
                    }
                }
                return be.cap;
            }
        );
        registerItemSide(AllBlockEntityTypes.BRASS_TUNNEL, be -> be.tunnelCapability);
        registerItemSide(AllBlockEntityTypes.CHUTE, be -> be.itemHandler);
        registerItemSide(AllBlockEntityTypes.SMART_CHUTE, be -> be.itemHandler);
        registerItemSide(AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE, be -> be.capability);
        registerItemSide(
            AllBlockEntityTypes.ITEM_DRAIN, (be, context) -> {
                if (context != null && context.getAxis().isHorizontal()) {
                    return be.itemHandlers.get(context);
                }
                return null;
            }
        );
        registerItemSide(
            AllBlockEntityTypes.DEPLOYER, be -> {
                if (be.invHandler == null) {
                    be.initHandler();
                }
                return be.invHandler;
            }
        );
        registerItemSide(AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.MECHANICAL_CRAFTER, MechanicalCrafterBlockEntity::getInvCapability);
        registerItemSide(AllBlockEntityTypes.CREATIVE_CRATE, be -> be.inv);
        registerItemSide(AllBlockEntityTypes.PACKAGER, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.REPACKAGER, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.PACKAGE_POSTBOX, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.PACKAGE_FROGPORT, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.TOOLBOX, be -> be.inventory);
        registerItemSide(AllBlockEntityTypes.TRACK_STATION, be -> be.depotBehaviour.itemHandler);
        registerFluidSide(
            AllBlockEntityTypes.FLUID_TANK, be -> {
                if (be.fluidCapability == null) {
                    be.refreshCapability();
                }
                return be.fluidCapability;
            }
        );
        registerFluidSide(AllBlockEntityTypes.BASIN, be -> be.fluidCapability);
        registerFluidSide(AllBlockEntityTypes.PORTABLE_FLUID_INTERFACE, be -> be.capability);
        registerFluidSide(AllBlockEntityTypes.HOSE_PULLEY, be -> be.handler);
        registerFluidSide(AllBlockEntityTypes.SPOUT, be -> be.tank.getCapability());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        CAPABILITY_REGISTRATIONS.forEach(registration -> registration.accept(event));
    }
}
