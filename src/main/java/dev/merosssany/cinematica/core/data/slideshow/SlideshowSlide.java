package dev.merosssany.cinematica.core.data.slideshow;

import org.joml.Vector2f;

import java.io.File;

public record SlideshowSlide(
        String title,
        String subtext,
        boolean isImage,
        File assetPath, // Renamed from imagePath for consistency
        Vector2f anchor
) {}
