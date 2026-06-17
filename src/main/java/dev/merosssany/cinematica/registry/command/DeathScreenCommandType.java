package dev.merosssany.cinematica.registry.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.merosssany.cinematica.core.data.death.DeathScreenSettings;
import dev.merosssany.cinematica.core.data.loader.CinematicaProjectLoader;
import dev.merosssany.cinematica.core.data.loader.assets.DeathScreenLoader;

import java.util.concurrent.CompletableFuture;

public class DeathScreenCommandType implements ArgumentType<DeathScreenSettings> {
    @Override
    public DeathScreenSettings parse(StringReader stringReader) throws CommandSyntaxException {
        String name = stringReader.readQuotedString();
        DeathScreenSettings slideshow = CinematicaProjectLoader.get(name, DeathScreenLoader.class);
        
        if (slideshow != null) return slideshow;
        else throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(stringReader);
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (String slideshow : CinematicaProjectLoader.getLoader(DeathScreenLoader.class).getRegistered()) {
            builder.suggest("\""+slideshow+"\"");
        }
        return builder.buildFuture();
    }
}
