package kirimatt.threads;

import kirimatt.ServerVoice;
import kirimatt.utils.RtpPacket;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
    public InetAddress serverIP;
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
//        if (!ServerVoice.isReceive) {
        int i = 0;
        while (ServerVoice.isCalled && ServerVoice.isPressedSend) {
            try {

                int len = audio_in.read(byteBuffer, 0, byteBuffer.length);


////                    AudioInputStream in = new AudioInputStream(audio_in);
////                    CompressInputStream compressInputStream = new CompressInputStream(in, false);
////                    int len = compressInputStream.read(byteBuffer);
//
////                    System.err.println("1");

//                    //Должен ли я использовать len для чего-то?
////                    System.err.println(Arrays.toString(byteBuffer));

//                    int len = audio_in.read(byteBuffer, 0, byteBuffer.length);

//                    short[] dataAudio = new short[len];
//                    for (int j = 0; j < len; j++) {
//                        dataAudio[j] = byteBuffer[j];
//                    }
//                    byte[] endByteDataUlaw = new byte[252];
////                    System.arraycopy(
////                            getByteArray(dataAudio),
////                            0,
////                            endByteDataUlaw,
////                            12
////                    );
//
//
//
////                    byte[] endByteDataUlaw = getByteArray(dataAudio);
////                    System.out.println(len + ", " + endByteDataUlaw.length);
////                    RtpPacket data = new RtpPacket(new ByteBufferKaitaiStream(endByteDataUlaw));
//////                    DatagramPacket data = new RtpPacket(endByteDataUlaw, endByteDataUlaw.length, InetAddress.getByName("10.3.101.1"), 60510);
////                    DatagramPacket datagramPacket = new DatagramPacket(
////                            data.headerExtension(),
////                            252,
////                            InetAddress.getByName("10.3.101.1"),
////                            60510
////                    );
//
//                    DatagramPacket data = new DatagramPacket(endByteDataUlaw, endByteDataUlaw.length, InetAddress.getByName("10.3.101.1"), 60510);
                RtpPacket rtpPacket = new RtpPacket();

                rtpPacket.encodeG711(byteBuffer, (short) i);

                System.out.println("send " + i++);
                datagramSocket.send(rtpPacket.getDatagramPacket(InetAddress.getByName("10.3.201.1"), serverPort));
//                    for (byte b : byteBuffer)
//                        ServerFrame.bytesList.add(b);
            } catch (IOException e) {
                System.err.println("Ошибка во время выполнения потока");
            }
        }
        audio_in.close();
        audio_in.drain();
        System.out.println("Thread stop");
//        } else
//            System.out.println("Receive now!");
    }
}
