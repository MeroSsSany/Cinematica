package dev.merosssany.cinematica.core.audio.data.stream;

import java.nio.ShortBuffer;

public interface AudioStream {
    int getChannels();
    int getSampleRate();
    int readSamples(ShortBuffer targetBuffer);
    boolean isFinished();
    void close() throws Exception;
    void reset();
}
