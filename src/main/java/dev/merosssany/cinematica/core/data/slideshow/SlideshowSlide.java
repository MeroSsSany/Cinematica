package dev.merosssany.cinematica.core.data.slideshow;

import org.joml.Vector2f;

import java.io.File;

public record SlideshowSlide(
        String title,
        String subtext,
        boolean isImage,
        File assetPath,
        Vector2f anchor,
        int typingSpeed,
        KenBurnsOptions kenBurns,
        VignetteOptions vignette,
        String titleColor,
        String textColor,
        String backgroundColor,
        int secondsToSwitch
) {}
