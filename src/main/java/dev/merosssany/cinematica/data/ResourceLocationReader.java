package dev.merosssany.cinematica.data;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class ResourceLocationReader {
    public static InputStream resourceLocationToStream(ResourceLocation location) throws IOException {
        var manager = Minecraft.getInstance().getResourceManager();
        
        // 2. Try to find the resource
        Optional<Resource> resource = manager.getResource(location);
        
        // 3. Open the stream if it exists
        if (resource.isPresent()) {
            return resource.get().open();
        } else {
            throw new FileNotFoundException("Could not find resource: " + location);
        }
    }
}
