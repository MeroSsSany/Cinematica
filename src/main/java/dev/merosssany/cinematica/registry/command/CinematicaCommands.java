package dev.merosssany.cinematica.registry.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.merosssany.cinematica.core.data.death.DeathScreenSettings;
import dev.merosssany.cinematica.core.security.ObjectKey;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.networking.NetworkManager;
import dev.merosssany.cinematica.networking.packet.OpenConfig;
import dev.merosssany.cinematica.networking.packet.OpenSlideshowPacket;
import dev.merosssany.cinematica.networking.packet.SelectedScenePacket;
import dev.merosssany.cinematica.core.registry.CinematicaRegistries;
import dev.merosssany.cinematica.registry.capablities.CinematicCapProvider;
import dev.merosssany.cinematica.registry.capablities.ICinematicCap;
import dev.merosssany.cinematica.registry.command.cinematica.CameraPositionCommand;
import dev.merosssany.cinematica.registry.command.cinematica.MinecraftCommandHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.LazyOptional;

import java.io.IOException;
import java.util.Collection;

public class CinematicaCommands {
    private static ObjectKey key;
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, ObjectKey key) {
        registerCinematicaCommands();
        CinematicaCommands.key = key;
        
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
                        ).then(
                                Commands.literal("attach")
                                        .requires(src -> src.hasPermission(2))
                                        .then(
                                                Commands.argument("entities", EntityArgument.entities()).then(
                                                        Commands.argument("deathscreen", new DeathScreenCommandType())
                                                                .executes(CinematicaCommands::attach)
                                                )
                                        )
                        ).then(
                                Commands.literal("get")
                                        .requires(src -> src.hasPermission(2))
                                        .then(
                                                Commands.literal("slideshow").then(
                                                        Commands.argument("name", new SlideshowCommandType())
                                                                .executes(CinematicaCommands::getSlideshow)
                                                )
                                        ).then(
                                                Commands.literal("deathscreen").then(
                                                        Commands.argument("entity", EntityArgument.entity())
                                                                .executes(CinematicaCommands::getDeathScreen)
                                                )
                                        )
                        ).then(
                                Commands.literal("refresh")
                                        .requires(src -> src.hasPermission(2))
                                        .executes(CinematicaCommands::refresh)
                        ).then(
                                Commands.literal("debug")
                                        .executes(CinematicaCommands::debug)
                        ).then(
                                Commands.literal("config")
                                        .requires(stack -> stack.hasPermission(2))
                                        .executes(CinematicaCommands::config)
                        )
        );
    }
    
    private static int config(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        
        if (player != null) NetworkManager.sendToPlayer(player, new OpenConfig());
        
        return Command.SINGLE_SUCCESS;
    }
    
    private static int debug(CommandContext<CommandSourceStack> context) {
        Cinematica.debug = !Cinematica.debug;
        context.getSource().sendSuccess(() -> Component.literal("Cinematica debugging has been "+(Cinematica.debug? "enabled":"disabled")), false);
        return Command.SINGLE_SUCCESS;
    }
    
    private static int refresh(CommandContext<CommandSourceStack> context) {
        try {
            context.getSource().sendSuccess(() -> Component.literal("Reloading..."), false);
            Cinematica.LoadDetail[] details = Cinematica.reloadAll(key);
            
            for (Cinematica.LoadDetail detail : details) {
                    MutableComponent literal = Component.literal(detail.msg());
                if (detail.failure()) {
                    context.getSource().sendFailure(literal);
                } else context.getSource().sendSuccess(() -> literal, false);
            }
            
        } catch (IOException e) {
            context.getSource().sendFailure(Component.literal("Failed to reload: "+e.getMessage()));
            Cinematica.getLogger().error(e.getMessage(), e);
        }
        
        return Command.SINGLE_SUCCESS;
    }
    
    private static void registerCinematicaCommands() {
        CinematicaRegistries.COMMAND_REGISTRY.register("camera", new CameraPositionCommand());
        CinematicaRegistries.COMMAND_REGISTRY.register("mc", new MinecraftCommandHandler());
    }
    
    private static int getSlideshow(CommandContext<CommandSourceStack> context) {
        SlideshowSettings settings = context.getArgument("name", SlideshowSettings.class);
        
        try {
            Component component = settings.toComponentJson();
            
            context.getSource().sendSuccess(() -> component, false);
        } catch (IOException e) {
            Cinematica.getLogger().error("Failed to convert slideshow to Component", e);
            context.getSource().sendFailure(Component.literal("Failed to read slideshow."));
            return 0;
        }
        
        return Command.SINGLE_SUCCESS;
    }
    
    private static int attach(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "entities");
        DeathScreenSettings settings = context.getArgument("deathscreen", DeathScreenSettings.class);
        int success = 0;
        
        try {
            for (Entity entity : entities) {
                // Fetch the capability safely
                LazyOptional<ICinematicCap> cap = entity.getCapability(CinematicCapProvider.INSTANCE);
                if (cap.isPresent()) {
                    cap.ifPresent(c -> c.setCinematicId(settings.name()));
                    success++;
                    
                    for (ServerPlayer player : context.getSource().getLevel().getServer().getPlayerList().getPlayers()) {
                        NetworkManager.sendToPlayer(player, new SelectedScenePacket(entity.getId(), settings.name()));
                    }
                }
            }
        } catch (Exception e) {
            Cinematica.getLogger().error(e.getMessage(), e);
            throw e;
        }
        
        final int finalSuccess = success;
        context.getSource().sendSuccess(() -> Component.literal("Successfully set " + finalSuccess + " entit" + ((finalSuccess > 1)? "ies" : "y") + " to show " + settings.name()), false);
        
        return Command.SINGLE_SUCCESS;
    }
    
    private static int open(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        SlideshowSettings settings = context.getArgument("name", SlideshowSettings.class);
        
        for (ServerPlayer player : players) {
            NetworkManager.sendToPlayer(player, new OpenSlideshowPacket(settings));
        }
        
        return Command.SINGLE_SUCCESS;
    }
    
    private static int openSelf(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException(); // Built-in Brigadier check
        SlideshowSettings slideshow = context.getArgument("name", SlideshowSettings.class);
        
        NetworkManager.sendToPlayer(player, new OpenSlideshowPacket(slideshow));
        return Command.SINGLE_SUCCESS;
    }
    
    private static int getDeathScreen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "entity");
        
        // Get the ID from the Capability instead of EntityData
        String id = entity.getCapability(CinematicCapProvider.INSTANCE)
                .map(ICinematicCap::getCinematicId)
                .orElse("");
        
        if (id.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Entity has no cinematic ID attached."));
            return 0;
        }
        
        context.getSource().sendSuccess(() -> Component.literal("Entity '")
                .append(entity.getDisplayName())
                .append("' is linked to: ")
                .append(Component.literal(id).withStyle(ChatFormatting.AQUA)), false);
        return Command.SINGLE_SUCCESS;
    }
}
