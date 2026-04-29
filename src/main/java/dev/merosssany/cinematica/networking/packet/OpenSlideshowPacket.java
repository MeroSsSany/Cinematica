package dev.merosssany.cinematica.networking.packet;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class OpenSlideshowPacket {
    private final SlideshowSettings settings;
    
    public OpenSlideshowPacket(SlideshowSettings slideshow) {
        settings = slideshow;
    }
    
    public OpenSlideshowPacket(FriendlyByteBuf byteBuf) {
        String json = byteBuf.toString(StandardCharsets.UTF_8);
        settings = SlideshowSettings.getGson().fromJson(json, SlideshowSettings.class);
    }
    
    public void encode(FriendlyByteBuf byteBuf) {
        try {
            byteBuf.writeUtf(settings.toJson().toString());
        } catch (IOException e) {
            Cinematica.getLogger().error("Failed to serialize SlideshowSettings",e);
        }
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // We will assume the player downloaded the assets
            Cinematica.register(settings);
        });
        context.setPacketHandled(true);
    }
}
