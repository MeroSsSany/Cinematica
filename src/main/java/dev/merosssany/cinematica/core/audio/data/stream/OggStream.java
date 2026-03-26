package dev.merosssany.cinematica.core.audio.data.stream;

import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class OggStream implements AutoCloseable, AudioStream {
    private final long decoder;
    private final int channels;
    private final int sampleRate;
    private boolean finished = false;
    
    public OggStream(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) throw new IOException("OGG file not found: " + path);
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer error = stack.mallocInt(1);
            
            decoder = STBVorbis.stb_vorbis_open_filename(
                    file.getAbsolutePath(),
                    error,
                    null
            );
            
            if (decoder == MemoryUtil.NULL) {
                throw new IllegalAccessError("Failed to open OGG. Error: " + error.get(0));
            }
            
            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            STBVorbis.stb_vorbis_get_info(decoder, info);
            
            channels = info.channels();
            sampleRate = info.sample_rate();
        }
    }
    
    public OggStream(InputStream inputStream) throws IOException {
        // 1. Read all bytes from the stream into a byte array
        byte[] bytes = inputStream.readAllBytes();
        
        // 2. Allocate a DIRECT ByteBuffer (STB needs memory it can see outside the JVM)
        java.nio.ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer error = stack.mallocInt(1);
            
            // 3. Open from memory instead of filename
            this.decoder = STBVorbis.stb_vorbis_open_memory(buffer, error, null);
            
            if (decoder == MemoryUtil.NULL) {
                MemoryUtil.memFree(buffer); // Clean up immediately on failure
                throw new IOException("Failed to open OGG from stream. Error: " + error.get(0));
            }
            
            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            STBVorbis.stb_vorbis_get_info(decoder, info);
            this.channels = info.channels();
            this.sampleRate = info.sample_rate();
        }
    }
    
    public int getChannels() {
        return channels;
    }
    
    public int getSampleRate() {
        return sampleRate;
    }
    
    /**
     * Reads PCM samples into provided buffer.
     * Returns number of samples read.
     */
    public int readSamples(ShortBuffer targetBuffer) {
        if (finished) return 0;
        
        int samples = STBVorbis.stb_vorbis_get_samples_short_interleaved(
                decoder,
                channels,
                targetBuffer
        );
        
        if (samples == 0) {
            finished = true;
        }
        
        return samples;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    @Override
    public void close() {
        STBVorbis.stb_vorbis_close(decoder);
    }
    
    @Override
    public void reset() {
    
    }
}

