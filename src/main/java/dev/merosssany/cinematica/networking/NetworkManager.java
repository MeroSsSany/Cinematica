package dev.merosssany.cinematica.networking;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.networking.packet.OpenConfig;
import dev.merosssany.cinematica.networking.packet.OpenSlideshowPacket;
import dev.merosssany.cinematica.networking.packet.SelectedScenePacket;
import dev.merosssany.cinematica.networking.packet.SyncDeathContextPacket;
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
                .named(ResourceLocation.fromNamespaceAndPath(Cinematica.modId, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        
        INSTANCE.messageBuilder(OpenSlideshowPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenSlideshowPacket::encode)
                .decoder(OpenSlideshowPacket::new)
                .consumerMainThread(OpenSlideshowPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(SelectedScenePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SelectedScenePacket::encode)
                .decoder(SelectedScenePacket::new)
                .consumerMainThread(SelectedScenePacket::handle)
                .add();
        
        INSTANCE.messageBuilder(SyncDeathContextPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncDeathContextPacket::encode)
                .decoder(SyncDeathContextPacket::new)
                .consumerMainThread(SyncDeathContextPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(OpenConfig.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenConfig::encode)
                .decoder(OpenConfig::new)
                .consumerMainThread(OpenConfig::handle)
                .add();
    }
    
    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }
    
    public static <MSG> void sendToPlayer(ServerPlayer player, MSG packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
