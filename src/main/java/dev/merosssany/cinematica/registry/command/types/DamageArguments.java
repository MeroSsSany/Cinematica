package dev.merosssany.cinematica.registry.command.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

import java.util.concurrent.CompletableFuture;

public class DamageArguments implements ArgumentType<ResourceKey<DamageType>> {
    private final HolderLookup.Provider registries;

    private DamageArguments(HolderLookup.Provider registry) {
        this.registries = registry;
    }
    
    public static DamageArguments damageType(CommandBuildContext context) {
        return new DamageArguments(context);
    }

    public static ResourceKey<DamageType> getDamageType(CommandContext<CommandSourceStack> context, String argument) throws Exception {
        ResourceKey<?> key = context.getArgument(argument, ResourceKey.class);

        if (key.isFor(Registries.DAMAGE_TYPE)) {
            return (ResourceKey<DamageType>) key;
        } else {
            throw new Exception("The value provided in argument 'type' is not a damage type.");
        }
    }

    @Override
    public ResourceKey<DamageType> parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation loc = ResourceLocation.read(reader);
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, loc);

        var registry = registries.lookupOrThrow(Registries.DAMAGE_TYPE);
        var lookup = registry.get(key);
        if (lookup.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var registry = registries.lookupOrThrow(Registries.DAMAGE_TYPE);

        for (ResourceKey<DamageType> key : registry.listElementIds().toList()) {
            builder.suggest(key.location().toString());
        }
        return builder.buildFuture();
    }
}
