package dev.merosssany.cinematica.networking.packet;

import dev.merosssany.cinematica.core.data.ClientDeathMemory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncDeathContextPacket {
    private final String sceneId;
    private final int attackerId;
    private final String message; // Optional: for custom death messages

    public SyncDeathContextPacket(String sceneId, int attackerId, DamageSource source, LivingEntity entity) {
        this.sceneId = sceneId;
        this.attackerId = attackerId;
        this.message = source.getLocalizedDeathMessage(entity).getString();
    }
    
    public SyncDeathContextPacket(FriendlyByteBuf byteBuf) {
        attackerId = byteBuf.readInt();
        sceneId = byteBuf.readUtf();
        message = byteBuf.readUtf();
    }
    
    public static void handle(SyncDeathContextPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientDeathMemory.pendingSceneId = msg.sceneId;
            ClientDeathMemory.pendingAttackerId = msg.attackerId;
            ClientDeathMemory.message = msg.message;
        });
        ctx.get().setPacketHandled(true);
    }
    
    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeInt(attackerId);
        byteBuf.writeUtf(sceneId);
        byteBuf.writeUtf(message);
    }
}