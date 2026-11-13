package dev.merosssany.cinematica.data;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.worldsave.CinematicaSavedData;
import dev.merosssany.cinematica.core.data.worldsave.CinematicaWorldSave;
import dev.merosssany.cinematica.core.data.worldsave.DamageDeathScreenSave;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.stream.Collectors;

public class SavedDamageTypes {
    private static CinematicaSavedData data;
    private static Map<String, String> saves;
    private static boolean saved;
    
    public static void load(MinecraftServer server) {
        if (saved) return;
        data = CinematicaSavedData.get(server);
        
        if (data == null) return;
        saves = data.getSaves().deathScreenSaves().stream().collect(Collectors.toMap(
                DamageDeathScreenSave::damageResourceId,
                DamageDeathScreenSave::deathScreenId,
                (a, b) -> b
        ));
        saved = true;
    }
    
    public static void add(String damageType, String deathScreenId) {
        saves.put(damageType, deathScreenId);
    }
    
    public static void remove(String damageType) {
        saves.remove(damageType);
    }
    
    public static void update() {
        data.setSaves(new CinematicaWorldSave(saves.entrySet().stream().map(e -> new DamageDeathScreenSave(e.getKey(), e.getValue())).toList()));
    }
    
    public static boolean contains(String id) {
        return saves.containsKey(id);
    }
    
    public static String get(String id) {
        return saves.get(id);
    }
    
    public static boolean isSaved() {
        return saved;
    }
    
    public static void dumpLogs() {
        Cinematica.getLogger().info(String.join(", ", saves.keySet()));
    }
}
