package com.zurrtum.create.client.vanillin;

import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.client.vanillin.config.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zurrtum.create.client.vanillin.Vanillin.MOD_ID;


public class VanillinConfig {
    public static final String VANILLIN_OVERRIDES = "vanillin:overrides";
    private static ModOverrides overrides;
    private static CClient client;

    public static ModOverrides modOverrides() {
        // Fabric metadata custom values are not available on NeoForge. Native
        // override metadata can be added once Vanillin's loader bridge is ported.
        return new ModOverrides(new ArrayList<>(), new ArrayList<>());
    }

    public static CClient client() {
        return client;
    }

    public static ModOverrides overrides() {
        return overrides;
    }

    public static void register() {
        client = Builder.create(CClient::new, MOD_ID, "client", true);
        overrides = modOverrides();
    }

    public static void apply(Configurator configurator) {
        var blockEntities = client.blockEntities;
        var blockEntityOverrides = overrides.blockEntities();
        for (Configurator.ConfiguredVisual configured : configurator.blockEntities.values()) {
            apply(configured, blockEntities, blockEntityOverrides);
        }

        var entities = client.entities;
        var entityOverrides = overrides.entities();
        for (Configurator.ConfiguredVisual configured : configurator.entities.values()) {
            apply(configured, entities, entityOverrides);
        }
    }

    private static void apply(
        Configurator.ConfiguredVisual configured,
        Map<String, ConfigBase.ConfigEnum<VisualConfigValue>> config,
        Map<String, List<VisualOverride>> overrides
    ) {
        String key = configured.configKey();
        VisualConfigValue enabled = config.get(key).get();
        configured.set(enabled, overrides.get(key));
    }
}
