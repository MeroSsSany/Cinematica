package dev.merosssany.cinematica.registry;

import com.mojang.serialization.Codec;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.worldsave.CinematicaWorldSave;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = 
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Cinematica.MODID);

    public static final Supplier<AttachmentType<String>> CINEMATIC_ID = ATTACHMENTS.register(
            "cinematic_id",
            () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .build()
    );

    public static final Supplier<AttachmentType<CinematicaWorldSave>> CINEMATICA_WORLDSAVES = ATTACHMENTS.register(
            "cinematica_worldsaves",
            () -> AttachmentType.builder(CinematicaWorldSave::empty).build()
    );

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}