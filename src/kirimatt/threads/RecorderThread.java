package kirimatt.threads;

import kirimatt.eventHandler.EventCallMonitor;
import kirimatt.eventHandler.events.CalledEvent;
import kirimatt.eventHandler.events.DuplexEvent;
import kirimatt.eventHandler.events.PressedSendEvent;
import kirimatt.eventHandler.events.ReceiveEvent;
import kirimatt.utils.CallMonitor;
import kirimatt.utils.RtpPacket;
import kirimatt.utils.VoiceApplication;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Класс потока для передачи записанного аудио
 *
 * @author azamat
 */
public class RecorderThread extends Thread {
    /**
     * Количество байт
     */
    private static final int LENGTH_BYTES = 240;
    /**
     * Микшер целевого прослушиваемого устройства
     */
    public TargetDataLine audio_in = null;
    /**
     * Кодировка
     */
    public AudioInputStream ulaw = null;
    /**
     * Сокет для передачи пакетов
     */
    public DatagramSocket datagramSocket;
    /**
     * Интернет адрес сервера
     */
    public InetAddress serverAddress;
    /**
     * Порт сервера
     */
    public int serverPort;
    /**
     * Массив байтов буфера для передачи пакетов
     */
    byte[] byteBuffer = new byte[LENGTH_BYTES];

    @Override
    public void run() {

        //TODO: На время тестов
        if (!CallMonitor.parseGetEvent(new ReceiveEvent()) || CallMonitor.parseGetEvent(new DuplexEvent())) {
            //Случай для того, когда принятых данных больше, чем отправленных.
//            if(VoiceApplication.bytesListSend.size() < VoiceApplication.bytesListReceive.size()) {
//                for (
//                        int silentIterate = 0;
//                        silentIterate < VoiceApplication.bytesListReceive.size()-VoiceApplication.bytesListSend.size();
//                        silentIterate++
//                )
//                    VoiceApplication.bytesListSend.add((byte)0); //Заполняет недостающую длину тишиной.
//            }
            int i = 0;

            byte[] bytesReadable = new byte[LENGTH_BYTES*2-24];
            byte[] ref = new byte[LENGTH_BYTES];

            EventCallMonitor isCalled = new CalledEvent();
            EventCallMonitor isPressedSend = new PressedSendEvent();

            while (CallMonitor.parseGetEvent(isCalled)
                    && CallMonitor.parseGetEvent(isPressedSend)) {
                try {

                    int len = audio_in.read(byteBuffer, 0, byteBuffer.length);

                    System.arraycopy(
                            byteBuffer,
                            0,
                            ref,
                            0,
                            byteBuffer.length
                    );

                    RtpPacket rtpPacket = new RtpPacket();

                    rtpPacket.encodeG711(byteBuffer, (short) i);

                    System.out.println("send " + i++);
                    datagramSocket.send(rtpPacket.getDatagramPacket(serverAddress, serverPort));

                    //Тишина была тут

                    AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(ref), VoiceApplication.getSendAudioFormat(), ref.length);
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                            VoiceApplication.getReceiveAudioFormat(),
                            ais
                    );
                    int lenReadable = audioInputStream.read(bytesReadable);
                    System.out.println(Arrays.toString(bytesReadable));

                    for (byte b : bytesReadable) {
                        VoiceApplication.bytesListSend.add(b);
                    }

                } catch (IOException e) {
                    System.err.println("Ошибка во время выполнения потока");
                }
        }
        audio_in.close();
        audio_in.drain();
        System.out.println("Thread stop");
        } else
            System.out.println("Receive now!");
    }
}
