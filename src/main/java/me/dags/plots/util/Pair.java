package me.dags.plots.util;

/**
 * @author dags <dags@dags.me>
 */
public class Pair<T, T1> {

    private static final Pair<?,?> EMPTY = new Pair<>(null, null);

    private final T first;
    private final T1 second;

    private Pair(T t, T1 t1) {
        this.first = t;
        this.second = t1;
    }

    public T first() {
        return first;
    }

    public T1 second() {
        return second;
    }

    public boolean present() {
        return this != EMPTY;
    }

    public static <T, T1> Pair<T, T1> of(T t, T1 t1) {
        return t == null || t1 == null ? empty() : new Pair<>(t, t1);
    }

    @SuppressWarnings("unchecked")
    public static <T, T1> Pair<T, T1> empty() {
        return (Pair<T, T1>) EMPTY;
    }
}
