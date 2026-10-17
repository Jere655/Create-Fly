package com.zurrtum.create.client;

import com.zurrtum.create.client.catnip.gui.render.*;
import com.zurrtum.create.client.catnip.render.DefaultSuperRenderTypeBuffer;
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
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.zurrtum.create.client.content.trains.TrainHUD;
import com.zurrtum.create.client.flywheel.impl.Flywheel;
import com.zurrtum.create.client.flywheel.backend.compile.FlwProgramsReloader;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModelEventHandler;
import com.zurrtum.create.client.foundation.ClientResourceReloadListener;
import com.zurrtum.create.client.model.obj.ObjLoader;
import com.zurrtum.create.client.foundation.gui.render.*;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsClient;
import com.zurrtum.create.client.foundation.ponder.CreatePonderPlugin;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderState;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderer;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderState;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderer;
import com.zurrtum.create.client.vanillin.Vanillin;
import com.zurrtum.create.content.trains.GlobalRailwayManager;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
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
    public static final ClientResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();
    private static final Identifier TRAIN_HUD_LAYER = asResource("train_hud");
    private static boolean trainHudRenderedThisFrame;

    public Create(IEventBus modEventBus) {
        modEventBus.addListener(this::onInitializeClient);
        modEventBus.addListener(PartialModelEventHandler::registerAdditional);
        modEventBus.addListener(PartialModelEventHandler::onBakingCompleted);
        modEventBus.addListener(Create::registerClientReloadListeners);
        modEventBus.addListener(Create::registerRenderBuffers);
        modEventBus.addListener(Create::registerGuiLayers);
        modEventBus.addListener(Create::registerPictureInPictureRenderers);
        modEventBus.addListener(AllFluidConfigs::registerClientExtensions);
        NeoForge.EVENT_BUS.addListener(Create::onKeyInput);
        NeoForge.EVENT_BUS.addListener(Create::onRenderGuiPre);
        NeoForge.EVENT_BUS.addListener(Create::onRenderGuiLayerPre);
    }

    private static void registerPictureInPictureRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(ItemTransformRenderState.class, ItemTransformElementRenderer::new);
        event.register(BlockTransformRenderState.class, BlockTransformElementRenderer::new);
        event.register(EntityBlockRenderState.class, EntityBlockRenderer::new);
        event.register(PartialRenderState.class, PartialElementRenderer::new);
        event.register(BlazeBurnerRenderState.class, BlazeBurnerElementRenderer::new);
        event.register(PressBasinRenderState.class, PressBasinRenderer::new);
        event.register(PressRenderState.class, PressRenderer::new);
        event.register(MixingBasinRenderState.class, MixingBasinRenderer::new);
        event.register(BasinBlazeBurnerRenderState.class, BasinBlazeBurnerRenderer::new);
        event.register(MillstoneRenderState.class, MillstoneRenderer::new);
        event.register(SawRenderState.class, SawRenderer::new);
        event.register(CrushWheelRenderState.class, CrushWheelRenderer::new);
        event.register(DeployerRenderState.class, DeployerRenderer::new);
        event.register(ManualBlockRenderState.class, ManualBlockRenderer::new);
        event.register(SpoutRenderState.class, SpoutRenderer::new);
        event.register(CrafterRenderState.class, CrafterRenderer::new);
        event.register(DrainRenderState.class, DrainRenderer::new);
        event.register(SandPaperRenderState.class, SandPaperRenderer::new);
        event.register(TitleTextRenderState.class, TitleTextRenderer::new);
        event.register(
            SceneRenderState.class,
            bufferSource -> new SceneRenderer(new DefaultSuperRenderTypeBuffer.Dispatcher())
        );
        event.register(FanRenderState.class, FanRenderer::new);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(
            VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND,
            TRAIN_HUD_LAYER,
            (graphics, deltaTracker) -> {
                var minecraft = net.minecraft.client.Minecraft.getInstance();
                trainHudRenderedThisFrame =
                    !minecraft.options.hideGui
                    && TrainHUD.renderOverlay(minecraft, graphics, deltaTracker);
            }
        );
    }

    private static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        trainHudRenderedThisFrame = false;
    }

    private static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        if (!trainHudRenderedThisFrame) {
            return;
        }

        Identifier name = event.getName();

        if (VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND.equals(name)
            || VanillaGuiLayers.CONTEXTUAL_INFO_BAR.equals(name)) {
            event.setCanceled(true);
        }
    }

    private static void registerClientReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(ObjLoader.INSTANCE.getId(), ObjLoader.INSTANCE);
        event.addListener(FlwProgramsReloader.INSTANCE.getId(), FlwProgramsReloader.INSTANCE);
        event.addListener(RESOURCE_RELOAD_LISTENER.getId(), RESOURCE_RELOAD_LISTENER);
        event.addListener(
            ((com.zurrtum.create.foundation.utility.CreateResourceReloader) TrainHatInfoReloadListener.LISTENER).getId(),
            TrainHatInfoReloadListener.LISTENER
        );
        event.addListener(Ponder.RESOURCE_RELOAD_LISTENER.getId(), Ponder.RESOURCE_RELOAD_LISTENER);
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
