package com.zurrtum.create.infrastructure.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

/** Create-specific fake-player behavior built on NeoForge's native fake player. */
public class FakePlayerEntity extends FakePlayer {
    public FakePlayerEntity(ServerLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel world, DamageSource damageSource) {
        return true;
    }

    @Nullable
    @Override
    public PlayerTeam getTeam() {
        // Scoreboard team is checked using the game profile name by default; fake players are not members.
        return null;
    }

    @Override
    public void startSleeping(BlockPos pos) {
        // A simulated player must never keep a bed occupied.
    }

    @Override
    public void openTextEdit(SignBlockEntity sign, boolean front) {
    }

    @Override
    public OptionalInt openMenu(@Nullable MenuProvider menuProvider) {
        return OptionalInt.empty();
    }

    @Override
    public void openHorseInventory(AbstractHorse horse, Container inventory) {
    }

    @Override
    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
        return false;
    }
}
