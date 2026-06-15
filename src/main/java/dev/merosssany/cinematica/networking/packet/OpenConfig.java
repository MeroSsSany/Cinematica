package dev.merosssany.cinematica.networking.packet;

import dev.merosssany.cinematica.renderer.config.ScenesScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenConfig {
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new ScenesScreen());
        });
        ctx.get().setPacketHandled(true);
    }
    
    public OpenConfig() {}
    public OpenConfig(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}
}
