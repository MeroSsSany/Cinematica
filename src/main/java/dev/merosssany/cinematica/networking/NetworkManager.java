package dev.merosssany.cinematica.networking;

import dev.merosssany.cinematica.core.data.CinematicaSettings;
import dev.merosssany.cinematica.core.wrapper.NetworkPacketHandler;
import dev.merosssany.cinematica.networking.packet.SettingsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;

public class NetworkManager implements NetworkPacketHandler {
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
    }
    
    public <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }
    
    @Override
    public void sendSettings(CinematicaSettings settings, UUID playerUuid) {
    
    }
}
