package dev.merosssany.cinematica.networking.packet;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.ClientDeathMemory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDeathContextPacket(int attackerId, String sceneId, String message) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncDeathContextPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Cinematica.MODID, "sync_death_context"));
    
    public static final StreamCodec<FriendlyByteBuf, SyncDeathContextPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncDeathContextPacket::attackerId,
            ByteBufCodecs.STRING_UTF8, SyncDeathContextPacket::sceneId,
            ByteBufCodecs.STRING_UTF8, SyncDeathContextPacket::message,
            SyncDeathContextPacket::new // Maps fields directly back into the record constructor
    );
    
    /**
     * Static Factory Constructor.
     * Use this method on the Server side to compile and instantiate the packet data seamlessly.
     */
    public static SyncDeathContextPacket create(String sceneId, int attackerId, DamageSource source, LivingEntity entity) {
        String localizedMessage = source.getLocalizedDeathMessage(entity).getString();
        return new SyncDeathContextPacket(attackerId, sceneId, localizedMessage);
    }
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            // Safe execution directly on the Minecraft main rendering thread layer
            ClientDeathMemory.pendingSceneId = this.sceneId;
            ClientDeathMemory.pendingAttackerId = this.attackerId;
            ClientDeathMemory.message = this.message;
        });
    }
}