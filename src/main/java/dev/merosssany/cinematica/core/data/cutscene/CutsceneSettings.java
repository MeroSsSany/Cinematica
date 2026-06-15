package dev.merosssany.cinematica.core.data.cutscene;

public record CutsceneSettings(
        String musicFile,
        String inheritSlideshow,
        CutsceneFrame[] frames
) {
}
