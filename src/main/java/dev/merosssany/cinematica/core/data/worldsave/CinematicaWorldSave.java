package dev.merosssany.cinematica.core.data.worldsave;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record CinematicaWorldSave(
        List<DamageDeathScreenSave> deathScreenSaves
) {
    public static final Codec<CinematicaWorldSave> CODEC = RecordCodecBuilder.create(in -> in
            .group(
                    DamageDeathScreenSave.CODEC.listOf().fieldOf("deathScreenSaves").forGetter(CinematicaWorldSave::deathScreenSaves)
            ).apply(in, CinematicaWorldSave::new)
    );
    
    public static CinematicaWorldSave empty() {
        return new CinematicaWorldSave(
                List.of()
        );
    }
}
