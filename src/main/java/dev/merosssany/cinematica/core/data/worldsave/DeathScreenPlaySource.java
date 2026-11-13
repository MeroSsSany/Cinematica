package dev.merosssany.cinematica.core.data.worldsave;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DeathScreenPlaySource(
        String deathScreenId,
        String sourceId
) {
    public static final Codec<DeathScreenPlaySource> CODEC = RecordCodecBuilder.create((in) -> in
            .group(
                    Codec.STRING.fieldOf("deathScreenId").forGetter(DeathScreenPlaySource::deathScreenId),
                    Codec.STRING.fieldOf("sourceId").forGetter(DeathScreenPlaySource::sourceId)
            ).apply(in, DeathScreenPlaySource::new)
    );
}
