package dev.merosssany.cinematica.core.audio.data.stream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class WavStream implements AudioStream, AutoCloseable {
    private final byte[] scratchBuffer;
    private AudioInputStream stream;
    private final int channels;
    private final int sampleRate;
    
    private InputStream inputStream;
    private File file;
    private boolean finished = false;
    
    public WavStream(String path) throws Exception {
        this.file = new File(path);
        this.stream = AudioSystem.getAudioInputStream(file);
        
        AudioFormat format = stream.getFormat();
        this.channels = format.getChannels();
        this.sampleRate = (int) format.getSampleRate();
        
        // Safety check: only support 16-bit for now to match AudioPlayer
        if (format.getSampleSizeInBits() != 16) {
            throw new UnsupportedAudioFileException("Only 16-bit WAV files are supported.");
        }
        
        this.scratchBuffer = new byte[4096 * 2];
    }
    
    public WavStream(InputStream inputStream) throws Exception {
        this.inputStream = inputStream;
        this.stream = AudioSystem.getAudioInputStream(inputStream);
        
        AudioFormat format = stream.getFormat();
        this.channels = format.getChannels();
        this.sampleRate = (int) format.getSampleRate();
        
        // Safety check: only support 16-bit for now to match AudioPlayer
        if (format.getSampleSizeInBits() != 16) {
            throw new UnsupportedAudioFileException("Only 16-bit WAV files are supported.");
        }
        
        this.scratchBuffer = new byte[4096 * 2];
    }
    
    @Override
    public int readSamples(ShortBuffer buffer) {
        if (finished) return 0;
        try {
            int bytesToRead = Math.min(scratchBuffer.length, buffer.remaining() * 2);
            int bytesRead = stream.read(scratchBuffer, 0, bytesToRead);
            
            if (bytesRead <= 0) {
                finished = true;
                return 0;
            }
            
            buffer.clear();
            buffer.put(ByteBuffer.wrap(scratchBuffer, 0, bytesRead)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer());
            buffer.flip();
            
            return (bytesRead / 2) / channels;
        } catch (Exception e) {
            finished = true;
            return 0;
        }
    }
    
    @Override public int getChannels() { return channels; }
    @Override public int getSampleRate() { return sampleRate; }
    @Override public boolean isFinished() { return finished; }
    
    @Override
    public void close() {
        try { stream.close(); } catch (Exception ignored) {}
    }
    
    @Override
    public void reset() {
        try {
            stream.close();
            if (file == null) stream = AudioSystem.getAudioInputStream(inputStream);
            else stream = AudioSystem.getAudioInputStream(file);
            
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}
