package dev.merosssany.cinematica.networking;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.networking.packet.OpenSlideshowPacket;
import dev.merosssany.cinematica.networking.packet.SyncDeathContextPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Cinematica.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkManager {
    
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0");
        
        registrar.playToClient(
                OpenSlideshowPacket.TYPE,
                OpenSlideshowPacket.STREAM_CODEC,
                OpenSlideshowPacket::handle
        );
        
        registrar.playToClient(
                SyncDeathContextPacket.TYPE,
                SyncDeathContextPacket.STREAM_CODEC,
                SyncDeathContextPacket::handle
        );
    }
    
    /**
     * Sends a packet from the Client up to the Server.
     */
    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }
    
    /**
     * Sends a packet from the Server down to a specific target player.
     */
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}