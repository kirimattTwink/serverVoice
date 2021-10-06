package kirimatt.utils;

import kirimatt.eventHandler.EventCallMonitor;
import kirimatt.eventHandler.events.*;

/**
 * @author azamat
 */
public class CallMonitor {

    /**
     * Переменная состояния звонка
     */
    private static volatile boolean isCalled = false;
    /**
     * Переменная состояния нажатой кнопки разговора
     */
    private static volatile boolean isPressedSend = false;
    /**
     * Переменная состояния принятия пакетов
     */
    private static volatile boolean isReceive = false;
    /**
     * Переменная состояния "говорит ли пользователь на данный момент"
     */
    private static volatile boolean isTalking = false;
    /**
     * Переменная состояния дуплексовой связи
     */
    private static volatile boolean isDuplex = true;

    /**
     * Монитор сеттера
     * Выставляет значения в этом классе
     * @param event Принимает на вход SetEventCallMonitor для геттеров (Использовать конструктор с параметром)
     */
    public static void parseSetEvent(EventCallMonitor event) {
        if (event instanceof CalledEvent) {
           isCalled = ((CalledEvent) event).isCalled();

        } else if (event instanceof PressedSendEvent) {
            isPressedSend = ((PressedSendEvent) event).isPressedSend();

        } else if (event instanceof ReceiveEvent) {
            isReceive = ((ReceiveEvent) event).isReceive();

        } else if (event instanceof TalkingEvent) {
            isTalking = ((TalkingEvent) event).isTalking();

        } else if (event instanceof DuplexEvent) {
            isDuplex = ((DuplexEvent) event).isDuplex();

        } else
            throw new IllegalArgumentException("Неизвестное событие" + event);

    }

    /**
     * Монитор геттера
     * @param event Принимает на вход EventCallMonitor для геттеров (Использовать конструктор без параметров)
     * @return Возвращает значения для манипулирования потоком
     */
    public static boolean parseGetEvent(EventCallMonitor event) {
        if (event instanceof CalledEvent) {
            return isCalled;

        } else if (event instanceof PressedSendEvent) {
            return isPressedSend;

        } else if (event instanceof ReceiveEvent) {
            return isReceive;

        } else if (event instanceof TalkingEvent) {
            return isTalking;

        } else if (event instanceof DuplexEvent) {
            return isDuplex;

        } else
            throw new IllegalArgumentException("Неизвестное событие" + event);
    }
}
