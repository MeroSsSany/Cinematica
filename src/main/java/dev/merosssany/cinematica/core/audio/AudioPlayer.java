package dev.merosssany.cinematica.core.audio;

import com.mojang.logging.LogUtils;
import dev.merosssany.cinematica.core.audio.data.AudioReader;
import dev.merosssany.cinematica.core.audio.data.stream.AudioStream;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import javax.sound.sampled.*;
import java.io.File;
import java.io.InputStream;
import java.nio.ShortBuffer;

public class AudioPlayer {
    private static final Logger logger = LogUtils.getLogger();
    
    private SourceDataLine line;
    private AudioStream currentStream;
    private File nextFile;
    private boolean shouldLoop;
    
    private boolean streaming = false;
    private boolean fading = false;
    
    private float fadeTimeElapsed;
    private float fadeDurationSeconds;
    private float initialFadeGain;
    private float currentVolume = 1.0f;
    
    private static int samplesToRead = 4096;
    private byte[] byteBuffer;
    private ShortBuffer shortBuf;
    
    public void startStream(File path) throws Exception {
        stop();
        currentStream = AudioReader.getStreamFromFile(path);
        if (currentStream == null) return;
        
        // Just prepare the buffers, don't open the line yet!
        int channels = currentStream.getChannels();
        shortBuf = MemoryUtil.memAllocShort(samplesToRead * channels);
        byteBuffer = new byte[samplesToRead * channels * 2];
        
        streaming = true;
        logger.info("Stream prepared for: \"{}\"", path.getName());
    }
    
    public void startStream(InputStream inputStream) throws Exception {
        stop();
        currentStream = AudioReader.getAudioStream(inputStream);
        if (currentStream == null) return;
        
        // Just prepare the buffers, don't open the line yet!
        int channels = currentStream.getChannels();
        shortBuf = MemoryUtil.memAllocShort(samplesToRead * channels);
        byteBuffer = new byte[samplesToRead * channels * 2];
        
        streaming = true;
        logger.info("Stream prepared from an existing stream");
    }
    
    public boolean isFading() {
        return fading;
    }
    
    public void updateStreaming() {
        if (!streaming || currentStream == null) return;
        
        shortBuf.clear();
        int samplesPerChannel = currentStream.readSamples(shortBuf);
        
        if (samplesPerChannel > 0) {
            int actualRate = currentStream.getSampleRate();
            int actualChannels = currentStream.getChannels();
            
            if (line == null || line.getFormat().getSampleRate() != (float)actualRate) {
                if (actualRate > 1) {
                    reopenLine(actualRate, actualChannels);
                }
            }
            
            if (line != null) {
                int totalSamples = samplesPerChannel * actualChannels;
                int bytesToWrite = totalSamples * 2;
                
                if (byteBuffer.length != bytesToWrite) {
                    byteBuffer = new byte[bytesToWrite];
                }
                
                // Convert Shorts to Bytes (Little Endian)
                for (int i = 0; i < totalSamples; i++) {
                    short sample = shortBuf.get(i);
                    byteBuffer[i * 2] = (byte) (sample & 0xFF);
                    byteBuffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
                }
                
                // The thread will wait here until the audio hardware is ready.
                line.write(byteBuffer, 0, bytesToWrite);
            }
        }
        
        if (currentStream.isFinished()) {
            if (nextFile != null) {
                try {
                    AudioStream nextStream = AudioReader.getStreamFromFile(nextFile);
                    currentStream.close();
                    currentStream = nextStream;
                    
                    if (!shouldLoop) nextFile = null;
                    
                    logger.info("Transitioned to next audio segment.");
                } catch (Exception e) {
                    logger.error("Failed seamless transition", e);
                    stop();
                }
            } else if (shouldLoop) {
                currentStream.reset();
            } else {
                stop();
            }
        }
    }
    
    private void reopenLine(int rate, int channels) {
        try {
            if (line != null) { line.stop(); line.close(); }
            
            AudioFormat format = new AudioFormat((float)rate, 16, channels, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            setVolume(currentVolume);
            logger.info("Audio line established at {}Hz ({} channels)", rate, channels);
        } catch (Exception e) {
            logger.error("Failed to open Java Sound line", e);
        }
    }
    
    public void setVolume(float volume) {
        this.currentVolume = volume;
        if (line == null || !line.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        
        FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (Math.log(Math.max(volume, 0.0001f)) / Math.log(10.0) * 20.0);
        gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
    }
    
    public void startFadeOut(float durationSeconds) {
        if (!streaming || fading) return;
        fading = true;
        fadeDurationSeconds = durationSeconds;
        fadeTimeElapsed = 0;
        initialFadeGain = currentVolume;
        logger.debug("Starting Java Sound fade out: {}s", durationSeconds);
    }
    
    public void updateFadeProgress(float deltaTime) {
        if (!fading || !streaming) return;
        
        fadeTimeElapsed += deltaTime;
        if (fadeTimeElapsed >= fadeDurationSeconds) {
            setVolume(0.0f);
            finishAndStop();
        } else {
            float progress = fadeTimeElapsed / fadeDurationSeconds;
            setVolume(initialFadeGain * (1.0f - progress));
        }
    }
    
    private void finishAndStop() {
        fading = false;
        if (line != null) {
            line.drain();
        }
        stop();
    }
    
    public synchronized void stop() {
        streaming = false;
        fading = false;
        
        SourceDataLine localLine = this.line;
        
        if (localLine != null) {
            try {
                localLine.stop();
                localLine.flush();
                localLine.close();
            } catch (Exception ignored) {
            } finally {
                if (this.line == localLine) {
                    this.line = null;
                }
            }
        }
        
        if (currentStream != null) {
            try { currentStream.close(); } catch (Exception ignored) {}
            currentStream = null;
        }
        
        if (shortBuf != null) {
            MemoryUtil.memFree(shortBuf);
            shortBuf = null;
        }
    }
    
    public void queueNext(File file, boolean loop) {
        this.nextFile = file;
        this.shouldLoop = loop;
    }
    
    public boolean isLooping() { return shouldLoop; }
    public void shouldLoop(boolean shouldLoop) { this.shouldLoop = shouldLoop; }
    public void cleanup() { stop(); }
    public void setSamples(int samples) {
        samplesToRead = samples;
    }
    public int getSamples() { return samplesToRead; }
    public boolean isStreaming() { return streaming; }
}