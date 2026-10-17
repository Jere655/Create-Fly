package com.zurrtum.create.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior$TrackIteration")
public interface TrackIterationAccessor {
    @Accessor("firstIteration")
    boolean create$isFirstIteration();
}
