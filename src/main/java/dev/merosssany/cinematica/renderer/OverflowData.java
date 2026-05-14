package dev.merosssany.cinematica.renderer;

import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public record OverflowData(
        List<FormattedCharSequence> lines,
        int height,
        int[] lengths
) {}
