package dev.merosssany.cinematica.core.data.death;

import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import net.minecraft.world.entity.Entity;

public record DeathScreenContext(
        SlideshowSettings settings,
        Entity entity,
        String deathMessage
) {}
