package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.foundation.block.SoundControlBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelEventHandler.class)
public class LevelEventHandlerMixin {
    @Shadow
    @Final
    private ClientLevel level;

    @WrapOperation(method = "levelEvent(ILnet/minecraft/core/BlockPos;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType getBreakSound(
        BlockState state,
        LevelReader world,
        BlockPos pos,
        Entity entity,
        Operation<SoundType> original
    ) {
        if (state.getBlock() instanceof SoundControlBlock block) {
            return block.getSoundGroup(level, pos);
        }
        return original.call(state, world, pos, entity);
    }
}
