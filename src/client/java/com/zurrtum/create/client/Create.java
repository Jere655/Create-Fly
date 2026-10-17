package com.zurrtum.create.client;

import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.content.contraptions.glue.SuperGlueSelectionHandler;
import com.zurrtum.create.client.content.contraptions.wrench.RadialWrenchHandler;
import com.zurrtum.create.client.content.equipment.bell.SoulPulseEffectHandler;
import com.zurrtum.create.client.content.equipment.potatoCannon.PotatoCannonRenderHandler;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxHandlerClient;
import com.zurrtum.create.client.content.equipment.zapper.ZapperRenderHandler;
import com.zurrtum.create.client.content.schematics.client.ClientSchematicLoader;
import com.zurrtum.create.client.content.schematics.client.SchematicAndQuillHandler;
import com.zurrtum.create.client.content.schematics.client.SchematicHandler;
import com.zurrtum.create.client.flywheel.impl.Flywheel;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModelEventHandler;
import com.zurrtum.create.client.foundation.ClientResourceReloadListener;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsClient;
import com.zurrtum.create.client.foundation.ponder.CreatePonderPlugin;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.vanillin.Vanillin;
import com.zurrtum.create.content.trains.GlobalRailwayManager;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.common.NeoForge;

import static com.zurrtum.create.Create.MOD_ID;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class Create {
    public static SoulPulseEffectHandler SOUL_PULSE_EFFECT_HANDLER;
    public static ValueSettingsClient VALUE_SETTINGS_HANDLER;
    public static SuperGlueSelectionHandler GLUE_HANDLER;
    public static GlobalRailwayManager RAILWAYS;
    public static PotatoCannonRenderHandler POTATO_CANNON_RENDER_HANDLER;
    public static ClientSchematicLoader SCHEMATIC_SENDER;
    public static SchematicHandler SCHEMATIC_HANDLER;
    public static SchematicAndQuillHandler SCHEMATIC_AND_QUILL_HANDLER;
    public static ZapperRenderHandler ZAPPER_RENDER_HANDLER;
    public static ClientResourceReloadListener RESOURCE_RELOAD_LISTENER;

    public Create(IEventBus modEventBus) {
        modEventBus.addListener(this::onInitializeClient);
        modEventBus.addListener(PartialModelEventHandler::registerAdditional);
        modEventBus.addListener(PartialModelEventHandler::onBakingCompleted);
        modEventBus.addListener(Create::registerRenderBuffers);
        modEventBus.addListener(AllFluidConfigs::registerClientExtensions);
        NeoForge.EVENT_BUS.addListener(Create::onKeyInput);
    }

    private static void registerRenderBuffers(RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(CreateRenderTypes.additive2());
        event.registerRenderBuffer(CreateRenderTypes.translucent());
        event.registerRenderBuffer(CreateRenderTypes.additive());
    }

    private void onInitializeClient(FMLClientSetupEvent event) {
        new Flywheel().onInitializeClient();
        new Ponder().onInitializeClient();
        new Vanillin().onInitializeClient();
        SOUL_PULSE_EFFECT_HANDLER = new SoulPulseEffectHandler();
        VALUE_SETTINGS_HANDLER = new ValueSettingsClient();
        GLUE_HANDLER = new SuperGlueSelectionHandler();
        RAILWAYS = new GlobalRailwayManager();
        POTATO_CANNON_RENDER_HANDLER = new PotatoCannonRenderHandler();
        SCHEMATIC_SENDER = new ClientSchematicLoader();
        SCHEMATIC_HANDLER = new SchematicHandler();
        SCHEMATIC_AND_QUILL_HANDLER = new SchematicAndQuillHandler();
        ZAPPER_RENDER_HANDLER = new ZapperRenderHandler();
        RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();
        AllConfigs.register();
        AllFluidConfigs.register();
        AllHandle.register();
        AllKeys.register();
        AllCasings.register();
        AllCTBehaviours.register();
        AllModels.register();
        AllPartialModels.register();
        AllEntityRenders.register();
        AllBlockEntityRenders.register();
        AllBlockEntityBehaviours.register();
        AllEntityBehaviours.register();
        AllItemTooltips.register();
        AllBlockLayers.register();
        AllBufferCaches.register(SuperByteBufferCache.getInstance());
        AllExtensions.register();
        AllMovementRenders.register();
        AllDisplaySourceRenders.register();
        AllTrackMaterialModels.register();
        AllTrackRenders.register();
        AllBogeyStyleRenders.register();
        AllTrainIcons.register();
        AllScheduleRenders.register();
        AllMenuScreens.register();
        AllPotatoProjectileTransforms.register();
        PonderIndex.addPlugin(new CreatePonderPlugin());
        // Optional REI/JEI/EIV, Trinkets, and ComputerCraft client adapters are
        // deferred until their native NeoForge integrations and dependencies are available.
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void invalidateRenderers() {
        SCHEMATIC_HANDLER.updateRenderers();
    }

    private static void onKeyInput(InputEvent.Key event) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.screen != null || SCHEMATIC_HANDLER == null) {
            return;
        }

        var input = event.getKeyEvent();
        boolean pressed = event.getAction() != 0;
        if (SCHEMATIC_HANDLER.onKeyInput(input, pressed)) {
            return;
        }
        if (ToolboxHandlerClient.onKeyInput(minecraft, input)) {
            return;
        }
        RadialWrenchHandler.onKeyInput(minecraft, input, pressed);
    }
}
