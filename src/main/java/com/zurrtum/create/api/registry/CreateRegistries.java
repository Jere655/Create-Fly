package com.zurrtum.create.api.registry;

import com.zurrtum.create.Create;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTargetType;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreateRegistries {
    private static final DeferredRegister<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPES = create(
        CreateRegistryKeys.ARM_INTERACTION_POINT_TYPE);
    private static final DeferredRegister<FanProcessingType> FAN_PROCESSING_TYPES = create(CreateRegistryKeys.FAN_PROCESSING_TYPE);
    private static final DeferredRegister<ItemAttributeType> ITEM_ATTRIBUTE_TYPES = create(CreateRegistryKeys.ITEM_ATTRIBUTE_TYPE);
    private static final DeferredRegister<DisplaySource> DISPLAY_SOURCES = create(CreateRegistryKeys.DISPLAY_SOURCE);
    private static final DeferredRegister<DisplayTarget> DISPLAY_TARGETS = create(CreateRegistryKeys.DISPLAY_TARGET);
    private static final DeferredRegister<MountedItemStorageType<?>> MOUNTED_ITEM_STORAGE_TYPES = create(
        CreateRegistryKeys.MOUNTED_ITEM_STORAGE_TYPE);
    private static final DeferredRegister<MountedFluidStorageType<?>> MOUNTED_FLUID_STORAGE_TYPES = create(
        CreateRegistryKeys.MOUNTED_FLUID_STORAGE_TYPE);
    private static final DeferredRegister<ContraptionType> CONTRAPTION_TYPES = create(CreateRegistryKeys.CONTRAPTION_TYPE);
    private static final DeferredRegister<MapCodec<? extends PotatoProjectileRenderMode>> POTATO_PROJECTILE_RENDER_MODES = create(
        CreateRegistryKeys.POTATO_PROJECTILE_RENDER_MODE);
    private static final DeferredRegister<MapCodec<? extends PotatoProjectileEntityHitAction>> POTATO_PROJECTILE_ENTITY_HIT_ACTIONS = create(
        CreateRegistryKeys.POTATO_PROJECTILE_ENTITY_HIT_ACTION);
    private static final DeferredRegister<MapCodec<? extends PotatoProjectileBlockHitAction>> POTATO_PROJECTILE_BLOCK_HIT_ACTIONS = create(
        CreateRegistryKeys.POTATO_PROJECTILE_BLOCK_HIT_ACTION);
    private static final DeferredRegister<PackagePortTargetType> PACKAGE_PORT_TARGET_TYPES = create(
        CreateRegistryKeys.PACKAGE_PORT_TARGET_TYPE);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = create(CreateRegistryKeys.MENU_TYPE);

    public static final Registry<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPE = createRegistry(ARM_INTERACTION_POINT_TYPES);
    public static final Registry<FanProcessingType> FAN_PROCESSING_TYPE = createRegistry(FAN_PROCESSING_TYPES);
    public static final Registry<ItemAttributeType> ITEM_ATTRIBUTE_TYPE = createRegistry(ITEM_ATTRIBUTE_TYPES);
    public static final Registry<DisplaySource> DISPLAY_SOURCE = createRegistry(DISPLAY_SOURCES);
    public static final Registry<DisplayTarget> DISPLAY_TARGET = createRegistry(DISPLAY_TARGETS);
    public static final Registry<MountedItemStorageType<?>> MOUNTED_ITEM_STORAGE_TYPE = createIntrusiveRegistry(
        MOUNTED_ITEM_STORAGE_TYPES);
    public static final Registry<MountedFluidStorageType<?>> MOUNTED_FLUID_STORAGE_TYPE = createRegistry(
        MOUNTED_FLUID_STORAGE_TYPES);
    public static final Registry<ContraptionType> CONTRAPTION_TYPE = createIntrusiveRegistry(CONTRAPTION_TYPES);
    public static final Registry<MapCodec<? extends PotatoProjectileRenderMode>> POTATO_PROJECTILE_RENDER_MODE = createRegistry(
        POTATO_PROJECTILE_RENDER_MODES);
    public static final Registry<MapCodec<? extends PotatoProjectileEntityHitAction>> POTATO_PROJECTILE_ENTITY_HIT_ACTION = createRegistry(
        POTATO_PROJECTILE_ENTITY_HIT_ACTIONS);
    public static final Registry<MapCodec<? extends PotatoProjectileBlockHitAction>> POTATO_PROJECTILE_BLOCK_HIT_ACTION = createRegistry(
        POTATO_PROJECTILE_BLOCK_HIT_ACTIONS);
    public static final Registry<PackagePortTargetType> PACKAGE_PORT_TARGET_TYPE = createRegistry(PACKAGE_PORT_TARGET_TYPES);
    public static final Registry<MenuType<?>> MENU_TYPE = createRegistry(MENU_TYPES);

    private static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key) {
        return DeferredRegister.create(key, Create.MOD_ID);
    }

    private static <T> Registry<T> createRegistry(DeferredRegister<T> register) {
        return register.makeRegistry(builder -> builder.sync(true));
    }

    private static <T> Registry<T> createIntrusiveRegistry(DeferredRegister<T> register) {
        return register.makeRegistry(builder -> builder.sync(true).withIntrusiveHolders());
    }

    public static void register(IEventBus modEventBus) {
        ARM_INTERACTION_POINT_TYPES.register(modEventBus);
        FAN_PROCESSING_TYPES.register(modEventBus);
        ITEM_ATTRIBUTE_TYPES.register(modEventBus);
        DISPLAY_SOURCES.register(modEventBus);
        DISPLAY_TARGETS.register(modEventBus);
        MOUNTED_ITEM_STORAGE_TYPES.register(modEventBus);
        MOUNTED_FLUID_STORAGE_TYPES.register(modEventBus);
        CONTRAPTION_TYPES.register(modEventBus);
        POTATO_PROJECTILE_RENDER_MODES.register(modEventBus);
        POTATO_PROJECTILE_ENTITY_HIT_ACTIONS.register(modEventBus);
        POTATO_PROJECTILE_BLOCK_HIT_ACTIONS.register(modEventBus);
        PACKAGE_PORT_TARGET_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
    }
}
