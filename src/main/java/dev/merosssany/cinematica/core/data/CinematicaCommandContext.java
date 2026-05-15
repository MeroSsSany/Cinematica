package dev.merosssany.cinematica.core.data;

import java.util.List;

public class CinematicaCommandContext {
    private final String[] params;
    private String failure;
    
    public CinematicaCommandContext(String[] params) {
        this.params = params;
    }
    
    public String[] params() {
        return params;
    }
    
    public void failure(String failure) {
        this.failure = failure;
    }
    
    public String failure() {
        return failure;
    }
}
