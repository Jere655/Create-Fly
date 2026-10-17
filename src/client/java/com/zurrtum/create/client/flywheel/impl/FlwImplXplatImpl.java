package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.client.flywheel.backend.engine.uniform.Uniforms;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.fml.ModList;

public class FlwImplXplatImpl implements FlwImplXplat {
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().getModFileById(modId) != null;
    }

    @Override
    public void dispatchReloadLevelRendererEvent(ClientLevel level) {
        BackendManagerImpl.onReloadLevelRenderer(level);
        Uniforms.onReloadLevelRenderer();
        //TODO Fabric
    }

    @Override
    public String getVersionStr() {
        return Flywheel.version();
    }

    @Override
    public FlwConfig getConfig() {
        return FabricFlwConfig.INSTANCE;
    }
}
