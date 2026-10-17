package com.zurrtum.create.client.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {
    @Invoker("affectedByCulling")
    boolean create$affectedByCulling(Entity entity);

    @Invoker("getBoundingBoxForCulling")
    AABB create$getBoundingBoxForCulling(Entity entity);
}
