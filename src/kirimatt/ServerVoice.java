package kirimatt;

public class ServerVoice {
    /**
     * Переменные для манипулирования выполнением потока
     */
    public static volatile boolean isCalled = false;
    public static volatile boolean isReceive = false;
    public static volatile boolean isPressedSend = false;
    public static volatile boolean isTalking = false;

    public static void main(String[] args) {
        ServerFrame serverFrame = new ServerFrame();
        serverFrame.setVisible(true);
    }
}
