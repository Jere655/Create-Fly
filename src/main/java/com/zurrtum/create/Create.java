package com.zurrtum.create;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.content.decoration.encasing.EncasingRegistry;
import com.zurrtum.create.content.equipment.armor.AllArmorMaterials;
import com.zurrtum.create.content.equipment.armor.AllEquipmentAssetKeys;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileBlockHitActions;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes;
import com.zurrtum.create.content.equipment.tool.AllToolMaterials;
import com.zurrtum.create.content.fluids.AllFlowCollision;
import com.zurrtum.create.content.fluids.tank.BoilerHeaters;
import com.zurrtum.create.content.kinetics.TorquePropagator;
import com.zurrtum.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.zurrtum.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.zurrtum.create.content.logistics.packagePort.AllPackagePortTargetTypes;
import com.zurrtum.create.content.logistics.packagerLink.GlobalLogisticsManager;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.zurrtum.create.content.schematics.ServerSchematicLoader;
import com.zurrtum.create.content.trains.GlobalRailwayManager;
import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.track.AllPortalTracks;
import com.zurrtum.create.foundation.CreateNBTProcessors;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.worldgen.AllConfiguredFeatures;
import com.zurrtum.create.infrastructure.worldgen.AllFeatures;
import com.zurrtum.create.infrastructure.worldgen.AllPlacedFeatures;
import com.zurrtum.create.infrastructure.worldgen.AllPlacementModifiers;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(Create.MOD_ID)
public class Create {
    public static final String MOD_ID = "create";
    public static final String NAME = "Create";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static String VERSION = "unknown";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static boolean Lazy = false;

    public static MinecraftServer SERVER;
    public static TorquePropagator TORQUE_PROPAGATOR;
    public static GlobalRailwayManager RAILWAYS;
    public static RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER;
    public static GlobalLogisticsManager LOGISTICS;
    public static ServerSchematicLoader SCHEMATIC_RECEIVER;
    private static boolean registered;

    public Create(IEventBus modEventBus, ModContainer modContainer) {
        VERSION = modContainer.getModInfo().getVersion().toString().split("\\+")[0];
        modEventBus.addListener(AllTransfer::registerCapabilities);
        AllFluidTypes.register(modEventBus);
        CreateRegistries.register(modEventBus);
        // Fabric initialized these static registries during loader bootstrap.
        // NeoForge opens registries during RegisterEvent, after mod construction.
        modEventBus.addListener(RegisterEvent.class, this::register);
        AllConfigs.register();
    }

    private void register(RegisterEvent event) {
        if (registered) {
            return;
        }
        registered = true;
        register();
    }

    public static void register() {
        TORQUE_PROPAGATOR = new TorquePropagator();
        RAILWAYS = new GlobalRailwayManager();
        REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
        LOGISTICS = new GlobalLogisticsManager();
        SCHEMATIC_RECEIVER = new ServerSchematicLoader();
        CreateRegistryKeys.register();
        AllPackageStyles.register();
        AllToolMaterials.register();
        AllArmorMaterials.register();
        EncasingRegistry.register();
        BlockStressValues.register();
        AllItems.register();
        AllFlowCollision.register();
        AllFluidTags.register();
        AllBlockTags.register();
        AllItemTags.register();
        AllMountedItemStorageTypeTags.register();
        AllContraptionTypeTags.register();
        AllEntityTags.register();
        AllSoundEvents.register();
        AllParticleTypes.register();
        AllDataComponents.register();
        AllDamageTypes.register();
        AllPackets.register();
        AllCreativeModeTabs.register();
        AllContraptionTypes.register();
        AllEntityTypes.register();
        AllBlockEntityTypes.register();
        AllAdvancements.register();
        AllRecipeTypes.register();
        AllRecipeSerializers.register();
        AllRecipeSets.register();
        AllFluidItemInventory.register();
        AllTransfer.register();
        AllOpenPipeEffectHandlers.register();
        AllArmInteractionPointTypes.register();
        AllFanProcessingTypes.register();
        BoilerHeaters.register();
        AllSynchedDatas.register();
        AllMountedStorageTypes.register();
        AllMovementBehaviours.register();
        AllContraptionMovementSettings.register();
        AllInteractionBehaviours.register();
        AllEquipmentAssetKeys.register();
        AllTrackMaterials.register();
        AllDisplayTargets.register();
        AllDisplaySources.register();
        AllMapDecorationTypes.register();
        AllBogeySizes.register();
        AllBogeyStyles.register();
        AllPortalTracks.register();
        AllSchedules.register();
        AllMenuTypes.register();
        AllAssemblyRecipeNames.register();
        AllPotatoProjectileRenderModes.register();
        AllPotatoProjectileBlockHitActions.register();
        AllPotatoProjectileEntityHitActions.register();
        AllItemAttributeTypes.register();
        AllPackagePortTargetTypes.register();
        AllUnpackingHandlers.register();
        AllFuelTimes.register();
        AllStructureProcessorTypes.register();
        CreateNBTProcessors.register();
        AllFeatures.register();
        AllConfiguredFeatures.register();
        AllPlacedFeatures.register();
        AllPlacementModifiers.register();
        AllMountedDispenseItemBehaviors.register();
        AllBlockSpoutingBehaviours.register();
        // Optional mod integrations are registered after their NeoForge ports are restored.
    }
}
