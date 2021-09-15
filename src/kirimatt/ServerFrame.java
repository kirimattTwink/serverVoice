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
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Главный фрейм приложения
 * отвечает за все представления серверной части
 *
 * @author azamat
 */
public class ServerFrame extends JFrame {

    /**
     * Лист байтов для записи в WAV файл
     */
    public static List<Byte> bytesList = new ArrayList<>();
    /**
     * Поле для ввода IP адреса
     */
    private final JTextField addressIPText;
    /**
     * Поле для ввода порта
     */
    private final JTextField portText;
    /**
     * Порт для принятия пакетов
     */
    public int port = 8888;
    /**
     * IP адрес по умолчанию
     */
    public String serverIP = "127.0.0.1";
    /**
     * Сокет
     */
    public DatagramSocket datagramSocket;
    /**
     * Поток с принятием пакетов
     */
    public PlayerThread playerThread;
    /**
     * Микшер для вывода
     */
    public SourceDataLine audioOut;
    /**
     * Клип для воспроизведения файла
     */
    @SuppressWarnings("all")
    public Optional<Clip> clip = Optional.empty();
    /**
     * Кнопка начала воспроизведения записанного файла
     */
    public JButton startButton;
    /**
     * Кнопка для остановки воспроизведения
     */
    public JButton endButton;

    public ServerFrame() throws HeadlessException {

        this.setLayout(new MigLayout());
        this.setSize(new Dimension(260, 220));

        JLabel clientLbl = new JLabel(GuiHelper.setHtmlTag("NEW SKYPE v2.0"));
        GuiHelper.setComponentSize(clientLbl, new Dimension(200, 30));

        JLabel lblIP = new JLabel(GuiHelper.setHtmlTag("IP address: "));
        addressIPText = new JTextField("10.255.253.146");
        GuiHelper.setComponentSize(addressIPText, new Dimension(120, 30));

        JLabel lblPort = new JLabel(GuiHelper.setHtmlTag("Port to connect: "));
        portText = new JTextField("8888");
        GuiHelper.setComponentSize(portText, new Dimension(120, 30));

        startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            if (datagramSocket == null || Integer.parseInt(portText.getText()) != datagramSocket.getLocalPort()) {
                try {
                    datagramSocket = new DatagramSocket(portText.getText().isEmpty() ? port : Integer.parseInt(portText.getText()));
                } catch (SocketException ex) {
                    System.err.println("Не удалось открыть сокет");
                }
            }
            clip.ifPresent(Clip::stop);

            initAudioReceive();
            initAudioSend();
        });
        GuiHelper.setComponentSize(startButton, new Dimension(120, 30));

        endButton = new JButton("End");
        endButton.addActionListener(e -> {

            ServerVoice.isCalled = false;
            startButton.setEnabled(true);
            endButton.setEnabled(false);

            byte[] arrayByte = new byte[bytesList.size()];
            int r = 0;
            for (Byte b : bytesList)
                arrayByte[r++] = b;
            try {
                writeAudioToWavFile(arrayByte, getAudioFormat(), "sound.wav");
                bytesList.clear();
            } catch (Exception exception) {
                System.err.println("Произошла ошибка при записи файла");
            }

        });
        GuiHelper.setComponentSize(endButton, new Dimension(120, 30));
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
                System.err.println("Возникла ошибка при воспроизведении файла");
            }

        });
        GuiHelper.setComponentSize(clipStartButton, new Dimension(120, 30));

        JButton clipEndButton = new JButton("Clip End");
        clipEndButton.addActionListener(e -> clip.ifPresent(Clip::stop));
        GuiHelper.setComponentSize(clipEndButton, new Dimension(120, 30));

        add(clientLbl, "gapleft 80 ,right, wrap, span");
        add(lblIP);
        add(addressIPText, "wrap");
        add(lblPort);
        add(portText, "wrap");
        add(startButton);
        add(endButton, "wrap");
        add(clipStartButton);
        add(clipEndButton);

    }

    /**
     * Метод для инициализации константного аудио формата
     *
     * @return Возвращает аудио формат
     */
    public static AudioFormat getAudioFormat() {
        float sampleRate = 16000.0f;
        int sampleSizeInBits = 16;
        int channel = 2;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channel, signed, bigEndian);
    }

    /**
     * Метод для записи в WAV файл
     *
     * @param data   Байты
     * @param format Формат аудио
     * @param fn     Строка пути к новому файлу
     * @throws Exception Ошибка при записи файла
     */
    public static void writeAudioToWavFile(byte[] data, AudioFormat format, String fn) throws Exception {
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), format, data.length);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(fn));
    }

    /**
     * Инициализация аудио.
     * Запускает поток проигрывателя
     */
    public void initAudioReceive() {
        try {
            AudioFormat format = getAudioFormat();

            DataLine.Info infoOut = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(infoOut)) {
                System.out.println("Not support");
                System.exit(0);
            }

            audioOut = (SourceDataLine) AudioSystem.getLine(infoOut);

            audioOut.open(format);

            audioOut.start();

            playerThread = new PlayerThread();

            playerThread.din = datagramSocket;
            playerThread.audioOut = audioOut;

            ServerVoice.isCalled = true;
            playerThread.start();

            endButton.setEnabled(true);
            startButton.setEnabled(false);

        } catch (LineUnavailableException e) {
            System.err.println("Линия не доступна");
        }
    }

    /**
     * Метод по инициализации аудио.
     * Запускает поток передачи пакетов с аудио.
     */
    public void initAudioSend() {
        try {
            AudioFormat format = getAudioFormat();

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Not support");
                System.exit(0);
            }

            TargetDataLine audio_in = (TargetDataLine) AudioSystem.getLine(info);

            audio_in.open(format);

            audio_in.start();

            RecorderThread recorderThread = new RecorderThread();
            InetAddress inetAddress = InetAddress.getByName(
                    addressIPText.getText().isEmpty() ? serverIP : addressIPText.getText()
            );
            recorderThread.audio_in = audio_in;
            recorderThread.datagramSocket = new DatagramSocket();
            recorderThread.serverIP = inetAddress;
            recorderThread.serverPort = portText.getText().isEmpty() ? port : Integer.parseInt(portText.getText());
            ServerVoice.isCalled = true;
            recorderThread.start();
            startButton.setEnabled(false);
            endButton.setEnabled(true);

        } catch (LineUnavailableException | SocketException | UnknownHostException e) {
            System.err.println("Ошибка инициализации аудио" + e);
        }
    }
}
