package dev.merosssany.cinematica.renderer.cutscene;

import dev.merosssany.cinematica.core.animation.RotationManager;
import dev.merosssany.cinematica.core.data.cutscene.CutsceneFrame;
import dev.merosssany.cinematica.core.data.cutscene.CutsceneSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.joml.Vector3d;
import org.joml.Vector3i;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Cutscene {
    protected final CutsceneSettings settings;
    protected final Vector3i origin;
    protected CutsceneScreen screen;
    protected int frame;
    protected boolean isFinished = false;
    
    private double lastTime;
    private double timeElapsed;
    private ArmorStand camera;
    private final Vector3d cachePos = new Vector3d();
    private final Vector3d cacheRotation = new Vector3d();
    private final float[] targetFloats = new float[2];
    private boolean screenOpened = false;
    
    public Cutscene(CutsceneSettings settings, Vector3i origin) {
        this.settings = settings;
        this.origin = origin;
        
        if (settings.inheritSlideshow() != null && !settings.inheritSlideshow().isEmpty()) {
            screen = new CutsceneScreen(settings);
        }
        
        lastTime = glfwGetTime();
    }
    
    public void update() {
        if (isFinished) return;
        
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        
        if (screen != null && !screenOpened) {
            minecraft.setScreen(screen);
            screenOpened = true;
        }
        
        if (camera == null) {
            camera = new ArmorStand(EntityType.ARMOR_STAND, minecraft.level);
            camera.setNoGravity(true);
            camera.setInvisible(true);
            minecraft.setCameraEntity(camera);
        }
        
        double current = glfwGetTime();
        double delta = current - lastTime;
        lastTime = current;
        timeElapsed += delta;
        
        if (frame >= settings.frames().length) {
            this.finishCutscene(minecraft);
            return;
        }
        
        CutsceneFrame currentFrame = settings.frames()[frame];
        Vector3d[] pos = currentFrame.position();
        double progress = Math.min(1.0, timeElapsed / currentFrame.time());
        
        RotationManager.getCubicBezierPoint(cachePos, pos[0], pos[1], pos[2], pos[3], progress);
        
        double targetX = cachePos.x + origin.x;
        double targetY = cachePos.y + origin.y;
        double targetZ = cachePos.z + origin.z;
        
        camera.setPos(targetX, targetY, targetZ);
        camera.xo = targetX;
        camera.yo = targetY;
        camera.zo = targetZ;
        
        if (currentFrame.lookTo() != null) {
            RotationManager.calculateBlockLookRotations(targetFloats, camera,
                    currentFrame.lookTo().x, currentFrame.lookTo().y, currentFrame.lookTo().z);
        } else {
            cacheRotation.set(0);
            RotationManager.getCubicBezierDerivative(cacheRotation, pos[0], pos[1], pos[2], pos[3], progress);
            RotationManager.calculateRotations(targetFloats, cacheRotation);
        }
        
        float smoothFactor = (float) (1.0 - Math.exp(-10.0f * delta));
        float blendedYaw = Mth.rotLerp(smoothFactor, camera.getYRot(), targetFloats[0]);
        float blendedPitch = Mth.rotLerp(smoothFactor, camera.getXRot(), targetFloats[1]);
        
        camera.setYRot(blendedYaw);
        camera.setXRot(blendedPitch);
        camera.yRotO = blendedYaw;
        camera.xRotO = blendedPitch;
        
        if (progress >= 1.0) {
            frame++;
            timeElapsed = 0;
            
            if (frame >= settings.frames().length) {
                this.finishCutscene(minecraft);
            }
        }
    }
    
    private void finishCutscene(Minecraft mc) {
        this.isFinished = true;
        if (mc.player != null) {
            mc.setCameraEntity(mc.player);
        }
        if (camera != null) {
            camera.discard();
        }
        
        if (screen != null) {
            if (mc.screen == screen) {
                mc.setScreen(null);
            } else {
                screen.onClose();
            }
        }
    }
    
    public boolean isFinished() {
        return this.isFinished;
    }
}