package dev.merosssany.cinematica.networking.packet;

import dev.merosssany.cinematica.core.data.CinematicaSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SettingsPacket {
    
    
    public SettingsPacket(FriendlyByteBuf byteBuf) {
    
    }
    
    public SettingsPacket(CinematicaSettings settings) {
    
    }
    
    public void encode(FriendlyByteBuf byteBuf) {
    
    }
    
    public void run(Supplier<NetworkEvent.Context> contextSupplier) {
    
    }
}
