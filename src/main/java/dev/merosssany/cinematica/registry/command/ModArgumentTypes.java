package dev.merosssany.cinematica.registry.command;

import dev.merosssany.cinematica.core.Cinematica;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModArgumentTypes {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = 
        DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Cinematica.MODID);

    public static final RegistryObject<SingletonArgumentInfo<SlideshowCommandType>> SLIDESHOW_TYPE =
        ARGUMENT_TYPES.register("slideshow", () ->
            ArgumentTypeInfos.registerByClass(SlideshowCommandType.class, SingletonArgumentInfo.contextFree(SlideshowCommandType::new))
        );
    
    public static void register(IEventBus bus) {
        ARGUMENT_TYPES.register(bus);
    }
}