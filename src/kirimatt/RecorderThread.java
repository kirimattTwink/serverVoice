package kirimatt;

import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Класс потока для передачи записанного аудио
 * @author azamat
 */
public class RecorderThread extends Thread {
    /**
     * Микшер целевого прослушиваемого устройства
     */
    public TargetDataLine audio_in = null;
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
    byte[] byteBuffer = new byte[2048];

    @Override
    public void run() {
        int i = 0;
        while (ServerVoice.isCalled) {
            try {
                audio_in.read(byteBuffer, 0, byteBuffer.length);
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
    }
}
