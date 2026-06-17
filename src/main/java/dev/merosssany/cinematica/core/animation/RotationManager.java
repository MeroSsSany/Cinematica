package dev.merosssany.cinematica.core.animation;

import net.minecraft.world.entity.Entity;
import org.joml.Vector3d;

public class RotationManager {
    public static Vector3d getCubicBezierPoint(Vector3d p0, Vector3d p1, Vector3d p2, Vector3d p3, double t) {
        return getCubicBezierPoint(new Vector3d(), p0, p1, p2, p3, t);
    }
    
    public static Vector3d getCubicBezierPoint(Vector3d target, Vector3d p0, Vector3d p1, Vector3d p2, Vector3d p3, double t) {
        // Clamp t to prevent weird behavior outside the bounds
        t = Math.max(0.0, Math.min(1.0, t));
        
        double u = 1.0 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;
        
        // Apply the polynomial Bezier formula: B(t) = (1-t)^3*P0 + 3(1-t)^2*t*P1 + 3(1-t)*t^2*P2 + t^3*P3
        double x = uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p3.x;
        double y = uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p3.y;
        double z = uuu * p0.z + 3 * uu * t * p1.z + 3 * u * tt * p2.z + ttt * p3.z;
        
        target.set(x, y, z);
        return target;
    }
    
    /**
     * Calculates the direction (tangent vector) of a Cubic Bézier curve at progress t.
     * @return A normalized Vector3d pointing in the exact direction of travel.
     */
    public static Vector3d getCubicBezierDerivative(Vector3d target, Vector3d p0, Vector3d p1, Vector3d p2, Vector3d p3, double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        double u = 1.0 - t;
        
        // The derivative formula components
        double x = 3 * u * u * (p1.x - p0.x) + 6 * u * t * (p2.x - p1.x) + 3 * t * t * (p3.x - p2.x);
        double y = 3 * u * u * (p1.y - p0.y) + 6 * u * t * (p2.y - p1.y) + 3 * t * t * (p3.y - p2.y);
        double z = 3 * u * u * (p1.z - p0.z) + 6 * u * t * (p2.z - p1.z) + 3 * t * t * (p3.z - p2.z);
        
        return target.set(x, y, z).normalize(); // Normalized so it's a pure direction vector
    }
    
    /**
     * Converts a 3D direction vector into Minecraft-friendly Yaw and Pitch rotations.
     * @param dir The normalized tangent vector from the derivative function.
     * @return A float array where [0] is Yaw and [1] is Pitch (in degrees).
     */
    public static float[] calculateRotations(float[] floats, Vector3d dir) {
        // Calculate Yaw (Left/Right)
        // Minecraft's 0 degrees Yaw points south (+Z), so we use atan2(-x, z)
        double yawRadians = Math.atan2(-dir.x, dir.z);
        float yaw = (float) Math.toDegrees(yawRadians);
        
        // Calculate Pitch (Up/Down)
        // dir.y is the vertical change. we use asin to get the tilt angle.
        double pitchRadians = -Math.asin(dir.y);
        float pitch = (float) Math.toDegrees(pitchRadians);
        
        floats[0] = yaw;
        floats[1] = pitch;
        
        return floats;
    }
    
    public static void faceBlock(Entity entity, int x, int y, int z) {
        double deltaX = (x + 0.5) - entity.getX();
        double deltaY = (y + 0.5) - entity.getEyeY();
        double deltaZ = (z + 0.5) - entity.getZ();
        
        double yawRadians = Math.atan2(-deltaX, deltaZ);
        float yaw = (float) Math.toDegrees(yawRadians);
        
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        // Pitch uses atan2(-y, horizontalDistance)
        double pitchRadians = -Math.atan2(deltaY, horizontalDistance);
        float pitch = (float) Math.toDegrees(pitchRadians);
        
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yRotO = yaw; // Update previous tick rotation to prevent visual stutter
        entity.xRotO = pitch;
    }
    
    
    public static void calculateBlockLookRotations(float[] targetFloats, Entity entity, int x, int y, int z) {
        double deltaX = (x + 0.5) - entity.getX();
        double deltaY = (y + 0.5) - entity.getEyeY();
        double deltaZ = (z + 0.5) - entity.getZ();
        
        double yawRadians = Math.atan2(-deltaX, deltaZ);
        float yaw = (float) Math.toDegrees(yawRadians);
        
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double pitchRadians = -Math.atan2(deltaY, horizontalDistance);
        float pitch = (float) Math.toDegrees(pitchRadians);
        
        targetFloats[0] = yaw;
        targetFloats[1] = pitch;
    }
}
