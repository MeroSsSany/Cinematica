package dev.merosssany.cinematica.core.data.death;

import net.minecraft.world.entity.Entity;

public record DeathScreenContext(
        DeathScreenSettings settings,
        Entity entity,
        String deathMessage
) {}
