package dev.merosssany.cinematica.networking.packet;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.registry.capablities.CinematicCapProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SelectedScenePacket {
    private final int entityId;
    private final String sceneId;
    
    public SelectedScenePacket(int entityId, String sceneId) {
        this.entityId = entityId;
        this.sceneId = sceneId;
    }
    
    public SelectedScenePacket(FriendlyByteBuf byteBuf) {
        entityId = byteBuf.readInt();
        sceneId = byteBuf.readUtf();
    }
    
    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeInt(entityId);
        byteBuf.writeUtf(sceneId);
    }
    
    public static void handle(SelectedScenePacket msg, Supplier<NetworkEvent.Context> ctx) {
        Cinematica.getLogger().info("Received Selected Scene");
        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);
            if (entity != null) {
                entity.getCapability(CinematicCapProvider.INSTANCE).ifPresent(cap -> {
                    cap.setCinematicId(msg.sceneId);
                });
            }
        });
        
        ctx.get().setPacketHandled(true);
    }
}