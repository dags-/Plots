package me.dags.plots.support.voxelsniper.mask;

/**
 * @author dags <dags@dags.me>
 */
public interface Mask2D {

    Mask2D ALL = (x, z) -> true;

    boolean contains(int x, int z);
}
