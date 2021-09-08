package kirimatt;

public class ServerVoice {
    public static volatile boolean isCalled = false;
    public static void main(String[] args) {
	    ServerFrame serverFrame = new ServerFrame();
	    serverFrame.setVisible(true);
    }
}
