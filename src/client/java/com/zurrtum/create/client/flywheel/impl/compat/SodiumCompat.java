package com.zurrtum.create.client.flywheel.impl.compat;

import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import com.zurrtum.create.client.flywheel.impl.FlwImpl;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/** Optional Sodium block-entity rendering bridge, linked reflectively to avoid a hard dependency. */
public final class SodiumCompat {
    private static final String RENDER_HANDLER_CLASS = "net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderHandler";
    private static final String RENDER_PREDICATE_CLASS = "net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderPredicate";
    private static boolean reportedMissingApi;
    public static final boolean ACTIVE = CompatMod.SODIUM.isLoaded && hasCompatibleApi();

    static {
        if (ACTIVE) {
            FlwImpl.LOGGER.debug("Detected Sodium");
        }
    }

    private SodiumCompat() {
    }

    private static boolean hasCompatibleApi() {
        if (!CompatMod.SODIUM.isLoaded) {
            return false;
        }
        try {
            ClassLoader loader = SodiumCompat.class.getClassLoader();
            Class<?> handlerClass = Class.forName(RENDER_HANDLER_CLASS, false, loader);
            Class<?> predicateClass = Class.forName(RENDER_PREDICATE_CLASS, false, loader);
            handlerClass.getMethod("instance");
            boolean hasAdd = false;
            boolean hasRemove = false;
            for (Method method : handlerClass.getMethods()) {
                if (method.getParameterCount() != 2
                    || !method.getParameterTypes()[0].isAssignableFrom(BlockEntityType.class)
                    || !method.getParameterTypes()[1].isAssignableFrom(predicateClass)) {
                    continue;
                }
                hasAdd |= method.getName().equals("addRenderPredicate");
                hasRemove |= method.getName().equals("removeRenderPredicate");
            }
            if (!predicateClass.isInterface() || !hasAdd || !hasRemove) {
                reportMissingApi("Sodium's block-entity render API has an incompatible shape");
                return false;
            }
            return true;
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportMissingApi(exception);
            return false;
        }
    }

    @Nullable
    public static <T extends BlockEntity> Object onSetBlockEntityVisualizer(
        BlockEntityType<T> type,
        @Nullable BlockEntityVisualizer<? super T> oldVisualizer,
        @Nullable BlockEntityVisualizer<? super T> newVisualizer,
        @Nullable Object predicate
    ) {
        if (!ACTIVE) {
            return null;
        }

        if (oldVisualizer == null && newVisualizer != null) {
            if (predicate != null) {
                throw new IllegalArgumentException("Sodium predicate must be null when old visualizer is null");
            }
            Object created = createPredicate();
            if (created == null) {
                return new Object();
            }
            invokeHandler("addRenderPredicate", type, created);
            return created;
        }

        if (oldVisualizer != null && newVisualizer == null) {
            if (predicate == null) {
                throw new IllegalArgumentException("Sodium predicate must not be null when old visualizer is not null");
            }
            invokeHandler("removeRenderPredicate", type, predicate);
            return null;
        }

        return predicate;
    }

    @Nullable
    private static Object createPredicate() {
        try {
            ClassLoader loader = SodiumCompat.class.getClassLoader();
            Class<?> predicateClass = Class.forName(RENDER_PREDICATE_CLASS, false, loader);
            if (!predicateClass.isInterface()) {
                reportMissingApi("Sodium's block-entity render predicate is not an interface");
                return null;
            }
            InvocationHandler invocationHandler = SodiumCompat::invokePredicate;
            return Proxy.newProxyInstance(loader, new Class<?>[]{predicateClass}, invocationHandler);
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportMissingApi(exception);
            return null;
        }
    }

    private static Object invokePredicate(Object proxy, Method method, Object[] arguments) {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "equals" -> proxy == (arguments == null ? null : arguments[0]);
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "CreateFlywheelSodiumRenderPredicate";
                default -> null;
            };
        }

        if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
            if (arguments != null) {
                for (Object argument : arguments) {
                    if (argument instanceof BlockEntity blockEntity) {
                        return !VisualizationHelper.tryAddBlockEntity(blockEntity);
                    }
                }
            }
            return true;
        }

        return null;
    }

    private static boolean invokeHandler(String methodName, BlockEntityType<?> type, Object predicate) {
        try {
            ClassLoader loader = SodiumCompat.class.getClassLoader();
            Class<?> handlerClass = Class.forName(RENDER_HANDLER_CLASS, false, loader);
            Object handler = handlerClass.getMethod("instance").invoke(null);
            for (Method method : handlerClass.getMethods()) {
                if (method.getName().equals(methodName)
                    && method.getParameterCount() == 2
                    && method.getParameterTypes()[0].isInstance(type)
                    && method.getParameterTypes()[1].isInstance(predicate)) {
                    method.invoke(handler, type, predicate);
                    return true;
                }
            }
            reportMissingApi("Sodium is missing " + methodName + "(BlockEntityType, BlockEntityRenderPredicate)");
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportMissingApi(exception);
        }
        return false;
    }

    private static void reportMissingApi(Object reason) {
        if (!reportedMissingApi) {
            reportedMissingApi = true;
            FlwImpl.LOGGER.warn("Sodium block-entity rendering integration is unavailable; using vanilla fallback ({})", reason);
        }
    }

    private static void reportMissingApi(Throwable exception) {
        if (!reportedMissingApi) {
            reportedMissingApi = true;
            FlwImpl.LOGGER.warn("Sodium block-entity rendering integration is unavailable; using vanilla fallback", exception);
        }
    }
}
