package dev.merosssany.cinematica.registry.command.cinematica;

import dev.merosssany.cinematica.core.data.CinematicaCommandContext;
import dev.merosssany.cinematica.core.data.ClientCameraMemory;
import dev.merosssany.cinematica.core.data.handler.CinematicaCommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class CameraPositionCommand implements CinematicaCommandHandler {
    @Override
    public boolean run(CinematicaCommandContext context) {
        try {
            // Validating the command
            String[] params = context.params();
            if (params.length < 7) {
                context.failure("Incorrect param size");
                return false;
            }
            
            // Setting the position of the camera
            ClientCameraMemory.x = parsePosition(context, params[0]);
            ClientCameraMemory.y = parsePosition(context, params[1]);
            ClientCameraMemory.z = parsePosition(context, params[2]);
            
            ClientCameraMemory.xRot = Double.parseDouble(params[3]);
            ClientCameraMemory.yRot = Double.parseDouble(params[4]);
            ClientCameraMemory.zRot = Double.parseDouble(params[5]);
            
            ClientCameraMemory.speed = Double.parseDouble(params[6]);
            ClientCameraMemory.render = true;
            
        } catch (NumberFormatException e) {
            context.failure("The parameters must be a decimal"); // PRINTED
        }
        
        return true;
    }
    
    private double parsePosition(CinematicaCommandContext context, String param) {
        // param is like: x$12 or 12
        
        if (param.matches("[xyz]\\$-?[0-9]+(\\.[0-9]+)?")) {
            String[] parts = param.split("\\$");
            
            if (parts.length > 1) {
                LocalPlayer player = Minecraft.getInstance().player;
                
                double position = switch (parts[0]) {
                    case "x" -> player.getX();
                    case "y" -> player.getY();
                    case "z" -> player.getZ();
                    default -> {
                        context.failure("Invalid Position, defaulting to 0 for "+parts[0]);
                        yield 0;
                    }
                };
                
                double providedPosition = Double.parseDouble(parts[1]);
                return position + providedPosition;
            } else context.failure("Invalid Position. Must be at least 2 sides of the \"$\", got: "+param);
        } else {
            return Double.parseDouble(param);
        }
        
        return 0;
    }
}
