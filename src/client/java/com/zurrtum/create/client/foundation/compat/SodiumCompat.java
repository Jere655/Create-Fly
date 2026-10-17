package com.zurrtum.create.client.foundation.compat;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.Create;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.content.fluids.tank.FluidTankBlock;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/** Keeps Sodium's sprite cache aware of sprites used only by Create renderers. */
@SuppressWarnings("deprecation")
public final class SodiumCompat {
    private static final boolean ENABLED = ModList.get().isLoaded("sodium");
    private static final Object SPRITE_UTIL = findSpriteUtil();
    private static final Method MARK_SPRITE_ACTIVE = findMarkSpriteActive();
    private static boolean reportedMissingApi;

    public static final Material SAW_TEXTURE = new Material(
        TextureAtlas.LOCATION_BLOCKS,
        Create.asResource("block/saw_reversed")
    );
    public static final Material FACTORY_PANEL_TEXTURE = new Material(
        TextureAtlas.LOCATION_BLOCKS,
        Create.asResource("block/factory_panel_connections_animated")
    );
    public static final Material SAW_VANILLA_TEXTURE = new Material(
        TextureAtlas.LOCATION_BLOCKS,
        Identifier.withDefaultNamespace("block/stonecutter_saw")
    );

    private SodiumCompat() {
    }

    private static Object findSpriteUtil() {
        if (!ENABLED) {
            return null;
        }
        try {
            Class<?> spriteUtil = Class.forName("net.caffeinemc.mods.sodium.api.texture.SpriteUtil");
            Field instance = spriteUtil.getField("INSTANCE");
            return instance.get(null);
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportMissingApi(exception);
            return null;
        }
    }

    private static Method findMarkSpriteActive() {
        if (SPRITE_UTIL == null) {
            return null;
        }
        try {
            return SPRITE_UTIL.getClass().getMethod("markSpriteActive", TextureAtlasSprite.class);
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportMissingApi(exception);
            return null;
        }
    }

    private static void reportMissingApi(Throwable exception) {
        if (!reportedMissingApi) {
            reportedMissingApi = true;
            Create.LOGGER.warn("Sodium sprite-cache integration is unavailable; Create will use normal atlas fallback", exception);
        }
    }

    public static void markSpriteActive(Minecraft minecraft) {
        if (!ENABLED || MARK_SPRITE_ACTIVE == null) {
            return;
        }
        AtlasManager atlasManager = minecraft.getAtlasManager();
        markSpriteActive(atlasManager.get(SAW_TEXTURE));
        markSpriteActive(atlasManager.get(SAW_VANILLA_TEXTURE));
        markSpriteActive(atlasManager.get(FACTORY_PANEL_TEXTURE));
    }

    public static void markPonderSpriteActive(PonderLevel world, Selection section) {
        if (!ENABLED || MARK_SPRITE_ACTIVE == null) {
            return;
        }
        boolean saw = true;
        boolean panel = true;
        Set<Fluid> fluids = new HashSet<>();
        for (BlockPos pos : section) {
            BlockState state = world.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (saw && state.is(AllBlocks.MECHANICAL_SAW)) {
                AtlasManager atlasManager = Minecraft.getInstance().getAtlasManager();
                markSpriteActive(atlasManager.get(SAW_TEXTURE));
                markSpriteActive(atlasManager.get(SAW_VANILLA_TEXTURE));
                saw = false;
                continue;
            }
            if (panel && state.is(AllBlocks.FACTORY_GAUGE)) {
                markSpriteActive(Minecraft.getInstance().getAtlasManager().get(FACTORY_PANEL_TEXTURE));
                panel = false;
                continue;
            }
            if (state.getBlock() instanceof FluidTankBlock
                && world.getBlockEntity(pos) instanceof FluidTankBlockEntity tank) {
                FluidStack stack = tank.getTankInventory().getFluid();
                if (!stack.isEmpty() && fluids.add(stack.getFluid())) {
                    markFluidSpriteActive(stack.getFluid());
                }
                continue;
            }
            FluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty() && fluids.add(fluidState.getType())) {
                markFluidSpriteActive(fluidState.getType());
            }
        }
    }

    public static void markFluidSpriteActive(Fluid fluid) {
        if (!ENABLED || MARK_SPRITE_ACTIVE == null) {
            return;
        }
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config != null) {
            markSpriteActive(config.still().get());
            markSpriteActive(config.flowing().get());
        }
    }

    private static void markSpriteActive(TextureAtlasSprite sprite) {
        try {
            MARK_SPRITE_ACTIVE.invoke(SPRITE_UTIL, sprite);
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportMissingApi(exception);
        }
    }
}
