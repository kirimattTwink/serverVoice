package kirimatt;

public class ServerVoice {
    /**
     * Переменная для манипулирования выполнения потока
     */
    public static volatile boolean isCalled = false;

    public static void main(String[] args) {
        ServerFrame serverFrame = new ServerFrame();
        serverFrame.setVisible(true);
    }
}
