package kirimatt;

import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author azamat
 */
public class PlayerThread extends Thread {
    public DatagramSocket din;
    public SourceDataLine audioOut;
    byte[] buffer = new byte[512];

    @Override
    public void run() {
        int i = 0;
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        while (ServerVoice.isCalled) {
            try {
                din.receive(incoming);
                buffer = incoming.getData();
                audioOut.write(buffer, 0,buffer.length);
                System.out.println("#" + i++);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        audioOut.close();
        audioOut.drain();
        System.out.println("Stop");
    }
}
