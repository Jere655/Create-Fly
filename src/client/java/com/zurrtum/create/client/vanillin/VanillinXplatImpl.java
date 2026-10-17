package com.zurrtum.create.client.vanillin;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class VanillinXplatImpl implements VanillinXplat {
    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
