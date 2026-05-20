package dev.merosssany.cinematica.networking.packet;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.renderer.slideshow.SlideshowScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenSlideshowPacket {
    private final String settings;
    
    public OpenSlideshowPacket(SlideshowSettings slideshow) {
        settings = slideshow.name();
    }
    
    public OpenSlideshowPacket(FriendlyByteBuf byteBuf) {
        settings = byteBuf.readUtf();
    }
    
    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeUtf(settings);
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            SlideshowSettings slideshow = Cinematica.getSlideshow(settings);
            Minecraft.getInstance().setScreen(new SlideshowScreen(slideshow));
        });
        context.setPacketHandled(true);
    }
}
