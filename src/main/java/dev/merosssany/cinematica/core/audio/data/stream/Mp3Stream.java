package dev.merosssany.cinematica.core.audio.data.stream;

import javazoom.jl.decoder.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ShortBuffer;

public class Mp3Stream implements AudioStream, AutoCloseable {
    private int channels;
    private Bitstream bitstream;
    private final Decoder decoder;
    private boolean finished = false;
    private int sampleRate = 44100;
    private String path;
    private InputStream inputStream;
    
    public Mp3Stream(String path) throws IOException {
        this.path = path;
        decoder = new Decoder();
        bitstream = new Bitstream(new FileInputStream(path));
        
        initStream(bitstream);
    }
    
    public Mp3Stream(InputStream inputStream) throws IOException {
        decoder = new Decoder();
        this.inputStream = inputStream;
        bitstream = new Bitstream(inputStream);
        
        initStream(bitstream);
    }
    
    public int getChannels() {
        return channels; // JLayer decodes to stereo
    }
    
    public int getSampleRate() {
        return sampleRate;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    public int readSamples(ShortBuffer buffer) {
        if (finished) return 0;
        
        try {
            Header frameHeader = bitstream.readFrame();
            if (frameHeader == null) {
                finished = true;
                return 0;
            }
            
            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);
            this.sampleRate = output.getSampleFrequency(); // Get the REAL rate from the file
            
            short[] pcm = output.getBuffer();
            buffer.clear();
            buffer.put(pcm).flip();
            bitstream.closeFrame();
            return output.getBufferLength() / getChannels(); // number of samples per channel
        } catch (BitstreamException | DecoderException e) {
            finished = true;
            return 0;
        }
    }
    
    @Override
    public void close() throws BitstreamException {
        bitstream.close();
    }
    
    private void initStream(Bitstream bitstream) throws IOException {
        // Peek at the first frame to get the correct metadata immediately
        try {
            Header firstFrame = bitstream.readFrame();
            if (firstFrame != null) {
                this.sampleRate = firstFrame.sample_frequency();

                bitstream.unreadFrame(); // Some versions of JLayer support this,
                this.channels = (firstFrame.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
            }
        } catch (BitstreamException e) {
            this.sampleRate = 44100; // Fallback
        }
    }
    
    @Override
    public void reset() {
        try {
            this.bitstream.close();
            this.initStream();
            
        } catch (Exception e) {
            this.finished = true;
        }
    }
    
    private void initStream() throws IOException {
        if (path == null || path.isEmpty()) bitstream = new Bitstream(inputStream);
        else bitstream = new Bitstream(new FileInputStream(path));
        initStream(bitstream);
    }
}

