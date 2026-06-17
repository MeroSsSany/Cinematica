package dev.merosssany.cinematica.core.registry.settings;

import dev.merosssany.cinematica.core.data.dialog.DialogSettings;
import dev.merosssany.cinematica.core.registry.CinematicaRegistry;
import dev.merosssany.cinematica.core.security.ObjectKey;

import java.util.concurrent.ConcurrentHashMap;

public class DialogRegistry extends CinematicaRegistry<String, DialogSettings> {
    public DialogRegistry(ObjectKey key) {
        super(new ConcurrentHashMap<>(), key);
    }
    
    public void register(DialogSettings resource) {
        register(resource.name(), resource);
    }
}
