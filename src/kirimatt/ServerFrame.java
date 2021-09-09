package kirimatt;

import components.GuiHelper;
import net.miginfocom.swing.MigLayout;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author azamat
 */
public class ServerFrame extends JFrame {
    public int port = 8888;
    public DatagramSocket datagramSocket;

    public static List<Byte> bytesList = new ArrayList<>();
    public PlayerThread playerThread;

    public static AudioFormat getAudioFormat() {
        float sampleRate = 16000.0f;
        int sampleSizeInBits = 16;
        int channel = 2;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channel, signed, bigEndian);
    }
    public SourceDataLine audioOut;

    @SuppressWarnings("all")
    public Optional<Clip> clip = Optional.empty();

    public JButton startButton;
    public JButton endButton;
    public ServerFrame() throws HeadlessException {

        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        this.setLayout(new MigLayout());
        this.setSize(new Dimension(220, 150));

        JLabel clientLbl = new JLabel(GuiHelper.setHtmlTag("SERVER"));
        GuiHelper.setComponentSize(clientLbl, new Dimension(200, 30));

        startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            initAudio();
            clip.ifPresent(Clip::stop);
        });
        GuiHelper.setComponentSize(startButton, new Dimension(100, 30));

        endButton = new JButton("End");
        endButton.addActionListener(e -> {
            ServerVoice.isCalled = false;
            startButton.setEnabled(true);
            endButton.setEnabled(false);

            byte[] arrayByte = new byte[bytesList.size()];
            int r = 0;
            for(Byte b : bytesList)
                arrayByte[r++] = b;
            try {
                writeAudioToWavFile(arrayByte, getAudioFormat(), "sound.wav");
                bytesList.clear();
            } catch (Exception exception) {
                exception.printStackTrace();
            }

        });
        GuiHelper.setComponentSize(endButton, new Dimension(100, 30));
        endButton.setEnabled(false);

        JButton clipStartButton = new JButton("Clip Start");
        clipStartButton.addActionListener(e -> {

            File file = new File("sound.wav");
            AudioInputStream audioInputStream;
            try {
                audioInputStream = AudioSystem.getAudioInputStream(file);

                clip = Optional.of(AudioSystem.getClip());
                clip.get().open(audioInputStream);
                clip.get().start();

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                ex.printStackTrace();
            }

        });
        GuiHelper.setComponentSize(clipStartButton, new Dimension(100, 30));

        JButton clipEndButton = new JButton("Clip End");
        clipEndButton.addActionListener(e -> clip.ifPresent(Clip::stop));
        GuiHelper.setComponentSize(clipEndButton, new Dimension(100, 30));

        add(clientLbl, "gapleft 80 ,right, wrap, span");
        add(startButton);
        add(endButton, "wrap");
        add(clipStartButton);
        add(clipEndButton);

    }

    public void initAudio() {
        try {
            AudioFormat format = getAudioFormat();

            DataLine.Info infoOut = new DataLine.Info(SourceDataLine.class, format);
            if(!AudioSystem.isLineSupported(infoOut)) {
                System.out.println("Not support");
                System.exit(0);
            }

            audioOut = (SourceDataLine) AudioSystem. getLine(infoOut);

            audioOut.open(format);

            audioOut.start();


            playerThread = new PlayerThread();

            playerThread.din = datagramSocket;
            playerThread.audioOut = audioOut;

            ServerVoice.isCalled = true;
            playerThread.start();

            endButton.setEnabled(true);
            startButton.setEnabled(false);


        } catch (LineUnavailableException  e) {
            e.printStackTrace();
        }
    }

    public static void writeAudioToWavFile(byte[] data, AudioFormat format, String fn) throws Exception {
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), format, data.length);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(fn));
    }
}
