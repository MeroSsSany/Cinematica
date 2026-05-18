package dev.merosssany.cinematica.core.data.death;

import net.minecraft.network.FriendlyByteBuf;

public record DeathScreenSettings(
        String name,
        boolean useDefaultSlideshow
) {
    public static DeathScreenSettings from(FriendlyByteBuf byteBuf) {
        return new DeathScreenSettings(
                byteBuf.readUtf(),
                byteBuf.readBoolean()
        );
    }
    
    public void write(FriendlyByteBuf byteBuf) {
        byteBuf.writeUtf(name);
        byteBuf.writeBoolean(useDefaultSlideshow);
    }
}
