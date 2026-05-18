package dev.merosssany.cinematica.core.registry;

import dev.merosssany.cinematica.core.security.ObjectKey;

import java.util.Map;
import java.util.Set;

public class CinematicaRegistry<KEY, VALUE> {
    protected final Map<KEY, VALUE> entries;
    protected final ObjectKey key;
    protected boolean frozen;
    
    protected CinematicaRegistry(Map<KEY, VALUE> entries, ObjectKey key) {
        this.entries = entries;
        this.key = key;
    }
    
    public void register(KEY key, VALUE value) {
        if (frozen) throw new IllegalStateException("The registry is frozen!");
        entries.put(key, value);
    }
    
    public VALUE get(KEY key) {
        return entries.get(key);
    }
    
    public boolean isFrozen() {
        return frozen;
    }
    
    public void setFrozen(boolean frozen, ObjectKey key) {
        if (this.key != key) throw new SecurityException("Incorrect key");
        this.frozen = frozen;
    }
    
    public void clear(ObjectKey key) {
        if (this.key != key) throw new SecurityException("Incorrect key");
        entries.clear();
    }
    
    public boolean isRegistered(KEY key) {
        return entries.containsKey(key);
    }
    
    public Set<KEY> getRegistered() {
        return entries.keySet();
    }
}
