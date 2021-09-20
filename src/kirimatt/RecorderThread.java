package kirimatt;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Класс потока для передачи записанного аудио
 *
 * @author azamat
 */
public class RecorderThread extends Thread {
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
    public InetAddress serverIP;
    /**
     * Порт сервера
     */
    public int serverPort;
    /**
     * Массив байтов буфера для передачи пакетов
     */
    byte[] byteBuffer = new byte[1400];

    @Override
    public void run() {
        if (!ServerVoice.isReceive) {
            int i = 0;
            while (ServerVoice.isCalled && ServerVoice.isPressedSend) {
                try {
                    int len = ulaw.read(byteBuffer, 0, byteBuffer.length);
                    //Должен ли я использовать len для чего-то?
                    DatagramPacket data = new DatagramPacket(byteBuffer, byteBuffer.length, serverIP, serverPort);
                    System.out.println("Send #" + i++);

                    datagramSocket.send(data);
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
