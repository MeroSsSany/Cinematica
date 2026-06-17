package dev.merosssany.cinematica.core.data.cutscene;

import org.joml.Vector3d;
import org.joml.Vector3i;

public record CutsceneFrame(
        Vector3d[] position,
        Vector3i lookTo,
        double time,
        boolean paused,
        boolean containSounds
) {
}
