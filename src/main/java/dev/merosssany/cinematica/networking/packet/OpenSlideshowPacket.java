package dev.merosssany.cinematica.networking.packet;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.renderer.slideshow.SlideshowScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenSlideshowPacket(String settings) implements CustomPacketPayload {
    
    // 1. UNIQUE IDENTIFIER
    // Replaces the old integer packet IDs with a strict, unique resource location type
    public static final CustomPacketPayload.Type<OpenSlideshowPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Cinematica.MODID, "open_slideshow"));
    
    // 2. UNIFIED STREAM CODEC
    // Replaces the separate manual encode() and decode() buffer loops with a modern composite stream pipeline
    public static final StreamCodec<FriendlyByteBuf, OpenSlideshowPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OpenSlideshowPacket::settings,
            OpenSlideshowPacket::new
    );
    
    // Convenience constructor so your server code can still just pass a settings object directly
    public OpenSlideshowPacket(SlideshowSettings slideshow) {
        this(slideshow.name());
    }
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    // 3. UPDATED MODERN NETWORK CONTEXT HANDLER
    public void handle(IPayloadContext context) {
        // Enqueue the packet logic onto the main game client rendering thread safely
        context.enqueueWork(() -> {
            SlideshowSettings slideshow = Cinematica.getSlideshow(this.settings);
            
            if (slideshow != null) {
                Minecraft.getInstance().setScreen(new SlideshowScreen(slideshow));
            }
        });
    }
}