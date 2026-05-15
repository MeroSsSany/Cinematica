package dev.merosssany.cinematica.registry.command.cinematica;

import dev.merosssany.cinematica.core.data.CinematicaCommandContext;
import dev.merosssany.cinematica.core.data.handler.CinematicaCommandHandler;
import net.minecraft.client.Minecraft;

import java.util.Arrays;

public class MinecraftCommandHandler implements CinematicaCommandHandler {
    @Override
    public boolean run(CinematicaCommandContext context) {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(context.params()).forEach(builder::append);
        return Minecraft.getInstance().player.connection.sendUnsignedCommand(builder.toString());
    }
}
