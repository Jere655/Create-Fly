package com.zurrtum.create.client.vanillin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.client.vanillin.config.CClient;
import com.zurrtum.create.client.vanillin.config.Configurator;
import com.zurrtum.create.client.vanillin.config.ModOverrides;
import com.zurrtum.create.client.vanillin.config.VisualConfigValue;
import com.zurrtum.create.client.vanillin.config.VisualOverride;
import com.zurrtum.create.client.vanillin.config.VisualOverrideValue;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zurrtum.create.client.vanillin.Vanillin.MOD_ID;

public class VanillinConfig {
    /**
     * Mods may set this key in their {@code [modproperties.create]} table in
     * {@code META-INF/neoforge.mods.toml}. Its value is a JSON string with
     * optional {@code block_entities} and {@code entities} objects, mapping
     * registry paths to visual-config values.
     */
    public static final String VANILLIN_OVERRIDES = "vanillin:overrides";
    private static ModOverrides overrides;
    private static CClient client;

    public static ModOverrides modOverrides() {
        var blockEntities = new ArrayList<VisualOverride>();
        var entities = new ArrayList<VisualOverride>();

        for (IModInfo modInfo : ModList.get().getMods()) {
            String modId = modInfo.getModId();
            Object rawOverrides = modInfo.getModProperties().get(VANILLIN_OVERRIDES);
            if (rawOverrides == null) {
                continue;
            }

            JsonObject modOverrides;
            try {
                JsonElement parsed = JsonParser.parseString(String.valueOf(rawOverrides));
                if (!parsed.isJsonObject()) {
                    throw new JsonParseException("Expected an object");
                }
                modOverrides = parsed.getAsJsonObject();
            } catch (JsonParseException | IllegalStateException exception) {
                Vanillin.CONFIG_LOGGER.warn(
                    "Mod '{}' attempted to override options with an invalid value, ignoring",
                    modId
                );
                continue;
            }

            readSection(blockEntities, modId, modOverrides, "block_entities", "block entity");
            readSection(entities, modId, modOverrides, "entities", "entity");
        }

        return new ModOverrides(blockEntities, entities);
    }

    private static void readSection(
        List<VisualOverride> destination,
        String modId,
        JsonObject overrides,
        String sectionName,
        String singular
    ) {
        if (!overrides.has(sectionName)) {
            return;
        }

        JsonElement section = overrides.get(sectionName);
        if (!section.isJsonObject()) {
            Vanillin.CONFIG_LOGGER.warn(
                "Mod '{}' attempted to override {} with an invalid value, ignoring",
                modId,
                sectionName
            );
            return;
        }

        for (Map.Entry<String, JsonElement> entry : section.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            String key = entry.getKey();
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                Vanillin.CONFIG_LOGGER.warn(
                    "Mod '{}' attempted to override {} '{}' with an invalid value, ignoring",
                    modId,
                    singular,
                    key
                );
                continue;
            }

            String valueString = value.getAsString();
            VisualOverrideValue parsed = VisualOverrideValue.parse(valueString);
            if (parsed == null) {
                Vanillin.CONFIG_LOGGER.warn(
                    "Mod '{}' attempted to override {} '{}' with an invalid value '{}', ignoring",
                    modId,
                    singular,
                    key,
                    valueString
                );
                continue;
            }

            destination.add(new VisualOverride(key, modId, parsed));
        }
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
