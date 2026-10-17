package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TerrainParticle.class)
public abstract class TerrainParticleMixin {
    @Unique
    private static final Vec3i[] DIRECTIONS = new Vec3i[]{
        new Vec3i(0, 0, -1),
        new Vec3i(0, 0, 1),
        new Vec3i(-1, 0, 0),
        new Vec3i(1, 0, 0),
        new Vec3i(-1, 0, -1),
        new Vec3i(1, 0, -1),
        new Vec3i(1, 0, 1),
        new Vec3i(-1, 0, 1),
        new Vec3i(0, -1, 0),
        new Vec3i(0, 1, 0),
        new Vec3i(0, -1, -1),
        new Vec3i(0, -1, 1),
        new Vec3i(-1, -1, 0),
        new Vec3i(1, -1, 0),
        new Vec3i(-1, -1, -1),
        new Vec3i(1, -1, -1),
        new Vec3i(1, -1, 1),
        new Vec3i(-1, -1, 1)
    };

    @Unique
    private static BlockPos findPos(ClientLevel world, BlockPos pos, BlockState state) {
        BlockState target = world.getBlockState(pos);
        if (target == state) {
            return pos;
        }
        BlockPos.MutableBlockPos position = pos.mutable();
        for (Vec3i move : DIRECTIONS) {
            target = world.getBlockState(position.setWithOffset(pos, move));
            if (target == state) {
                return position;
            }
        }
        return pos;
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getParticleIcon(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private static TextureAtlasSprite onParticle(
        BlockModelShaper models,
        BlockState state,
        Operation<TextureAtlasSprite> original,
        @Local(argsOnly = true) ClientLevel world,
        @Local(argsOnly = true) BlockPos pos,
        @Share("pos") LocalRef<BlockPos> blockPos
    ) {
        BlockStateModel model = models.getBlockModel(state);
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            blockPos.set(findPos(world, pos, state));
            return wrapper.particleSpriteWithInfo(world, blockPos.get(), state);
        } else {
            return model.particleIcon();
        }
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/block/BlockColors;getColor(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;I)I"))
    private int copycatParticleColor(
        BlockColors colors,
        BlockState state,
        BlockAndTintGetter level,
        BlockPos pos,
        int tintIndex,
        Operation<Integer> original,
        @Local(argsOnly = true) ClientLevel world,
        @Share("pos") LocalRef<BlockPos> blockPos
    ) {
        if (state.getBlock() instanceof CopycatBlock) {
            BlockPos actualPos = blockPos.get();
            if (actualPos == null) {
                actualPos = pos;
            }

            BlockState material = CopycatBlock.getMaterial(world, actualPos);

            if (material.is(Blocks.GRASS_BLOCK)) {
                return 0xFFFFFF;
            }

            return original.call(colors, material, level, actualPos, tintIndex);
        }

        return original.call(colors, state, level, pos, tintIndex);
    }
}
