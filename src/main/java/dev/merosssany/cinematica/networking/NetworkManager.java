package dev.merosssany.cinematica.networking;

import dev.merosssany.cinematica.networking.packet.OpenSlideshowPacket;
import dev.merosssany.cinematica.networking.packet.SettingsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }
    
    public static void register() {
        INSTANCE = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("cinematica", "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        
        INSTANCE.messageBuilder(SettingsPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SettingsPacket::encode)
                .decoder(SettingsPacket::new)
                .consumerMainThread(SettingsPacket::run)
                .add();
        
        INSTANCE.messageBuilder(OpenSlideshowPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenSlideshowPacket::encode)
                .decoder(OpenSlideshowPacket::new)
                .consumerMainThread(OpenSlideshowPacket::handle)
                .add();
    }
    
    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }
    
    public static <MSG> void sendToPlayer(ServerPlayer player, MSG packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
