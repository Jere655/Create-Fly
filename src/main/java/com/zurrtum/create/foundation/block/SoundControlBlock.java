package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;

public interface SoundControlBlock extends IBlockExtension {
    SoundType getSoundGroup(LevelReader level, BlockPos pos);

    @Override
    default SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
        return getSoundGroup(level, pos);
    }
}
