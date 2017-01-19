package me.dags.plots.support.voxelsniper.mask;

/**
 * @author dags <dags@dags.me>
 */
public interface Mask {

    Mask ALL = new Mask() {
        @Override
        public boolean contains(int x, int y, int z) {
            return true;
        }

        @Override
        public Mask2D toMask2D() {
            return Mask2D.ALL;
        }
    };

    boolean contains(int x, int y, int z);

    Mask2D toMask2D();
}
