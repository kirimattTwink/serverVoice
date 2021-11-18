package kirimatt.eventHandler.events;

import kirimatt.eventHandler.EventCallMonitor;

/**
 * @author azamat
 */
public class ReceiveEvent extends EventCallMonitor {
    private boolean isReceive;

    /**
     * Конструктор с аргументом для сеттера
     *
     * @param isReceive Принимает на вход булеву переменную
     */
    public ReceiveEvent(boolean isReceive) {
        this.isReceive = isReceive;
    }

    /**
     * Конструктор без аргументов для геттера
     */
    public ReceiveEvent() {
        isReceive = false;
    }

    public boolean isReceive() {
        return isReceive;
    }
}
