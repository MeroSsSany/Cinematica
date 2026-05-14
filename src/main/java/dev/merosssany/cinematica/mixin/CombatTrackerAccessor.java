package dev.merosssany.cinematica.mixin;

import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(CombatTracker.class)
public interface CombatTrackerAccessor {
    @Accessor("entries")
    List<CombatEntry> getCombatEntries();
    
    @Invoker("getMostSignificantFall")
    CombatEntry callGetMostSignificantFall();
}
