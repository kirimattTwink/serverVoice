package kirimatt;

import javax.sound.sampled.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;
import java.util.Objects;

/**
 * @author azamat
 */
public class PlayerThread extends Thread {
    public DatagramSocket din;
    public SourceDataLine audioOut;
    public byte[] buffer = new byte[512];

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

                for (byte b : buffer) ServerFrame.bytesList.add(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        audioOut.close();
        audioOut.drain();
        System.out.println("Stop");
    }
}
