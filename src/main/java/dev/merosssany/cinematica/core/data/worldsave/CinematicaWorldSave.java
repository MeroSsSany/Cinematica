package dev.merosssany.cinematica.core.data.worldsave;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record CinematicaWorldSave(
        List<DeathScreenPlaySource> damageSource,
        List<DeathScreenPlaySource> entitySource
) {
    public static final Codec<CinematicaWorldSave> CODEC = RecordCodecBuilder.create(in -> in
            .group(
                    DeathScreenPlaySource.CODEC.listOf().fieldOf("damageSource").forGetter(CinematicaWorldSave::damageSource),
                    DeathScreenPlaySource.CODEC.listOf().fieldOf("entitySource").forGetter(CinematicaWorldSave::entitySource)
            ).apply(in, CinematicaWorldSave::new)
    );
    
    public static CinematicaWorldSave empty() {
        return new CinematicaWorldSave(
                List.of(),
                List.of()
        );
    }
}
