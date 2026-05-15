package dev.merosssany.cinematica.core.data.slideshow;

import org.joml.Vector2f;

import java.util.List;

public record SlideshowSlide(
        String title,
        String subtext,
        boolean isImage,
        String assetPath,
        Vector2f anchor,
        int typingSpeed,
        KenBurnsOptions kenBurns,
        VignetteOptions vignette,
        String titleColor,
        String textColor,
        String backgroundColor,
        int secondsToSwitch,
        List<String> commands
) {}
