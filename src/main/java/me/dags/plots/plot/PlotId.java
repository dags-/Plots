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
        this.hash = 31 * x + z;
    }

    public boolean isPresent() {
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
        return x + ":" + z;
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other instanceof PlotId && this.hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public static PlotId valueOf(String input) {
        if (input != null && input.length() >= 3) {
            String[] split = input.split(":");
            try {
                int x = Integer.valueOf(split[0]);
                int z = Integer.valueOf(split[1]);
                return new PlotId(x, z);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
