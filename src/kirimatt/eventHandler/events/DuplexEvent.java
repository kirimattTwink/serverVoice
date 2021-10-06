package kirimatt.eventHandler.events;

import kirimatt.eventHandler.EventCallMonitor;

/**
 * @author azamat
 */
public class DuplexEvent extends EventCallMonitor {
    boolean isDuplex;

    /**
     * Конструктор с аргументом для сеттера
     * @param isDuplex Принимает на вход булеву переменную
     */
    public DuplexEvent(boolean isDuplex) {
        this.isDuplex = isDuplex;
    }

    /**
     * Конструктор без аргументов для геттера
     */
    public DuplexEvent() {
    }

    public boolean isDuplex() {
        return isDuplex;
    }
}
