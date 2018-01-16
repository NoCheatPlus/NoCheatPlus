package fr.neatmonster.nocheatplus.event.mini;

/**
 * Minimal listener for one event.
 * @author asofold
 *
 * @param <E>
 */
public interface MiniListener<E> {
    public void onEvent(E event);
}
