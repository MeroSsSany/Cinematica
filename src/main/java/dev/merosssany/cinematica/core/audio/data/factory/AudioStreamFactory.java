package dev.merosssany.cinematica.core.audio.data.factory;

import dev.merosssany.cinematica.core.audio.data.stream.AudioStream;

import java.io.InputStream;

public interface AudioStreamFactory {
    AudioStream create(String file) throws Exception;
    AudioStream create(InputStream inputStream) throws Exception;
}
