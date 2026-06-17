package dev.merosssany.cinematica.networking.packet;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.loader.CinematicaProjectLoader;
import dev.merosssany.cinematica.core.data.loader.assets.SlideshowLoader;
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
    public static final CustomPacketPayload.Type<OpenSlideshowPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Cinematica.MODID, "open_slideshow"));
    
    public static final StreamCodec<FriendlyByteBuf, OpenSlideshowPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OpenSlideshowPacket::settings,
            OpenSlideshowPacket::new
    );
    
    public OpenSlideshowPacket(SlideshowSettings slideshow) {
        this(slideshow.name());
    }
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            SlideshowSettings slideshow = CinematicaProjectLoader.get(settings, SlideshowLoader.class);
            
            if (slideshow != null) {
                Minecraft.getInstance().setScreen(new SlideshowScreen(slideshow));
            }
        });
    }
}