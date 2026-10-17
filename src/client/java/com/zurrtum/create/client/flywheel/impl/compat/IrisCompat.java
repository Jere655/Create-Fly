package com.zurrtum.create.client.flywheel.impl.compat;

import com.zurrtum.create.client.flywheel.impl.FlwImpl;

import java.lang.reflect.Method;

/** Optional Iris state bridge, linked reflectively to avoid a hard dependency. */
public final class IrisCompat {
    private static final String IRIS_API_CLASS = "net.irisshaders.iris.api.v0.IrisApi";
    private static boolean reportedMissingApi;
    public static final boolean ACTIVE = CompatMod.IRIS.isLoaded && hasCompatibleApi();

    static {
        if (ACTIVE) {
            FlwImpl.LOGGER.debug("Detected Iris");
        }
    }

    private IrisCompat() {
    }

    private static boolean hasCompatibleApi() {
        if (!CompatMod.IRIS.isLoaded) {
            return false;
        }
        try {
            Class<?> apiClass = Class.forName(IRIS_API_CLASS, false, IrisCompat.class.getClassLoader());
            apiClass.getMethod("getInstance");
            apiClass.getMethod("isShaderPackInUse");
            apiClass.getMethod("isRenderingShadowPass");
            return true;
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportMissingApi(exception);
            return false;
        }
    }

    public static boolean isShaderPackInUse() {
        return invokeBoolean("isShaderPackInUse");
    }

    public static boolean isRenderingShadowPass() {
        return invokeBoolean("isRenderingShadowPass");
    }

    private static boolean invokeBoolean(String methodName) {
        if (!ACTIVE) {
            return false;
        }
        try {
            Class<?> apiClass = Class.forName(IRIS_API_CLASS, false, IrisCompat.class.getClassLoader());
            Object api = apiClass.getMethod("getInstance").invoke(null);
            Method method = apiClass.getMethod(methodName);
            return Boolean.TRUE.equals(method.invoke(api));
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportMissingApi(exception);
            return false;
        }
    }

    private static void reportMissingApi(Throwable exception) {
        if (!reportedMissingApi) {
            reportedMissingApi = true;
            FlwImpl.LOGGER.warn("Iris shader-state integration is unavailable; shader-aware rendering is disabled", exception);
        }
    }
}
