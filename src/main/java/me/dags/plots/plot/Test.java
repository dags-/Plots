package me.dags.plots.plot;

import com.flowpowered.math.vector.Vector2i;

import java.util.Arrays;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Test {
    public static void main(String[] args) {
        PlotBounds one = new PlotBounds(new Vector2i(23, 10), new Vector2i(42, 54));
        PlotBounds two = new PlotBounds(new Vector2i(233, 1), new Vector2i(213, 242));
        PlotBounds thr = new PlotBounds(new Vector2i(453, 345), new Vector2i(5464, 234));
        PlotBounds fou = new PlotBounds(new Vector2i(-345, -234), new Vector2i(-213, -234));
        List<PlotBounds> list = Arrays.asList(one, two, thr, fou);
        PlotMask mask = PlotMask.of(list);
        System.out.println(mask);
    }
}
