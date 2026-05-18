package dev.merosssany.cinematica.core.registry.settings;

import dev.merosssany.cinematica.core.security.ObjectKey;
import dev.merosssany.cinematica.core.data.scrollingtext.CreditsSettings;
import dev.merosssany.cinematica.core.registry.CinematicaRegistry;

import java.util.concurrent.ConcurrentHashMap;

public class CreditScreenRegistry extends CinematicaRegistry<String, CreditsSettings> {
    public CreditScreenRegistry(ObjectKey key) {
        super(new ConcurrentHashMap<>(), key);
    }
    
    public void register(CreditsSettings creditsSettings) {
        register(creditsSettings.name(), creditsSettings);
    }
}
