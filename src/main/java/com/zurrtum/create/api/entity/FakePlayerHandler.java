package com.zurrtum.create.api.entity;

import com.zurrtum.create.infrastructure.player.FakePlayerEntity;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.FakePlayer;

public interface FakePlayerHandler {
    static boolean has(Entity player) {
        return player instanceof FakePlayer || player instanceof FakePlayerEntity;
    }
}
