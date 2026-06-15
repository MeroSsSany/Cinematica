package dev.merosssany.cinematica.event;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.networking.NetworkManager;
import dev.merosssany.cinematica.networking.packet.SyncDeathContextPacket;
import dev.merosssany.cinematica.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = Cinematica.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ForgeEvents {
    
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Entity attacker = event.getSource().getEntity();
            String sceneId = "";
            
            if (attacker != null) {
                sceneId = attacker.getData(ModAttachments.CINEMATIC_ID.get());
            }
            
            NetworkManager.sendToPlayer(player,
                    SyncDeathContextPacket.create(
                            sceneId,
                            attacker != null ? attacker.getId() : -1,
                            event.getSource(),
                            event.getEntity()
                    )
            );
        }
    }
}