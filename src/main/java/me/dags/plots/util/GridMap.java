package me.dags.plots.util;

import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */

// A two dimensional in
public class GridMap<T> {

    private static final GridMap<?> EMPTY = new GridMap<>();

    private final T[] layer;
    private final T defaultT;
    private final int xWidth, zWidth;

    public GridMap(Function<Integer, T[]> supplier, T defaultT, int xWidth, int zWidth) {
        this.xWidth = xWidth;
        this.zWidth = zWidth;
        this.layer = supplier.apply(xWidth * zWidth);
        this.defaultT = defaultT;
    }

    private GridMap() {
        this.layer = null;
        this.defaultT = null;
        this.xWidth = 0;
        this.zWidth = 0;
    }

    public T get(int x, int z) {
        T t = layer[index(x, z)];
        return t != null ? t : defaultT;
    }

    public void set(int x, int z, T t) {
        layer[index(x, z)] = t;
    }

    public void fill(T val, int xMin, int xMax, int zMin, int zMax) {
        for (int x = xMin; x < xMax; x++) {
            for (int z = zMin; z < zMax; z++) {
                set(x, z, val);
            }
        }
    }

    private int index(int x, int z) {
        if (this == EMPTY) {
            throw new UnsupportedOperationException("GridMap is empty!");
        }
        x = (x = x % xWidth) < 0 ? xWidth + x : x;
        z = (z = z % zWidth) < 0 ? zWidth + z : z;
        return x + (z * xWidth);
    }

    @SuppressWarnings("unchecked")
    public static <T> GridMap<T> empty() {
        return (GridMap<T>) EMPTY;
    }
}
