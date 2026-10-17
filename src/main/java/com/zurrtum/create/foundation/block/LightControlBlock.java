package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;

public interface LightControlBlock extends IBlockExtension {
    int getLuminance(BlockGetter world, BlockPos pos);

    @Override
    default boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    @Override
    default int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return getLuminance(level, pos);
    }
}
