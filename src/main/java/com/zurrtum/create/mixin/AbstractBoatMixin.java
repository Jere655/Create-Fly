package com.zurrtum.create.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractBoat.class)
public abstract class AbstractBoatMixin extends Entity {
    public AbstractBoatMixin(EntityType<?> type, Level world) {
        super(type, world);
    }
}
