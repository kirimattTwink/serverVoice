package kirimatt.threads;

import kirimatt.eventHandler.EventCallMonitor;
import kirimatt.eventHandler.events.CalledEvent;
import kirimatt.eventHandler.events.ReceiveEvent;
import kirimatt.utils.CallMonitor;
import kirimatt.utils.RtpPacket;
import kirimatt.utils.VoiceApplication;

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

        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

        CallMonitor.parseSetEvent(new ReceiveEvent(false));

        try {
            din.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        EventCallMonitor isCalled = new CalledEvent();
        EventCallMonitor isReceiveEnabled = new ReceiveEvent(true);

        while (CallMonitor.parseGetEvent(isCalled)) {

            try {
                din.receive(incoming);

                buffer = incoming.getData();

                RtpPacket rtpPacket = new RtpPacket();
                byte[] outbuf = rtpPacket.decodeG711(buffer);

                CallMonitor.parseSetEvent(isReceiveEnabled);

                audioOut.write(outbuf, 0, outbuf.length);

                for (byte b : outbuf)
                    VoiceApplication.addToReceiveBytes(b);
            } catch (SocketTimeoutException e) {

                CallMonitor.parseSetEvent(new ReceiveEvent(false));

            } catch (IOException e) {
                System.err.println("Ошибка во время выполнения потока " + e);
            }
        }

        audioOut.close();
        audioOut.drain();

        CallMonitor.parseSetEvent(new ReceiveEvent(false));

        System.out.println("Stop");
    }
}
