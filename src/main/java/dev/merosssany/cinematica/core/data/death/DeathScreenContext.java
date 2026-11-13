package dev.merosssany.cinematica.core.data.death;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public record DeathScreenContext(
        DeathScreenSettings settings,
        @Nullable Entity entity,
        String deathMessage
) {}
