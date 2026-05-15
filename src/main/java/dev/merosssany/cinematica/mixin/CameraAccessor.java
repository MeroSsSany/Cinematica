package dev.merosssany.cinematica.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor("position")
    @Mutable
    void setPos(Vec3 pos);
    
    @Invoker("setPosition")
    void callSetPosition(double x, double y, double z);
    
    @Invoker("setRotation")
    void callSetRotation(float yRot, float xRot);
}