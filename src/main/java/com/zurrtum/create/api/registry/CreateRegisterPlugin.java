package com.zurrtum.create.api.registry;

import java.util.List;

public interface CreateRegisterPlugin {
    List<CreateRegisterPlugin> PLUGINS = createPlugins();

    default void onBlockRegister() {
    }

    default void onFluidRegister() {
    }

    default void onDataLoaderRegister() {
    }

    default void onEntityAttributeRegister() {
    }

    static List<CreateRegisterPlugin> createPlugins() {
        // Fabric entrypoints are loader-specific. NeoForge integrations register
        // through its event bus and are added here as they are ported.
        return List.of();
    }

    static void registerBlock() {
        PLUGINS.forEach(CreateRegisterPlugin::onBlockRegister);
    }

    static void registerFluid() {
        PLUGINS.forEach(CreateRegisterPlugin::onFluidRegister);
    }

    static void registerDataLoader() {
        PLUGINS.forEach(CreateRegisterPlugin::onDataLoaderRegister);
    }

    static void registerEntityAttributes() {
        PLUGINS.forEach(CreateRegisterPlugin::onEntityAttributeRegister);
    }
}
