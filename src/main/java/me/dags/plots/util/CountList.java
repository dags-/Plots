package me.dags.plots.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public class CountList<V, T> {

    private final Multimap<V, T> map = ArrayListMultimap.create();
    private final List<V> order = new ArrayList<>();
    private final Comparator<V> reverseComparator;
    private final Comparator<V> comparator;
    private final int capacity;

    public CountList(Comparator<V>  comparator, int capacity) {
        this.reverseComparator = (v1, v2) -> comparator.compare(v2, v1);
        this.comparator = comparator;
        this.capacity = capacity;
    }

    public Stream<T> get() {
        return order.stream().distinct().map(map::get).flatMap(Collection::stream);
    }

    public void add(V key, T value) {
        if (order.size() < capacity) {
            addValue(key, value);
        } else {
            checkAndReplace(key, value);
        }
    }

    private void checkAndReplace(V key, T value) {
        V removeKey = null;
        for (int i = order.size() - 1; i > 0; i--) {
            V test = order.get(i);
            if (comparator.compare(key, test) > 0) {
                removeKey = test;
                break;
            }
        }
        if (removeKey != null) {
            replace(removeKey, key, value);
        }
    }

    private void replace(V oldKey, V newKey, T newValue) {
        Collection<T> vals = map.get(oldKey);
        if (vals.size() > 0) {
            T removeVal = vals.iterator().next();
            map.remove(oldKey, removeVal);
            order.remove(oldKey);
            addValue(newKey, newValue);
        }
    }

    private void addValue(V v, T t) {
        map.put(v, t);
        order.add(v);
        Collections.sort(order, reverseComparator);
    }
}
