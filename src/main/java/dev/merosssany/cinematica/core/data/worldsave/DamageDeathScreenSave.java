package dev.merosssany.cinematica.core.data.worldsave;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DamageDeathScreenSave(
        String deathScreenId,
        String damageResourceId
) {
    public static final Codec<DamageDeathScreenSave> CODEC = RecordCodecBuilder.create((in) -> in
            .group(
                    Codec.STRING.fieldOf("deathScreenId").forGetter(DamageDeathScreenSave::deathScreenId),
                    Codec.STRING.fieldOf("damageResourceId").forGetter(DamageDeathScreenSave::damageResourceId)
            ).apply(in, DamageDeathScreenSave::new)
    );
}
