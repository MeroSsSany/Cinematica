package dev.merosssany.cinematica.registry.capablities;

public class CinematicCap implements ICinematicCap {
    private String cinematicId = "";

    @Override
    public String getCinematicId() { return cinematicId; }

    @Override
    public void setCinematicId(String id) { this.cinematicId = id; }
}
