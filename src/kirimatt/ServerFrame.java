package kirimatt;

import components.GuiHelper;
import net.miginfocom.swing.MigLayout;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author azamat
 */
public class ServerFrame extends JFrame {
    public int port = 8888;
    public static List<Byte> bytesList = new ArrayList<>();

    public static AudioFormat getAudioFormat() {
        float samplerate = 8000.0f;
        int sampleSizeInbits = 16;
        int channel = 2;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(samplerate, sampleSizeInbits, channel, signed, bigEndian);
    }
    public SourceDataLine audioOut;
    Clip clip;

    public JButton startButton;
    public JButton endButton;
    public ServerFrame() throws HeadlessException {
        this.setLayout(new MigLayout());
        this.setSize(new Dimension(220, 120));

        JLabel clientLbl = new JLabel(GuiHelper.setHtmlTag("SERVER"));
        GuiHelper.setComponentSize(clientLbl, new Dimension(200, 30));

        startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            initAudio();
            if(clip != null)
                clip.stop();
        });
        GuiHelper.setComponentSize(startButton, new Dimension(100, 30));

        endButton = new JButton("End");
        endButton.addActionListener(e -> {
            ServerVoice.isCalled = false;
            startButton.setEnabled(true);
            endButton.setEnabled(false);

            //TODO Разобраться с преобразованием списка в массив.
            Byte[] array = new Byte[bytesList.size()];
            array = bytesList.toArray(array);
            byte[] arraybyte = new byte[array.length];
            int r = 0;
            for(Byte b : array)
                arraybyte[r++] = b;
            try {
                writeAudioToWavFile( arraybyte, getAudioFormat(), "sound.wav");
            } catch (Exception exception) {
                exception.printStackTrace();
            }


        });
        GuiHelper.setComponentSize(endButton, new Dimension(100, 30));

        add(clientLbl, "gapleft 80 ,right, wrap, span");
        add(startButton);
        add(endButton);
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

            PlayerThread playerThread = new PlayerThread();
            playerThread.din = new DatagramSocket(port);
            playerThread.audioOut = audioOut;

            ServerVoice.isCalled = true;
            playerThread.start();

            endButton.setEnabled(true);
            startButton.setEnabled(false);


        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeAudioToWavFile(byte[] data, AudioFormat format, String fn) throws Exception {
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), format, data.length);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(fn));
    }
}
