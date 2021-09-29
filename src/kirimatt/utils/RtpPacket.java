package kirimatt.utils;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Класс для взаимодействия с RTP пакетами, которые приходят по UDP
 *
 * @author azamat
 */
public class RtpPacket {
    /**
     * Bias for linear code.
     */
    public static final int BIAS = 0x84;

    /**
     * Коэффициент усиления звука
     */
    public static final int SOUND_AMPLIFY = (int) Math.pow(2, 8) - 64;

    /**
     * Массив для обозначения конца сегмента
     */
    public static final int[] SEG_END = {0xFF, 0x1FF, 0x3FF, 0x7FF, 0xFFF, 0x1FFF, 0x3FFF, 0x7FFF};

    /**
     * Начальный отступ для Timestamp переменной
     */
    public static final int TIMESTAMP_SHIFT = 960;
    /**
     * Массив для декодирования
     */
    short[] uLaw2L16 = new short[]{
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
    /**
     * Данные без заголовка RTP
     */
    private byte[] dataPacket;
    /**
     * Данные с заголовком RTP
     */
    private byte[] packetWithHeader;
    /**
     * Счетчик пакетов для SEQUENCE
     */
    private short counter;
    /**
     * Timestamp RTP пакета
     */
    private int timestamp;

    /**
     * Метод для преобразования линейного звука в CODEC G711 Mu Law
     *
     * @param pcm_val единица звука
     * @return Возвращает закодированную единицу звука
     */
    public static int linear2uLaw(int pcm_val) {
        int mask;
        int seg;
        //unsigned char uVal;
        int uVal;

        // Get the sign and the magnitude of the value.
        if (pcm_val < 0) {
            pcm_val = BIAS - pcm_val;
            mask = 0x7F;
        } else {
            pcm_val += BIAS;
            mask = 0xFF;
        }
        // Convert the scaled magnitude to segment number.
        seg = search(pcm_val, SEG_END);

        // Combine the sign, segment, quantization bits; and complement the code word.

        if (seg >= 8) return (0x7F ^ mask); // out of range, return maximum value.
        else {
            uVal = (seg << 4) | ((pcm_val >> (seg + 3)) & 0xF);
            return (uVal ^ mask);
        }
    }

    /**
     * Метод преобразования масштабируемой величины в номер сегмента
     *
     * @param val   значение единицы звука
     * @param table массив с байтами 0xFF, 0x1FF, 0x3FF, 0x7FF, 0xFFF, 0x1FFF, 0x3FFF, 0x7FFF
     * @return длину массива
     */
    static int search(int val, int[] table) {
        for (int i = 0; i < table.length; i++) if (val <= table[i]) return i;
        return table.length;
    }

    /**
     * Преобразование в цельный пакет RTP для UDP
     *
     * @param inetAddress IP адрес
     * @param port        Порт
     * @return Возвращает пакет для передачи по UDP
     */
    public DatagramPacket getDatagramPacket(InetAddress inetAddress, int port) {
        return new DatagramPacket(
                packetWithHeader,
                packetWithHeader.length,
                inetAddress,
                port
        );
    }

    /**
     * Добавление заголовка RTP
     */
    public void encode(byte[] dataPacket, short counter) {
        this.dataPacket = dataPacket;
        this.counter = counter;
        this.packetWithHeader = new byte[dataPacket.length + 12];

        byte lowerByte = (byte) (this.counter & 0xFF);
        byte higherByte = (byte) (this.counter >> 8);

        timestamp = dataPacket.length * counter + TIMESTAMP_SHIFT; // Длина пакета * порядковый номер + начальный сдвиг

        packetWithHeader[0] = (byte) 0x80;//const
        packetWithHeader[1] = (byte) 0x00;//const
        packetWithHeader[2] = higherByte;//volatile frame number
        packetWithHeader[3] = lowerByte;//volatile frame number
        packetWithHeader[4] = (byte) (timestamp >> 24);//volatile timestamp
        packetWithHeader[5] = (byte) (timestamp >> 16);//volatile timestamp
        packetWithHeader[6] = (byte) (timestamp >> 8);//volatile timestamp
        packetWithHeader[7] = (byte) (timestamp);//volatile timestamp
        packetWithHeader[8] = (byte) 0x02;//volatile ssrc?? once at session
        packetWithHeader[9] = (byte) 0x43;//volatile ssrc?? once at session
        packetWithHeader[10] = (byte) 0x91;//volatile ssrc?? once at session
        packetWithHeader[11] = (byte) 0xFA;//volatile ssrc?? once at session

        System.arraycopy(
                this.dataPacket,
                0,
                this.packetWithHeader,
                12,
                this.dataPacket.length
        );

    }

    /**
     * Кодирование Mu Law RTP пакета.
     * В применение к нему строго определять Аудио-Формат.
     * Обратить внимание к Sample Size In Bits, которое для ENCODE равняется 8.
     */
    public void encodeG711(byte[] dataPacket, short counter) {

        for (int i = 0; i < dataPacket.length; i++) {
            dataPacket[i] = (byte) (linear2uLaw(dataPacket[i] * SOUND_AMPLIFY));
        }

        encode(dataPacket, counter);
    }

    /**
     * Декодирование RTP пакета.
     *
     * @param data Массив байт данных, включая заголовок
     * @return Возвращает массив байт данных за исключением заголовка
     */
    public byte[] decode(byte[] data) {
        this.counter = (short) (((0xFF & data[2]) << 8) | (0xFF & data[3]));

        this.timestamp = ((0xFF & data[4]) << 24) | ((0xFF & data[5]) << 16) |
                ((0xFF & data[6]) << 8) | (0xFF & data[7]);

        this.dataPacket = new byte[data.length - 12];

        System.arraycopy(
                data,
                12,
                this.dataPacket,
                0,
                dataPacket.length
        );

        return dataPacket;
    }

    /**
     * Декодирование Mu Law RTP пакета.
     * В применение к нему строго определять Аудио-Формат.
     * Обратить внимание к Sample Size In Bits, которое для DECODE равняется 16.
     *
     * @param data массив байт данных, включая заголовок
     * @return output массив байт данных в два раза больший чем на входе, вычитая заголовок.
     */
    public byte[] decodeG711(byte[] data) {
        this.dataPacket = decode(data);

        byte[] output = new byte[dataPacket.length * 2];

        for (int j = 0; j < dataPacket.length; j++) {

            output[j * 2] = (byte) (0x00FF & uLaw2L16[dataPacket[j] + 128]);
            output[j * 2 + 1] = (byte) ((0xFF00 & uLaw2L16[dataPacket[j] + 128]) >> 8);

        }
        return output;
    }
}
