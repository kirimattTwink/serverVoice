package kirimatt.eventHandler.events;

import kirimatt.eventHandler.EventCallMonitor;

/**
 * @author azamat
 */
public class PressedSendEvent extends EventCallMonitor {
    private boolean isPressedSend;

    /**
     * Конструктор с аргументом для сеттера
     *
     * @param isPressedSend Принимает на вход булеву переменную
     */
    public PressedSendEvent(boolean isPressedSend) {
        this.isPressedSend = isPressedSend;
    }

    /**
     * Конструктор без аргументов для геттера
     */
    public PressedSendEvent() {
        isPressedSend = false;
    }

    public boolean isPressedSend() {
        return isPressedSend;
    }
}
