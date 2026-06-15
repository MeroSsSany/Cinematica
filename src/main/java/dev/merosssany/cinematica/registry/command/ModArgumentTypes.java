package dev.merosssany.cinematica.registry.command;

import dev.merosssany.cinematica.core.Cinematica;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

public class ModArgumentTypes {
    // The base register handles command argument tracking pools
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
            DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Cinematica.MODID);
    
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<SlideshowCommandType>> SLIDESHOW_TYPE =
            ARGUMENT_TYPES.register("slideshow", () ->
                    SingletonArgumentInfo.contextFree(SlideshowCommandType::new)
            );
    
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<DeathScreenCommandType>> DEATH_SCREEN_TYPE =
            ARGUMENT_TYPES.register("cine_death_screen", () ->
                    SingletonArgumentInfo.contextFree(DeathScreenCommandType::new)
            );
    
    public static void register(IEventBus bus) {
        ARGUMENT_TYPES.register(bus);
        // Register the class mapping when the registry event fires
        bus.addListener(ModArgumentTypes::registerTypeInfo);
    }
    
    private static void registerTypeInfo(RegisterEvent event) {
        event.register(Registries.COMMAND_ARGUMENT_TYPE, helper -> {
            ArgumentTypeInfos.registerByClass(SlideshowCommandType.class, SLIDESHOW_TYPE.get());
            ArgumentTypeInfos.registerByClass(DeathScreenCommandType.class, DEATH_SCREEN_TYPE.get());
        });
    }
}