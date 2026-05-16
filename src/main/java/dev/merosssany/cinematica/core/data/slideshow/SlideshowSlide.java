package dev.merosssany.cinematica.core.data.slideshow;

import dev.merosssany.cinematica.core.data.RGBA;
import org.joml.Vector2f;

import java.util.List;

public record SlideshowSlide(
        String title,
        String subtext,
        String titleColor,
        String assetPath,
        String textColor,
        String backgroundColor,
        int typingSpeed,
        int secondsToSwitch,
        int radius,
        RGBA tint,
        Vector2f anchor,
        KenBurnsOptions kenBurns,
        VignetteOptions vignette,
        List<String> commands
) {}
