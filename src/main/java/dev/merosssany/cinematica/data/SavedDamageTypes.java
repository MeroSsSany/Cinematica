package dev.merosssany.cinematica.data;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.worldsave.CinematicaSavedData;
import dev.merosssany.cinematica.core.data.worldsave.CinematicaWorldSave;
import dev.merosssany.cinematica.core.data.worldsave.DeathScreenPlaySource;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SavedDamageTypes {
    private static CinematicaSavedData data;
    private static Map<String, String> damageSaves;
    private static Map<String, String> entitySaves;
    private static boolean saved;
    
    public static void load(MinecraftServer server) {
        if (saved) return;
        data = CinematicaSavedData.get(server);
        
        damageSaves = data.getSaves().damageSource().stream().collect(Collectors.toMap(
                DeathScreenPlaySource::sourceId,
                DeathScreenPlaySource::deathScreenId,
                (a, b) -> b
        ));
        entitySaves = data.getSaves().entitySource().stream().collect(Collectors.toMap(
                DeathScreenPlaySource::sourceId,
                DeathScreenPlaySource::deathScreenId
        ));
        saved = true;
    }
    
    public static void add(String damageType, String deathScreenId) {
        damageSaves.put(damageType, deathScreenId);
    }
    
    public static void remove(String damageType) {
        damageSaves.remove(damageType);
    }
    
    public static void update() {
        data.setSaves(new CinematicaWorldSave(
                fromMap(damageSaves),
                fromMap(entitySaves)
        ));
    }
    
    private static List<DeathScreenPlaySource> fromMap(Map<String, String> map) {
        return damageSaves.entrySet().stream().map(e -> new DeathScreenPlaySource(e.getKey(), e.getValue())).toList();
    }
    
    public static boolean contains(String id) {
        return damageSaves.containsKey(id);
    }
    
    public static String get(String id) {
        return damageSaves.get(id);
    }
    
    public static boolean isSaved() {
        return saved;
    }
    
    private static void clean() {
        data = null;
        damageSaves = null;
        entitySaves = null;
        saved = false;
    }
    
    @SubscribeEvent
    public static void onServerClosed(ServerStoppedEvent event) {
        clean();
    }
    
    public static void addEntity(String entity, String deathScreen) {
        entitySaves.put(entity, deathScreen);
    }
    
    public static boolean containsEntity(String entity) {
        return entitySaves.containsKey(entity);
    }
    
    public static String getSceneIdFromEntity(String entity) {
        return entitySaves.get(entity);
    }
}
