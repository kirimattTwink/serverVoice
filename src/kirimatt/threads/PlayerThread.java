package kirimatt.threads;

import kirimatt.gui.ServerFrame;
import kirimatt.ServerVoice;
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

    /**
     * Массив для декодирования
     */
    short[] ulaw2L16 = new short[] {
            -32124, -31100, -30076, -29052,
            -28028, -27004, -25980, -24956,
            -23932, -22908, -21884, -20860,
            -19836, -18812, -17788, -16764,
            -15996, -15484, -14972, -14460,
            -13948, -13436, -12924, -12412,
            -11900, -11388, -10876, -10364,
            -9852, -9340, -8828, -8316,
            -7932, -7676, -7420, -7164,
            -6908, -6652, -6396, -6140,
            -5884, -5628, -5372, -5116,
            -4860, -4604, -4348, -4092,
            -3900, -3772, -3644, -3516,
            -3388, -3260, -3132, -3004,
            -2876, -2748, -2620, -2492,
            -2364, -2236, -2108, -1980,
            -1884, -1820, -1756, -1692,
            -1628, -1564, -1500, -1436,
            -1372, -1308, -1244, -1180,
            -1116, -1052, -988, -924,
            -876, -844, -812, -780,
            -748, -716, -684, -652,
            -620, -588, -556, -524,
            -492, -460, -428, -396,
            -372, -356, -340, -324,
            -308, -292, -276, -260,
            -244, -228, -212, -196,
            -180, -164, -148, -132, -120,
            -112, -104, -96, -88, -80, -72, -64, -56,
            -48, -40, -32, -24, -16, -8, 0, 32124,
            31100, 30076, 29052, 28028,
            27004, 25980, 24956, 23932,
            22908, 21884, 20860, 19836,
            18812, 17788, 16764, 15996,
            15484, 14972, 14460, 13948,
            13436, 12924, 12412, 11900,
            11388, 10876, 10364, 9852,
            9340, 8828, 8316, 7932,
            7676, 7420, 7164, 6908,
            6652, 6396, 6140, 5884,
            5628, 5372, 5116, 4860,
            4604, 4348, 4092, 3900,
            3772, 3644, 3516, 3388,
            3260, 3132, 3004, 2876,
            2748, 2620, 2492, 2364,
            2236, 2108, 1980, 1884,
            1820, 1756, 1692, 1628,
            1564, 1500, 1436, 1372,
            1308, 1244, 1180, 1116,
            1052, 988, 924, 876,
            844, 812, 780, 748, 716, 684, 652, 620,
            588, 556, 524, 492, 460, 428, 396, 372,
            356, 340, 324, 308, 292, 276, 260, 244,
            228, 212, 196, 180, 164, 148, 132, 120,
            112, 104, 96, 88, 80, 72, 64, 56,
            48, 40, 32, 24, 16, 8, 0
    };

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

//                byte[] outbuf = new byte[LENGTH_BYTES*2];

                ServerVoice.isReceive = true;

//                int len = buffer.length;
//
//                for (int j=0; j<len; j++) {
////                    int currentIndex = buffer[i] & 0xFF;
////                    outbuf[j++] = muLawDecompressTable_low[currentIndex];
////                    outbuf[j++] = muLawDecompressTable_high[currentIndex];
//
//                    outbuf[j*2] = (byte) (0x00FF & ulaw2L16[incoming.getData()[j] + 128]);
//                    outbuf[j*2+1] = (byte)((0xFF00 & ulaw2L16[incoming.getData()[j] + 128]) >> 8);
//
//                    if(j < 24) {
//                        outbuf[j] = 0;
//                    }
////                    outbuf[j*2] = (byte) ((byte) (ulaw2linear(buffer[j]) & 0x00FF)>>4);
////                    outbuf[j*2+1] = (byte) ((ulaw2linear((buffer[j] ))& 0xFF00)>>8);
//
////                    buffer[j] = (byte) ((ulaw2linear((buffer[j] ))& 0xFF00)>>8);
//                }
//                srcLine.write(outbuf, 0, len*2);

                audioOut.write(outbuf, 0, outbuf.length);
//                System.out.println("#" + i++);

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
