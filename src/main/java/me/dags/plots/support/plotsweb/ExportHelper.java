package me.dags.plots.support.plotsweb;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public interface ExportHelper {

    boolean isEnabled();

    Optional<URL> lookup(Object obj);

    Optional<URL> getExportLink(Path path);

    Optional<URL> getExportLink(String name, byte[] data);
}
