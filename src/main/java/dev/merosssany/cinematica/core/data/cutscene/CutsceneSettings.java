package dev.merosssany.cinematica.core.data.cutscene;

import dev.merosssany.cinematica.core.data.CinematicaAsset;

public record CutsceneSettings(
        String name,
        String musicFile,
        String inheritSlideshow,
        CutsceneFrame[] frames,
        int[] showAtStage
) implements CinematicaAsset {
}
