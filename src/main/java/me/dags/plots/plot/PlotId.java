package me.dags.plots.plot;

/**
 * @author dags <dags@dags.me>
 */
public class PlotId {

    public static final PlotId EMPTY = new PlotId();

    private final int x;
    private final int z;
    private final int hash;

    private PlotId() {
        this.x = Integer.MIN_VALUE;
        this.z = Integer.MIN_VALUE;
        this.hash = Integer.MIN_VALUE;
    }

    public PlotId(int x, int z) {
        this.x = x;
        this.z = z;
        this.hash = hash(x, z);
    }

    public boolean present() {
        return this != EMPTY;
    }

    public int plotX() {
        return x;
    }

    public int plotZ() {
        return z;
    }

    @Override
    public String toString() {
        return string(x, z);
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other instanceof PlotId && this.hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    static int hash(int x, int z) {
        return 31 * x + z;
    }

    static int transform(int coord, int gridWidth) {
        return (coord < 0 ? coord - gridWidth : coord) / gridWidth;
    }

    public static String string(int x, int z) {
        return x + ":" + z;
    }

    public static boolean isValid(String input) {
        return input.matches("-?\\d+:-?\\d+");
    }

    public static PlotId of(int x, int z) {
        return new PlotId(x, z);
    }

    public static PlotId parse(String input) {
        if (input != null && isValid(input)) {
            String[] split = input.split(":");
            int x = Integer.valueOf(split[0]);
            int z = Integer.valueOf(split[1]);
            return new PlotId(x, z);
        }
        return EMPTY;
    }
}
