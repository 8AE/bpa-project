
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;

public class WaveEngine implements LineListener, Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    // Sound name -> Sound clip
    private HashMap<String, Clip> clipMap;

    private int maxClips;
    private int counter = 0;
    Clip clip;

    public WaveEngine() {
        this(256);
    }

    public WaveEngine(int maxClips) {
        this.maxClips = maxClips;
        clipMap = new HashMap<String, Clip>(maxClips);
    }

    public void load(String name, String filename) {
        if (counter == maxClips) {
            System.out.println("ERROR: cannot load a sound clip any more.");
            return;
        }

        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(
                    getClass().getResource(filename));

            AudioFormat format = stream.getFormat();
            // transform ulaw/alaw format into pcm format
            if ((format.getEncoding() == AudioFormat.Encoding.ULAW)
                    || (format.getEncoding() == AudioFormat.Encoding.ALAW)) {
                AudioFormat newFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        format.getSampleRate(),
                        format.getSampleSizeInBits() * 2, format.getChannels(),
                        format.getFrameSize() * 2, format.getFrameRate(), true);
                stream = AudioSystem.getAudioInputStream(newFormat, stream);
                format = newFormat;
            }

            DataLine.Info info = new DataLine.Info(Clip.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("ERROR: not supported format");
                System.exit(0);
            }

            clip = (Clip) AudioSystem.getLine(info);
            clip.addLineListener(this);
            clip.open(stream);
            clipMap.put(name, clip);
            stream.close();
        } catch (UnsupportedAudioFileException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);

            try {
                CrashReport cr = new CrashReport(e);
                cr.show();
            } catch (Exception n) {
                // do nothing
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);

            try {
                CrashReport cr = new CrashReport(e);
                cr.show();
            } catch (Exception n) {
                // do nothing
            }
        } catch (LineUnavailableException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);

            try {
                CrashReport cr = new CrashReport(e);
                cr.show();
            } catch (Exception n) {
                // do nothing
            }
        }
    }

    public void play(String name) {
        clip.stop();
        Clip clip = (Clip) clipMap.get(name);
        if (clip != null) {
            clip.start();
        }
    }

    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP) {
            Clip clip = (Clip) event.getSource();
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    public void isLoop(boolean isLoop) {
        if (isLoop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }

    }

    public void stop() {
        clip.stop();
    }

}
