package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.IdentityHashMap;
import java.util.Map;

public final class PartialModelEventHandler {
    private static final Map<PartialModel, StandaloneModelKey<SimpleModelWrapper>> KEYS = new IdentityHashMap<>();

    private PartialModelEventHandler() {
    }

    public static void registerAdditional(ModelEvent.RegisterStandalone event) {
        KEYS.clear();
        PartialModel.ALL.forEach((id, partial) -> {
            StandaloneModelKey<SimpleModelWrapper> key = new StandaloneModelKey<>(id::toString);
            event.register(key, new SimpleUnbakedStandaloneModel<>(
                id, (model, baker) -> (SimpleModelWrapper) SimpleModelWrapper.bake(baker, model, BlockModelRotation.IDENTITY)
            ));
            KEYS.put(partial, key);
        });
    }

    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        KEYS.forEach((partial, key) -> partial.bakedModel = event.getModelManager().getStandaloneModel(key));
        PartialModel.populateOnInit = true;
    }
}
