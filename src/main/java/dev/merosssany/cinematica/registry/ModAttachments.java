package dev.merosssany.cinematica.registry;

import com.mojang.serialization.Codec;
import dev.merosssany.cinematica.core.Cinematica;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    // Replaces your old ResourceLocation "cinematic_id" capability setup
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = 
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Cinematica.MODID);

    // Define a persistent attachment that saves a simple String to NBT automatically
    public static final Supplier<AttachmentType<String>> CINEMATIC_ID = ATTACHMENTS.register(
            "cinematic_id",
            () -> AttachmentType.builder(() -> "") // Default value is an empty string
                    .serialize(Codec.STRING)      // Tells NeoForge how to save it to disk
                    .build()
    );

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}