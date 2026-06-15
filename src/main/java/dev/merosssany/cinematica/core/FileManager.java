package dev.merosssany.cinematica.core;

import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

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
    
    public static Path getCinematicaFolder() throws IOException {
        Path musicPath = geConfigDir().resolve("cinematica");
        if (Files.notExists(musicPath)) {
            Files.createDirectories(musicPath);
        }
        return musicPath;
    }
    
    public static File[] getAllFilesFrom(Path path, FilenameFilter consumer) throws IOException {
        File folder = path.toFile();
        if (!folder.isDirectory()) throw new IOException("Not a directory: " + path);
        
        return folder.listFiles(consumer);
    }
    public static File getRandomFileFrom(Path path, Collection<String> filter) throws IOException {
        File[] files = getAllFilesFrom(path, (dir, name) -> {
            int lastDot = name.lastIndexOf('.');
            if (lastDot == -1) return false; // No extension
            String ext = name.substring(lastDot + 1).toLowerCase();
            return filter.contains(ext);
        });
        
        if (files == null || files.length == 0) {
            throw new IOException("No supported music files found at: " + path.toAbsolutePath());
        }
        
        int index = (int) (Math.random() * files.length);
        return files[index];
    }
}
