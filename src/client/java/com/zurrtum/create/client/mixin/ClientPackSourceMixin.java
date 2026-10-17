package com.zurrtum.create.client.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSelectionConfig;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(ClientPackSource.class)
public abstract class ClientPackSourceMixin {
    @Inject(method = "populatePackList(Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    private void loadResourcePack(BiConsumer<String, Function<String, Pack>> consumer, CallbackInfo ci) {
        IModFileInfo modFileInfo = ModList.get().getModFileById(MOD_ID);
        IModInfo modInfo = modFileInfo.getMods().stream()
            .filter(info -> info.getModId().equals(MOD_ID))
            .findFirst()
            .orElseThrow();
        IModFile modFile = modFileInfo.getFile();
        String version = modInfo.getVersion().toString();
        Path assetRoot = modFile.findResource(PackType.CLIENT_RESOURCES.getDirectory());

        consumer.accept(
            MOD_ID, id -> {
                PackLocationInfo info = createInfo(id, modInfo.getDisplayName(), id, version);
                BuiltInMetadata meta = createMeta(Component.translatable("advancement.create.root"));
                PackSelectionConfig position = new PackSelectionConfig(true, Pack.Position.BOTTOM, false);
                return createPack(assetRoot, info, meta, position, id, "minecraft", "flywheel", "vanillin", "ponder", "fabric");
            }
        );
        if (ModList.get().isLoaded("holdmyitems")) {
            Path assets = modFile.findResource("holdmyitems");
            if (Files.exists(assets)) {
                consumer.accept(
                    MOD_ID + "_holdmyitems", id -> {
                        PackLocationInfo info = createInfo(id, "Create & Hold My Items", MOD_ID, version);
                        BuiltInMetadata meta = createMeta(Component.literal("Hold My Items compatible addon"));
                        PackSelectionConfig position = new PackSelectionConfig(true, Pack.Position.TOP, false);
                        return createPack(assets, info, meta, position, "minecraft", MOD_ID);
                    }
                );
            }
        }
        Path legacyCopperAssets = modFile.findResource("legacy_copper");
        if (Files.exists(legacyCopperAssets)) {
            consumer.accept(
                MOD_ID + "_legacy_copper", id -> {
                    PackLocationInfo info = createInfo(id, "Create legacy copper", MOD_ID, version);
                    BuiltInMetadata meta = createMeta(Component.literal("Replacement textures for Vanilla Copper"));
                    PackSelectionConfig position = new PackSelectionConfig(false, Pack.Position.TOP, false);
                    return createPack(legacyCopperAssets, info, meta, position, "minecraft", MOD_ID);
                }
            );
        }
    }

    @Unique
    private static PackLocationInfo createInfo(String id, String name, String namespace, String version) {
        return new PackLocationInfo(
            id,
            Component.literal(name),
            PackSource.BUILT_IN,
            Optional.of(new KnownPack(namespace, id, version))
        );
    }

    @Unique
    private static BuiltInMetadata createMeta(Component description) {
        return BuiltInMetadata.of(
            PackMetadataSection.CLIENT_TYPE,
            new PackMetadataSection(
                description,
                SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).minorRange()
            )
        );
    }

    @Unique
    private static Pack createPack(
        Path assetRoot,
        PackLocationInfo info,
        BuiltInMetadata metadata,
        PackSelectionConfig position,
        String... namespaces
    ) {
        VanillaPackResourcesBuilder builder = new VanillaPackResourcesBuilder().setMetadata(metadata)
            .exposeNamespace(namespaces);
        builder.pushAssetPath(PackType.CLIENT_RESOURCES, assetRoot);
        VanillaPackResources resources = builder.build(info);
        Pack.ResourcesSupplier packFactory = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo packInfo) {
                return resources;
            }

            @Override
            public PackResources openFull(PackLocationInfo packInfo, Pack.Metadata packMetadata) {
                return resources;
            }
        };
        return Pack.readMetaAndCreate(info, packFactory, PackType.CLIENT_RESOURCES, position);
    }
}
