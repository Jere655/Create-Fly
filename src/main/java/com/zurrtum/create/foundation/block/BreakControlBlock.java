package com.zurrtum.create.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;

public interface BreakControlBlock extends IBlockExtension {
    boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player);

    @Override
    default boolean onDestroyedByPlayer(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        ItemStack toolStack,
        boolean willHarvest,
        FluidState fluid
    ) {
        return (player == null || onDestroyedByPlayer(state, level, pos, player))
            && IBlockExtension.super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
    }
}
