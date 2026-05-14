package dev.merosssany.cinematica.registry.capablities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class CinematicCapProvider implements ICapabilitySerializable<CompoundTag> {
    public static Capability<ICinematicCap> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
    
    private final ICinematicCap backend = new CinematicCap();
    private final LazyOptional<ICinematicCap> optional = LazyOptional.of(() -> backend);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return INSTANCE.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("cinematicId", backend.getCinematicId());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.setCinematicId(nbt.getString("cinematicId"));
    }
}