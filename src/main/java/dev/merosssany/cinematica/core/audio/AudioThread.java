package dev.merosssany.cinematica.core.audio;

import com.mojang.logging.LogUtils;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioThread extends Thread {
    private static final Logger logger = LogUtils.getLogger();
    
    private final AudioPlayer player;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean end = new AtomicBoolean();
    private final AtomicBoolean exitWhenDone = new AtomicBoolean();
    private final Runnable endCallback;
    
    private double lastTime;
    
    public AudioThread(AudioPlayer player, Runnable endCallback) {
        super("Music-Audio-Thread");
        this.player = player;
        this.endCallback = endCallback;
        
        setDaemon(true);
        lastTime = GLFW.glfwGetTime();
    }
    
    @Override
    public void run() {
        try {
            while (running.get()) {
                double current = GLFW.glfwGetTime();
                double delta = current - lastTime;
                lastTime = current;
                
                Runnable task;
                while((task = tasks.poll()) != null) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        logger.error("Error has occurred in the dispatched task.", e);
                    }
                }
                
                // Process audio state operations sequentially on the same thread context
                if (player.isStreaming()) {
                    player.updateStreaming();
                    if (player.isFading()) {
                        player.updateFadeProgress((float) delta);
                    }
                }
                
                // Evaluate end conditions smoothly without multithreading data anomalies
                if (!player.isStreaming() && !player.isLooping() && !player.isFading() && !end.get()) {
                    end.set(true);
                    if (endCallback != null) {
                        endCallback.run();
                    }
                    if (exitWhenDone.get()) break;
                }
                
                try {
                    // Waking up frequently is safe since updateStreaming chunks are small (1024 samples)
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error has occurred in the AudioThread", e);
        } finally {
            player.cleanup();
        }
    }
    
    public void startStream(File file) {
        tasks.add(() -> {
            try {
                player.startStream(file);
            } catch (Exception e) {
                logger.error("Failed to start stream", e);
            }
        });
    }
    
    public void startStream(InputStream inputStream) {
        tasks.add(() -> {
            try {
                player.startStream(inputStream);
            } catch (Exception e) {
                logger.error("Failed to start stream", e);
            }
        });
    }
    
    public void shutdown() {
        running.set(false);
        this.interrupt();
    }
    
    public void addTask(Runnable runnable) {
        if (!running.get() && !exitWhenDone.get()) return;
        tasks.add(runnable);
    }
    
    public AudioPlayer getPlayer() { return player; }
    public void requestExitAfterPlayback() { exitWhenDone.set(true); }
}