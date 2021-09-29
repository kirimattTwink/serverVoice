package kirimatt.threads;

import kirimatt.ServerVoice;
import kirimatt.gui.ServerFrame;
import kirimatt.utils.RtpPacket;

import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Поток проигрывателя.
 * Принимает пакеты и обрабатывает их.
 * Закомментирована возможность проигрывания в реальном времени.
 * Записывает байты в список, для дальнейшего преобразования в файл
 *
 * @author azamat
 */
public class PlayerThread extends Thread {

    /**
     * Количество байт в пакете
     */
    public static final int LENGTH_BYTES = 240;
    /**
     * Сокет для принятия пакетов
     */
    public volatile DatagramSocket din;
    /**
     * Микшер
     */
    public SourceDataLine audioOut;
    /**
     * Массив байтов для принятия пакетов
     */
    public byte[] buffer = new byte[LENGTH_BYTES];

    @Override
    public void run() {
        int i = 0;
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        ServerVoice.isReceive = false;
        try {
            din.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (ServerVoice.isCalled) {

            try {
                din.receive(incoming);

                buffer = incoming.getData();

                RtpPacket rtpPacket = new RtpPacket();
                byte[] outbuf = rtpPacket.decodeG711(buffer);

                ServerVoice.isReceive = true;

                audioOut.write(outbuf, 0, outbuf.length);

                for (byte b : outbuf)
                    ServerFrame.bytesList.add(b);
            } catch (SocketTimeoutException e) {
                ServerVoice.isReceive = false;
            } catch (IOException e) {
                System.err.println("Ошибка во время выполнения потока " + e);
            }
        }

        audioOut.close();
        audioOut.drain();

        ServerVoice.isReceive = false;

        System.out.println("Stop");
    }
}
