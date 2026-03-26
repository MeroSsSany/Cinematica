package dev.merosssany.cinematica.core.audio.data.factory;

import dev.merosssany.cinematica.core.audio.data.AudioReader;
import dev.merosssany.cinematica.core.audio.data.stream.AudioStream;
import dev.merosssany.cinematica.core.audio.data.stream.Mp3Stream;
import dev.merosssany.cinematica.core.audio.data.stream.OggStream;
import dev.merosssany.cinematica.core.audio.data.stream.WavStream;

import java.io.IOException;
import java.io.InputStream;

public class DefaultFactories {
    public static void register() {
        AudioReader.registerFactory("mp3", "494433", new Mp3StreamFactory());
        AudioReader.registerFactory("ogg", "4F676753", new OggStreamFactory());
        AudioReader.registerFactory("oga", "4F676753", new OggStreamFactory());
        AudioReader.registerFactory("wav", "52494646", new WavStreamFactory());
    }
    
    public static class WavStreamFactory implements AudioStreamFactory {
        
        @Override
        public AudioStream create(String file) throws Exception {
            return new WavStream(file);
        }
        
        @Override
        public AudioStream create(InputStream inputStream) throws Exception {
            return new WavStream(inputStream);
        }
    }
    
    public static class OggStreamFactory implements AudioStreamFactory {
        
        @Override
        public AudioStream create(String file) throws Exception {
            return new OggStream(file);
        }
        
        @Override
        public AudioStream create(InputStream inputStream) throws Exception {
            return new OggStream(inputStream);
        }
    }
    
    public static class Mp3StreamFactory implements AudioStreamFactory {
        
        @Override
        public AudioStream create(String file) throws Exception {
            return new Mp3Stream(file);
        }
        
        @Override
        public AudioStream create(InputStream inputStream) throws IOException {
            return new Mp3Stream(inputStream);
        }
    }
}
