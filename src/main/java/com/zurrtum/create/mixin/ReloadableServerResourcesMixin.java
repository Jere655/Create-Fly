package com.zurrtum.create.mixin;

import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import net.minecraft.commands.Commands;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @Inject(method = "lambda$loadResources$3(Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;Ljava/util/List;Lnet/minecraft/server/permissions/PermissionSet;Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Lnet/minecraft/server/ReloadableServerRegistries$LoadResult;)Ljava/util/concurrent/CompletionStage;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private static void onReload(
        FeatureFlagSet enabledFeatures,
        Commands.CommandSelection commandSelection,
        List<?> updatedContextTags,
        PermissionSet functionCompilationPermissions,
        ResourceManager resourceManager,
        Executor backgroundExecutor,
        Executor mainThreadExecutor,
        ReloadableServerRegistries.LoadResult fullRegistries,
        CallbackInfoReturnable<CompletionStage<ReloadableServerResources>> cir
    ) {
        PotionRecipe.data = new PotionRecipe.ReloadData(fullRegistries.lookupWithUpdatedTags(), enabledFeatures);
    }
}
