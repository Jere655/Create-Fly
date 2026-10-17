package com.zurrtum.create.client.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.zurrtum.create.client.flywheel.impl.FlwCommands;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow
    private CommandDispatcher<SharedSuggestionProvider> commands;

    @Inject(method = "handleCommands(Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket;)V", at = @At("TAIL"))
    private void addCommand(ClientboundCommandsPacket packet, CallbackInfo ci) {
        FlwCommands.registerClientCommands(commands);
    }
}
