package dev.merosssany.cinematica.event;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.data.SavedDamageTypes;
import dev.merosssany.cinematica.networking.NetworkManager;
import dev.merosssany.cinematica.networking.packet.SyncDeathContextPacket;
import dev.merosssany.cinematica.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Cinematica.MODID, bus = EventBusSubscriber.Bus.GAME)
public class NeoForgeEvents {
    
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Entity attacker = event.getSource().getEntity();
            String id = event.getSource().typeHolder().getRegisteredName();
            
            if (attacker != null) {
                String sceneId = attacker.getData(ModAttachments.CINEMATIC_ID.get());
                
                NetworkManager.sendToPlayer(player,
                        SyncDeathContextPacket.create(
                                sceneId,
                                attacker.getId(),
                                event.getSource(),
                                event.getEntity()
                        )
                );
                return;
            }
            
            if (SavedDamageTypes.contains(id)) {
                String deathScreenId = SavedDamageTypes.get(id);
                Cinematica.getLogger().info("Sending '{}' death event to player {}", deathScreenId, player.getName());
                
                NetworkManager.sendToPlayer(player,
                        SyncDeathContextPacket.create(
                                deathScreenId,
                                0,
                                event.getSource(),
                                player
                        )
                );
            }
        }
    }
}