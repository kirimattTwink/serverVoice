package kirimatt.eventHandler.events;

import kirimatt.eventHandler.EventCallMonitor;

/**
 * @author azamat
 */
public class TalkingEvent extends EventCallMonitor {
    private boolean isTalking;

    /**
     * Конструктор с аргументом для сеттера
     *
     * @param isTalking Принимает на вход булеву переменную
     */
    public TalkingEvent(boolean isTalking) {
        this.isTalking = isTalking;
    }

    /**
     * Конструктор без аргументов для геттера
     */
    public TalkingEvent() {
        isTalking = false;
    }

    public boolean isTalking() {
        return isTalking;
    }
}
