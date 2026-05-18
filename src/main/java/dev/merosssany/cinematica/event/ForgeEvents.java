package dev.merosssany.cinematica.event;

import dev.merosssany.cinematica.networking.NetworkManager;
import dev.merosssany.cinematica.networking.packet.SelectedScenePacket;
import dev.merosssany.cinematica.networking.packet.SyncDeathContextPacket;
import dev.merosssany.cinematica.registry.capablities.CinematicCapProvider;
import dev.merosssany.cinematica.registry.capablities.ICinematicCap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static dev.merosssany.cinematica.core.Cinematica.modId;

@Mod.EventBusSubscriber(modid = modId, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(modId, "cinematic_id"), new CinematicCapProvider());
        }
    }
    
    @SubscribeEvent
    public static void onPlayerTrackEntity(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        target.getCapability(CinematicCapProvider.INSTANCE).ifPresent(cap -> {
            String sceneId = cap.getCinematicId();
            if (!sceneId.isEmpty()) {
                // Send the packet to the player who just started seeing this entity
                NetworkManager.sendToPlayer((ServerPlayer) event.getEntity(),
                        new SelectedScenePacket(target.getId(), sceneId));
            }
        });
    }
    
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Entity attacker = event.getSource().getEntity();
            String sceneId = "";
            
            if (attacker != null) {
                sceneId = attacker.getCapability(CinematicCapProvider.INSTANCE)
                        .map(ICinematicCap::getCinematicId)
                        .orElse("");
            }
            
            NetworkManager.sendToPlayer(player, new SyncDeathContextPacket(sceneId, attacker != null ? attacker.getId() : -1, event.getSource(), event.getEntity()));
        }
    }
}
