package kirimatt.gui;

import components.GuiHelper;
import kirimatt.eventHandler.events.CalledEvent;
import kirimatt.utils.CallMonitor;
import kirimatt.utils.VoiceApplication;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Главный фрейм приложения
 * отвечает за все представления серверной части
 *
 * @author azamat
 */
public class ServerFrame extends JFrame {

    public static final Dimension FRAME_SIZE = new Dimension(380, 250);
    public static final Dimension LABEL_DESC_SIZE = new Dimension(300, 30);
    public static final Dimension TEXT_FIELD_SIZE = new Dimension(240, 30);
    public static final Dimension DEFAULT_BUTTON_SIZE = new Dimension(120, 30);
    /**
     * Поле для ввода IP адреса
     */
    private final JTextField addressIPText;
    /**
     * Поле для ввода порта
     */
    private final JTextField portText;
    /**
     * Поле для ввода порта отправки
     */
    private final JTextField portSendText;
    /**
     * Кнопка начала воспроизведения записанного файла
     */
    public JButton startButton;
    /**
     * Кнопка для остановки воспроизведения
     */
    public JButton endButton;
    /**
     * Кнопка для отправки пакетов
     */
    public JButton sendButton;
    /**
     * Состояние кнопки отправки
     */
    public boolean isTurnedOn = false;

    public ServerFrame() throws HeadlessException {

        VoiceApplication voiceApplication = new VoiceApplication();

        this.setLayout(new MigLayout());
        this.setSize(FRAME_SIZE);

        JLabel clientLbl = new JLabel(GuiHelper.setHtmlTag("NEW SKYPE v2.0"));
        GuiHelper.setComponentSize(clientLbl, LABEL_DESC_SIZE);

        JLabel lblIP = new JLabel(GuiHelper.setHtmlTag("IP address send: "));
        addressIPText = new JTextField("10.3.101.1");
        GuiHelper.setComponentSize(addressIPText, TEXT_FIELD_SIZE);

        JLabel lblPortSend = new JLabel(GuiHelper.setHtmlTag("Port to send: "));
        portSendText = new JTextField("60100");
        GuiHelper.setComponentSize(portSendText, TEXT_FIELD_SIZE);

        JLabel lblPort = new JLabel(GuiHelper.setHtmlTag("Port to receive: "));
        portText = new JTextField("5555");
        GuiHelper.setComponentSize(portText, TEXT_FIELD_SIZE);

        startButton = new JButton("Start");
        startButton.addActionListener(e -> {

            voiceApplication.setServerReceivePort(Integer.parseInt(portText.getText()));
            voiceApplication.start();

            sendButton.setEnabled(true);
            endButton.setEnabled(true);

        });
        GuiHelper.setComponentSize(startButton, DEFAULT_BUTTON_SIZE);

        sendButton = new JButton("Talk");
        sendButton.addActionListener(e -> {

            voiceApplication.setServerSendPort(Integer.parseInt(portSendText.getText()));
            voiceApplication.setServerSendIP(addressIPText.getText());

            voiceApplication.talk();

            if (!isTurnedOn) {
                sendButton.setText("Shut");
                isTurnedOn = true;
            }
            else {
                sendButton.setText("Talk");
                isTurnedOn = false;
            }

        });
        GuiHelper.setComponentSize(sendButton, DEFAULT_BUTTON_SIZE);
        sendButton.setEnabled(false);

        endButton = new JButton("End");
        endButton.addActionListener(e -> {

            startButton.setEnabled(true);
            endButton.setEnabled(false);
            sendButton.setEnabled(false);

            voiceApplication.end();

        });
        GuiHelper.setComponentSize(endButton, DEFAULT_BUTTON_SIZE);
        endButton.setEnabled(false);

        JButton clipStartButton = new JButton("Clip Start");
        clipStartButton.addActionListener(e -> voiceApplication.clipStart());
        GuiHelper.setComponentSize(clipStartButton, DEFAULT_BUTTON_SIZE);

        JButton clipEndButton = new JButton("Clip End");
        clipEndButton.addActionListener(e -> voiceApplication.clipEnd());
        GuiHelper.setComponentSize(clipEndButton, DEFAULT_BUTTON_SIZE);

        add(clientLbl, "gapleft 120 ,right, wrap, span");
        add(lblIP);
        add(addressIPText, "wrap, span");
        add(lblPort);
        add(portText, "wrap, span");
        add(lblPortSend);
        add(portSendText, "wrap, span");
        add(startButton);
        add(sendButton);
        add(endButton, "wrap");
        add(clipStartButton);
        add(clipEndButton);
        //TODO: Исправить выход на более изящный.
        setDefaultCloseOperation(onCloseFrame());

    }

    public static int onCloseFrame() {
        CallMonitor.parseSetEvent(new CalledEvent(false));

        return JFrame.EXIT_ON_CLOSE;
    }
}
