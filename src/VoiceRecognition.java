import javax.sound.sampled.*;
import java.util.function.Consumer;

public class VoiceRecognition implements Runnable {
    private static final int AMPLITUDE_THRESHOLD = 2000;
    private static final int DOT_DURATION_MS = 150;
    private static final int DASH_DURATION_MS = 450;
    private static final int LETTER_GAP_MS = 450;
    private static final int WORD_GAP_MS = 1050;
    private static final AudioFormat FORMAT = new AudioFormat(8000.0f, 16, 1, true, false);

    private volatile boolean running = true;
    private final Consumer<String> morseCallback;

    public VoiceRecognition(Consumer<String> morseCallback) {
        this.morseCallback = morseCallback;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            TargetDataLine microphone = AudioSystem.getTargetDataLine(FORMAT);
            microphone.open(FORMAT);
            microphone.start();

            byte[] buffer = new byte[1024];
            boolean soundOn = false;
            long soundStartTime = 0;
            long silenceStartTime = System.currentTimeMillis();

            StringBuilder morseCode = new StringBuilder();

            while (running) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                boolean currentSoundDetected = isSoundPresent(buffer, bytesRead);

                long currentTime = System.currentTimeMillis();

                if (currentSoundDetected && !soundOn) {
                    soundOn = true;
                    soundStartTime = currentTime;

                    long silenceDuration = currentTime - silenceStartTime;
                    if (silenceDuration > WORD_GAP_MS) {
                        morseCode.append(" / ");
                        publish(morseCode.toString());
                    } else if (silenceDuration > LETTER_GAP_MS) {
                        morseCode.append(" ");
                        publish(morseCode.toString());
                    }
                } else if (!currentSoundDetected && soundOn) {
                    soundOn = false;
                    long soundDuration = currentTime - soundStartTime;

                    if (soundDuration < DOT_DURATION_MS) {
                        morseCode.append(".");
                        publish(morseCode.toString());
                    } else if (soundDuration < DASH_DURATION_MS) {
                        morseCode.append("-");
                        publish(morseCode.toString());
                    } else {
                        morseCode.append("-");
                        publish(morseCode.toString());
                    }
                    silenceStartTime = currentTime;
                }

                Thread.sleep(10);
            }

            microphone.stop();
            microphone.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publish(String morse) {
        morseCallback.accept(morse);
    }

    private boolean isSoundPresent(byte[] audioBuffer, int bytesRead) {
        int maxAmplitude = 0;

        for (int i = 0; i < bytesRead - 1; i += 2) {
            int low = audioBuffer[i] & 0xff;
            int high = audioBuffer[i + 1];
            int sample = (high << 8) | low;

            if (sample < 0) sample = -sample;
            if (sample > maxAmplitude) maxAmplitude = sample;
        }
        return maxAmplitude > AMPLITUDE_THRESHOLD;
    }
}