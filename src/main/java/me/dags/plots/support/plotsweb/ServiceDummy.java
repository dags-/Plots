package me.dags.plots.support.plotsweb;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
class ServiceDummy implements ExportHelper {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public Optional<URL> getExportLink(Path path) {
        return Optional.empty();
    }
}
