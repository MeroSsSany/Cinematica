package dev.merosssany.cinematica.event;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.ClientCameraMemory;
import dev.merosssany.cinematica.core.data.ClientDeathMemory;
import dev.merosssany.cinematica.core.data.death.DeathScreenContext;
import dev.merosssany.cinematica.core.data.death.DeathScreenSettings;
import dev.merosssany.cinematica.mixin.CameraAccessor;
import dev.merosssany.cinematica.renderer.slideshow.CineDeathScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Cinematica.modId, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST) // making sure Cinematica opens its scene the last
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof DeathScreen && !(event.getScreen() instanceof CineDeathScreen)) {
            // Did the server tell us a specific cinematic to play?
            if (!ClientDeathMemory.pendingSceneId.isEmpty() && Cinematica.SlideshowExists(ClientDeathMemory.pendingSceneId)) {
                
                DeathScreenSettings settings = Cinematica.deathScreenRegistry().get(ClientDeathMemory.pendingSceneId);
                Entity attacker = Minecraft.getInstance().level.getEntity(ClientDeathMemory.pendingAttackerId);
                
                // We pass the data to custom screen
                event.setNewScreen(new CineDeathScreen(new DeathScreenContext(settings, attacker, ClientDeathMemory.message)));
                
                // Clear the memory so it doesn't trigger again on the next death
                ClientDeathMemory.pendingSceneId = "";
                ClientDeathMemory.pendingAttackerId = -1;
                ClientDeathMemory.message = "";
            }
        }
    }
    
    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (ClientCameraMemory.render) {
            Camera camera = event.getCamera();
            CameraAccessor acc = (CameraAccessor) camera;
            
            // Current Camera Position
            Vec3 currentPos = camera.getPosition();
            
            // Target Position from your Memory
            Vec3 targetPos = new Vec3(ClientCameraMemory.x, ClientCameraMemory.y, ClientCameraMemory.z);
            
            // Calculate the distance and move a small step towards it based on speed
            if (currentPos.distanceTo(targetPos) > 0.01 && ClientCameraMemory.speed != 0) {
                Vec3 newPos = currentPos.lerp(targetPos, ClientCameraMemory.speed);
                acc.callSetPosition(newPos.x, newPos.y, newPos.z);
            } else {
                acc.callSetPosition(targetPos.x, targetPos.y, targetPos.z);
            }
            
            acc.callSetRotation((float)ClientCameraMemory.yRot, (float)ClientCameraMemory.xRot);
        }
    }
}
