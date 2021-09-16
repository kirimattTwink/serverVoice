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

    public static final Dimension FRAME_SIZE = new Dimension(380, 220);
    public static final Dimension LABEL_DESC_SIZE = new Dimension(300, 30);
    public static final Dimension TEXT_FIELD_SIZE = new Dimension(240, 30);
    public static final Dimension DEFAULT_BUTTON_SIZE = new Dimension(120, 30);
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
     * Поток с отправкой пакетов
     */
    public RecorderThread recorderThread;
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
    /**
     * Кнопка для отправки пакетов
     */
    public JButton sendButton;

    public ServerFrame() throws HeadlessException {

        this.setLayout(new MigLayout());
        this.setSize(FRAME_SIZE);

        JLabel clientLbl = new JLabel(GuiHelper.setHtmlTag("NEW SKYPE v2.0"));
        GuiHelper.setComponentSize(clientLbl, LABEL_DESC_SIZE);

        JLabel lblIP = new JLabel(GuiHelper.setHtmlTag("IP address: "));
        addressIPText = new JTextField("10.255.253.146");
        GuiHelper.setComponentSize(addressIPText, TEXT_FIELD_SIZE);

        JLabel lblPort = new JLabel(GuiHelper.setHtmlTag("Port to connect: "));
        portText = new JTextField("8888");
        GuiHelper.setComponentSize(portText, TEXT_FIELD_SIZE);

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

            ServerVoice.isReceive = true;
            initAudioReceive();

            sendButton.setEnabled(true);

        });
        GuiHelper.setComponentSize(startButton, DEFAULT_BUTTON_SIZE);


        sendButton = new JButton("Talk");
        sendButton.addActionListener(e -> {

            if (!ServerVoice.isTalking) {
                if (datagramSocket == null || Integer.parseInt(portText.getText()) != datagramSocket.getLocalPort()) {
                    try {
                        datagramSocket = new DatagramSocket(portText.getText().isEmpty() ? port : Integer.parseInt(portText.getText()));
                    } catch (SocketException ex) {
                        System.err.println("Не удалось открыть сокет");
                    }
                }
                clip.ifPresent(Clip::stop);

                ServerVoice.isPressedSend = true;

                initAudioSend();

                ServerVoice.isTalking = true;
                if (!ServerVoice.isReceive)
                    sendButton.setText("Shut");
            } else {
                ServerVoice.isPressedSend = false;
                ServerVoice.isTalking = false;
                sendButton.setText("Talk");
            }

        });
        GuiHelper.setComponentSize(sendButton, DEFAULT_BUTTON_SIZE);
        sendButton.setEnabled(false);

        endButton = new JButton("End");
        endButton.addActionListener(e -> {

            ServerVoice.isCalled = false;
            startButton.setEnabled(true);
            endButton.setEnabled(false);
            sendButton.setEnabled(false);

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
        GuiHelper.setComponentSize(endButton, DEFAULT_BUTTON_SIZE);
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
        GuiHelper.setComponentSize(clipStartButton, DEFAULT_BUTTON_SIZE);

        JButton clipEndButton = new JButton("Clip End");
        clipEndButton.addActionListener(e -> clip.ifPresent(Clip::stop));
        GuiHelper.setComponentSize(clipEndButton, DEFAULT_BUTTON_SIZE);

        add(clientLbl, "gapleft 120 ,right, wrap, span");
        add(lblIP);
        add(addressIPText, "wrap, span");
        add(lblPort);
        add(portText, "wrap, span");
        add(startButton);
        add(sendButton);
        add(endButton, "wrap");
        add(clipStartButton);
        add(clipEndButton);
        //TODO: Исправить выход на более изящный.
        setDefaultCloseOperation(onCloseFrame());

//        this.addKeyListener(new KeyListener() {
//
//
//            @Override
//            public void keyTyped(KeyEvent e) {
//
//            }
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//                System.out.println("Key pressed code=" + e.getKeyCode() + ", char=" + e.getKeyChar());
//                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
//                    System.err.println("1");
//                    ServerVoice.isPressedSend = true;
//                    recorderThread.start();
//                }
//            }
//
//            @Override
//            public void keyReleased(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
//                    ServerVoice.isPressedSend = false;
//                }
//            }
//        });
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

            recorderThread = new RecorderThread();
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

    public int onCloseFrame() {
        ServerVoice.isCalled = false;
        return JFrame.EXIT_ON_CLOSE;
    }
}
