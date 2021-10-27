package kirimatt.utils;

import kirimatt.eventHandler.events.CalledEvent;
import kirimatt.eventHandler.events.PressedSendEvent;
import kirimatt.eventHandler.events.ReceiveEvent;
import kirimatt.eventHandler.events.TalkingEvent;
import kirimatt.threads.PlayerThread;
import kirimatt.threads.RecorderThread;
import lombok.SneakyThrows;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author azamat
 */
public class VoiceApplication {
    /**
     * Лист байтов для записи в WAV файл
     */
    private static volatile List<Byte> bytesListReceive = new CopyOnWriteArrayList<>();
    /**
     * Лист байтов для записи в WAV файл
     */
    private static volatile List<Byte> bytesListSend = new CopyOnWriteArrayList<>();
    /**
     * Порт для принятия пакетов
     */
    public int serverReceivePort = 5555;
    /**
     * Порт для отправки пакетов
     */
    public int serverSendPort = 60100;
    /**
     * IP адрес для отправки пакетов
     */
    public String serverSendIP = "10.3.201.1";
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

    public VoiceApplication() {
    }

    public VoiceApplication(int serverReceivePort, int serverSendPort, String serverSendIP) {
        this.serverReceivePort = serverReceivePort;
        this.serverSendPort = serverSendPort;
        this.serverSendIP = serverSendIP;
    }

    public void setServerReceivePort(int serverReceivePort) {
        this.serverReceivePort = serverReceivePort;
    }

    public void setServerSendIP(String serverSendIP) {
        this.serverSendIP = serverSendIP;
    }

    public void setServerSendPort(int serverSendPort) {
        this.serverSendPort = serverSendPort;
    }

    /**
     * Метод для инициализации константного аудио формата отправки
     *
     * @return Возвращает аудио формат
     */
    public static AudioFormat getSendAudioFormat() {
        float sampleRate = 8000f;
        int sampleSizeInBits = 8;
        int channel = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channel, signed, bigEndian);
    }

    /**
     * Метод для инициализации константного аудио формата принятия
     *
     * @return Возвращает аудио формат
     */
    public static AudioFormat getReceiveAudioFormat() {
        float sampleRate = 8000.0f;
        int sampleSizeInBits = 16;
        int channel = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channel, signed, bigEndian);
    }

    /**
     * Метод для добавления байт, которые будут отсылаться
     * Для дальнейшей записи в WAV.
     */
    public static void addToSendBytes(byte b) {
        bytesListSend.add(b);
    }

    /**
     * Метод для добавления байт, которые принимаются
     * Для дальнейшей записи в WAV.
     */
    public static void addToReceiveBytes(byte b) {
        bytesListReceive.add(b);
    }

    public void start() {
        if (datagramSocket == null || serverReceivePort != datagramSocket.getLocalPort()) {
            try {
                datagramSocket = new DatagramSocket(serverReceivePort);
            } catch (SocketException ex) {
                System.err.println("Не удалось открыть сокет");
            }
        }
        clip.ifPresent(Clip::stop);

        CallMonitor.parseSetEvent(new ReceiveEvent(true));

        initAudioReceive();
    }

    public void talk() {
        if (!CallMonitor.parseGetEvent(new TalkingEvent())) {
            if (datagramSocket == null || serverSendPort != datagramSocket.getLocalPort()) {
                try {
                    datagramSocket = new DatagramSocket(serverSendPort);
                } catch (SocketException ex) {
                    System.err.println("Не удалось открыть сокет");
                }
            }
            clip.ifPresent(Clip::stop);

            CallMonitor.parseSetEvent(new PressedSendEvent(true));

            initAudioSend();

            CallMonitor.parseSetEvent(new TalkingEvent(true));


        } else {

            CallMonitor.parseSetEvent(new PressedSendEvent(false));
            CallMonitor.parseSetEvent(new TalkingEvent(false));

        }
    }

    @SneakyThrows
    public void end() {
        CallMonitor.parseSetEvent(new CalledEvent(false));

        //Для заполнения списка тишиной
//        if (recorderThread != null)
//            recorderThread.run();

        byte[] mixedBytes = new byte[Math.min(bytesListSend.size(), bytesListReceive.size())];
        try {
            int counter = 0;
            for (int i = 0; i < mixedBytes.length; i++) {

                mixedBytes[i] = (byte) ((bytesListReceive.get(i) + bytesListSend.get(i))/2);

//                mixedBytes[i] = (byte) ((bytesListReceive.get(i) & bytesListSend.get(i)));
            }

        } catch (IndexOutOfBoundsException exception) {
            System.err.println(bytesListReceive.size());
            System.err.println(bytesListSend.size());
            System.err.println(mixedBytes.length);
        }
        System.out.println("end");

        try {
            writeAudioToWavFile(mixedBytes, getReceiveAudioFormat(), "sound.wav");
            bytesListReceive.clear();
        } catch (Exception exception) {
            System.err.println("Произошла ошибка при записи файла");
        }
    }

    public void clipStart() {
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
    }

    public void clipEnd() {
        clip.ifPresent(Clip::stop);
    }

    /**
     * Инициализация аудио.
     * Запускает поток проигрывателя
     */
    public void initAudioReceive() {
        try {
            AudioFormat format = getReceiveAudioFormat();

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

            CallMonitor.parseSetEvent(new CalledEvent(true));

            playerThread.start();


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
            AudioFormat format = getSendAudioFormat();

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
                    serverSendIP
            );
            recorderThread.audio_in = audio_in;
            recorderThread.datagramSocket = new DatagramSocket();
            recorderThread.serverAddress = inetAddress;
            recorderThread.serverPort = serverSendPort;
            System.out.println(recorderThread.serverPort);

            CallMonitor.parseSetEvent(new CalledEvent(true));

            recorderThread.start();

        } catch (LineUnavailableException | UnknownHostException | SocketException e) {
            System.err.println("Ошибка инициализации аудио" + e);
        }
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

}
