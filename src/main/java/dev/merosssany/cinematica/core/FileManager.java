package dev.merosssany.cinematica.core;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.merosssany.cinematica.core.Cinematica.getLogger;

public class FileManager {
	public static Path geConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}
	
	public static void createFolder(String folderName, Path path) throws IOException {
		Files.createDirectories(path.resolve(folderName));
	}
	
	public static void init() throws IOException {
		Path game = geConfigDir();
		Path musicFolder = Path.of(game.toString(), "cinematica");
		
		if (!musicFolder.toFile().exists()) createFolder("cinematica",game);
	}
    
    public static Path getCinematicaFolder() {
        try {
            Path cinematica = geConfigDir().resolve("cinematica");
            if (Files.notExists(cinematica)) {
                Files.createDirectories(cinematica);
            }
            return cinematica;
        } catch (IOException e) {
            getLogger().error("A fatal error has occurred while retrieving config folder.",e);
            throw new RuntimeException(e);
        }
    }
}
