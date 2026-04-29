package dev.merosssany.cinematica.core.data.dialog;

public record DialogStage(
        String name,
        String entityType,
        String texture,
        String text,
        String audio,
        DialogOption[] options,
        int color,
        int time,
        boolean useTexture,
        DialogTextInput textInput
) {}