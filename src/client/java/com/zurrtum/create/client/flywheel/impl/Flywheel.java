package com.zurrtum.create.client.flywheel.impl;

import net.minecraft.commands.synchronization.ArgumentTypeInfos;

public final class Flywheel {
    public static final String MOD_ID = "flywheel";
    private static final String VERSION = "1.0.6+create";

    public void onInitializeClient() {
        setupImpl();
        FlwImpl.init();
    }

    private static void setupImpl() {
        // These client-only debug arguments are added to the lookup map without
        // registering entries in the synchronized command-argument registry.
        ArgumentTypeInfos.BY_CLASS.put(BackendArgument.class, BackendArgument.INFO);
        ArgumentTypeInfos.BY_CLASS.put(DebugModeArgument.class, DebugModeArgument.INFO);
        ArgumentTypeInfos.BY_CLASS.put(LightSmoothnessArgument.class, LightSmoothnessArgument.INFO);
    }

    public static String version() {
        return VERSION;
    }
}
