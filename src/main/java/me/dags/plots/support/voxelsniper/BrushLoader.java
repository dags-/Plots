package me.dags.plots.support.voxelsniper;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author dags <dags@dags.me>
 */
public class BrushLoader {

    private static final String NAME = "com.thevoxelbox.voxelsniper.brush.Brush";
    private static final String PATH = "com/thevoxelbox/voxelsniper/brush/Brush.class";

    // This is kinda horrible, sorry
    static void load() {
        try {
            new Loader().loadClass(NAME);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static class Loader extends ClassLoader {

        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.equals(BrushLoader.NAME)) {
                try (InputStream inputStream = BrushLoader.class.getClassLoader().getResourceAsStream(BrushLoader.PATH)) {
                    byte[] data = new byte[10000];
                    int length = inputStream.read(data);
                    return defineClass(name, data, 0, length);
                } catch (IOException e) {
                    throw new ClassNotFoundException(name, e);
                }
            }
            return Class.forName(name);
        }
    }
}
