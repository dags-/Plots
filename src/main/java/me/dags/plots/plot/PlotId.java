package me.dags.plots.plot;

/**
 * @author dags <dags@dags.me>
 */
public class PlotId {

    private final int x;
    private final int z;
    private final int hash;

    public PlotId(int x, int z) {
        this.x = x;
        this.z = z;
        this.hash = 31 * x + z;
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
}
