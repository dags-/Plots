package me.dags.plots.support.plotsweb;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public interface ExportHelper {

    boolean isEnabled();

    Optional<URL> getExportLink(Path path);
}
