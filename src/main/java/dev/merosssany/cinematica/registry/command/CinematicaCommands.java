package dev.merosssany.cinematica.registry.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.networking.NetworkManager;
import dev.merosssany.cinematica.networking.packet.OpenSlideshowPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class CinematicaCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("cinematica")
                        .then(
                                Commands.literal("open")
                                        .requires(src -> src.hasPermission(2))
                                        .then(
                                                Commands.literal("slideshow").then(
                                                        Commands.argument("name", new SlideshowCommandType())
                                                                .executes(CinematicaCommands::openSelf)
                                                                .then(
                                                                        Commands.argument("players", EntityArgument.players()).executes(CinematicaCommands::open)
                                                                )
                                                )
                                        )
                        )
        );
    }
    
    private static int open(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        SlideshowSettings settings = context.getArgument("name", SlideshowSettings.class);
        
        for (ServerPlayer player : players) {
            NetworkManager.sendToPlayer(player, new OpenSlideshowPacket(settings));
        }
        
        return Command.SINGLE_SUCCESS;
    }
    
    private static int openSelf(CommandContext<CommandSourceStack> context) {
        SlideshowSettings slideshow = context.getArgument("name", SlideshowSettings.class);
        
        NetworkManager.sendToPlayer(context.getSource().getPlayer(), new OpenSlideshowPacket(slideshow));
        
        return Command.SINGLE_SUCCESS;
    }
}
