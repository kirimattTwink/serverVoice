package kirimatt;

import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author azamat
 */
public class PlayerThread extends Thread {
    public volatile DatagramSocket din;
    public SourceDataLine audioOut;
    public byte[] buffer = new byte[2048];

    @Override
    public void run() {
        int i = 0;
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        while (ServerVoice.isCalled) {
            try {
                din.receive(incoming);
                buffer = incoming.getData();

                //TODO: Расскоментировать для включения реал-тайм звука.
                //audioOut.write(buffer, 0,buffer.length);
                System.out.println("#" + i++);

                for (byte b : buffer)
                    ServerFrame.bytesList.add(b);
            } catch (IOException e) {
                System.err.println("Ошибка во время выполнения потока " + e);
            }
        }

        audioOut.close();
        audioOut.drain();

        System.out.println("Stop");
    }
}
