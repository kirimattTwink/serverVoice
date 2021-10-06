package kirimatt.eventHandler.events;

import kirimatt.eventHandler.EventCallMonitor;

/**
 * @author azamat
 */
public class CalledEvent extends EventCallMonitor {
    private boolean isCalled;

    /**
     * Конструктор с аргументом для сеттера
     * @param isCalled Принимает на вход булеву переменную
     */
    public CalledEvent(boolean isCalled) {
        this.isCalled = isCalled;
    }

    /**
     * Конструктор без аргументов для геттера
     */
    public CalledEvent() {
        isCalled = false;
    }

    public boolean isCalled() {
        return isCalled;
    }
}
