package dev.merosssany.cinematica.core.audio.data;

import dev.merosssany.cinematica.core.audio.data.factory.AudioStreamFactory;
import dev.merosssany.cinematica.core.audio.data.stream.AudioStream;
import dev.merosssany.cinematica.core.audio.data.stream.Mp3Stream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AudioReader {
    private static final Map<String, AudioStreamFactory> extDecoders = new ConcurrentHashMap<>();
    private static final Map<String, AudioStreamFactory> hexDecoders = new ConcurrentHashMap<>();
    
    public static AudioStream getStreamFromFile(File file) throws Exception {
        if (file.exists() && file.isFile()) {
            String name = file.getName();
            String[] dots = name.split("\\.");
            String ext = dots[dots.length-1];
            
            return extDecoders.get(ext).create(file.getAbsolutePath());
        }
        return null;
    }
    
    public static AudioStream getAudioStream(InputStream is) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(is);
        bis.mark(16); // Mark the start so we can go back
        
        byte[] header = new byte[4];
        bis.read(header);
        bis.reset(); // Rewind the stream to the beginning
        
        String hex = bytesToHex(header);
        
        for (var entry : hexDecoders.entrySet()) {
            if (hex.startsWith(entry.getKey())) return entry.getValue().create(bis);
        }
        
        // Fallback or Error
        return new Mp3Stream(bis);
    }
    
    public static String bytesToHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes).toUpperCase();
    }
    
    public static void registerFactory(String ext, String header, AudioStreamFactory factory) {
        extDecoders.put(ext, factory);
        hexDecoders.put(header, factory);
    }
    
    public static Set<String> getSupportedFiles() {
        return extDecoders.keySet();
    }
}
