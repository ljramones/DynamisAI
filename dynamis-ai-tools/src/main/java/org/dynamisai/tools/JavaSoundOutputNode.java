package org.dynamisai.tools;

import io.dynamis.audio.api.AcousticConstants;
import io.dynamis.audio.dsp.device.AudioDevice;
import io.dynamis.audio.dsp.device.AudioDeviceException;
import org.dynamis.core.logging.DynamisLogger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AudioDevice sink backed by javax.sound.sampled.
 *
 * The DSP thread calls write() synchronously during renderBlock(); write()
 * therefore only enqueues into a ring buffer and returns immediately.
 * A dedicated I/O thread drains the ring and writes to SourceDataLine.
 */
public final class JavaSoundOutputNode implements AudioDevice {

    private static final DynamisLogger log = DynamisLogger.get(JavaSoundOutputNode.class);

    private static final int RING_BLOCKS = 10;

    private int sampleRate;
    private int channels;
    private int blockSize;

    private float[] ringBuffer;
    private int ringCapacity;
    private int writePos;
    private int readPos;
    private final Object ringLock = new Object();

    private byte[] byteBuffer;
    private SourceDataLine line;
    private Thread ioThread;

    private final AtomicBoolean open = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void open(int sampleRate, int channels, int blockSize)
        throws AudioDeviceException {
        if (open.getAndSet(true)) {
            throw new IllegalStateException("JavaSoundOutputNode already open");
        }

        this.sampleRate = sampleRate;
        this.channels = channels;
        this.blockSize = blockSize;

        int floatsPerBlock = blockSize * channels;
        ringCapacity = RING_BLOCKS * floatsPerBlock;
        ringBuffer = new float[ringCapacity];
        byteBuffer = new byte[floatsPerBlock * Short.BYTES];
        writePos = 0;
        readPos = 0;

        AudioFormat format = new AudioFormat(sampleRate, 16, channels, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            open.set(false);
            throw new AudioDeviceException(
                "No " + sampleRate + "Hz/16-bit/" + channels + "ch output line available");
        }

        try {
            int lineBufferBytes = floatsPerBlock * Short.BYTES * 4;
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, lineBufferBytes);
            line.start();
        } catch (LineUnavailableException e) {
            open.set(false);
            throw new AudioDeviceException("Failed to open SourceDataLine", e);
        }

        running.set(true);
        ioThread = Thread.ofVirtual().name("dynamisai-audio-out").start(this::ioLoop);

        long ringMs = (ringCapacity / channels) * 1000L / sampleRate;
        log.info(String.format("JavaSoundOutputNode open: %sHz %sch block=%s ring=%sms", sampleRate, channels, blockSize, ringMs));
    }

    @Override
    public void write(float[] buffer, int frameCount, int channels) {
        if (!running.get()) {
            return;
        }

        int floats = frameCount * channels;
        synchronized (ringLock) {
            int available = ringCapacity - used();
            if (available < floats) {
                log.warn(String.format("JavaSoundOutputNode: ring full, dropping %s frames", frameCount));
                return;
            }
            for (int i = 0; i < floats; i++) {
                ringBuffer[writePos] = buffer[i];
                writePos = (writePos + 1) % ringCapacity;
            }
            ringLock.notifyAll();
        }
    }

    @Override
    public void close() {
        if (!open.getAndSet(false)) {
            return;
        }

        running.set(false);
        synchronized (ringLock) {
            ringLock.notifyAll();
        }

        if (ioThread != null) {
            try {
                ioThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (line != null) {
            line.drain();
            line.close();
        }
        log.info("JavaSoundOutputNode closed");
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    @Override
    public String deviceDescription() {
        return "JavaSoundOutputNode";
    }

    @Override
    public int actualSampleRate() {
        return sampleRate > 0 ? sampleRate : AcousticConstants.SAMPLE_RATE;
    }

    @Override
    public float outputLatencyMs() {
        if (sampleRate <= 0 || channels <= 0) {
            return 0f;
        }
        return (ringCapacity / (float) channels) * 1000f / sampleRate;
    }

    private void ioLoop() {
        int floatsPerBlock = blockSize * channels;
        float[] block = new float[floatsPerBlock];

        while (running.get() || usedUnlocked() >= floatsPerBlock) {
            synchronized (ringLock) {
                while (used() < floatsPerBlock && running.get()) {
                    try {
                        ringLock.wait(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                if (used() < floatsPerBlock) {
                    break;
                }
                for (int i = 0; i < floatsPerBlock; i++) {
                    block[i] = ringBuffer[readPos];
                    readPos = (readPos + 1) % ringCapacity;
                }
            }
            floatToInt16Le(block, byteBuffer, floatsPerBlock);
            line.write(byteBuffer, 0, floatsPerBlock * Short.BYTES);
        }
    }

    private int used() {
        int u = writePos - readPos;
        return u < 0 ? u + ringCapacity : u;
    }

    private int usedUnlocked() {
        int u = writePos - readPos;
        return u < 0 ? u + ringCapacity : u;
    }

    private static void floatToInt16Le(float[] src, byte[] dst, int count) {
        for (int i = 0; i < count; i++) {
            short s = (short) Math.max(Short.MIN_VALUE,
                Math.min(Short.MAX_VALUE, (int) (src[i] * Short.MAX_VALUE)));
            dst[i * 2] = (byte) (s & 0xFF);
            dst[i * 2 + 1] = (byte) ((s >>> 8) & 0xFF);
        }
    }
}
