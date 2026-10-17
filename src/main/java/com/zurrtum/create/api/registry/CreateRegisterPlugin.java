package com.zurrtum.create.api.registry;

import net.neoforged.fml.loading.FMLLoader;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Early Create registration hooks.
 *
 * <p>NeoForge extensions are discovered with Java's {@link ServiceLoader}. A
 * provider must be listed in
 * {@code META-INF/services/com.zurrtum.create.api.registry.CreateRegisterPlugin}
 * in its mod jar. The built-in provider runs before third-party providers;
 * third-party providers run in class-name order for deterministic bootstrap.
 */
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

    private static List<CreateRegisterPlugin> createPlugins() {
        return ServiceLoader.load(FMLLoader.getGameLayer(), CreateRegisterPlugin.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .sorted(Comparator
                .comparing((CreateRegisterPlugin plugin) -> !plugin.getClass()
                    .getName()
                    .equals("com.zurrtum.create.AllEarlyRegistries"))
                .thenComparing(plugin -> plugin.getClass().getName()))
            .toList();
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
