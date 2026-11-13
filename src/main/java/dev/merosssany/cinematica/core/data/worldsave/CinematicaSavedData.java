package dev.merosssany.cinematica.core.data.worldsave;

import dev.merosssany.cinematica.core.Cinematica;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class CinematicaSavedData extends SavedData {
    public static final String NAME = "CinematicaWorldSaves";
    private static final SavedData.Factory<CinematicaSavedData> FACTORY = new SavedData.Factory<>(
            CinematicaSavedData::new,
            CinematicaSavedData::load,
            null
    );
    private CinematicaWorldSave saves;
    
    public CinematicaSavedData() {
        saves = CinematicaWorldSave.empty();
    }
    
    private CinematicaSavedData(CinematicaWorldSave saves) {
        this.saves = saves;
    }
    
    public CinematicaWorldSave getSaves() {
        return saves;
    }
    
    public void setSaves(CinematicaWorldSave saves) {
        this.saves = saves;
        setDirty();
    }
    
    @Override @NotNull
    public CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        CinematicaWorldSave.CODEC.encodeStart(NbtOps.INSTANCE, saves)
                .ifSuccess(encoded -> tag.put("CinematicaWorldSave", encoded));
        return tag;
    }
    
    public static CinematicaSavedData load(@NotNull CompoundTag tag, HolderLookup.Provider provider) {
         var saves = CinematicaWorldSave.CODEC.parse(NbtOps.INSTANCE, tag)
                .result()
                .orElseGet(() -> {
                    Cinematica.getLogger().error("Failed to load Cinematica world saves, falling back to default world saves.");
                    return CinematicaWorldSave.empty();
                });
         return new CinematicaSavedData(saves);
    }
    
    public static CinematicaSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }
}
