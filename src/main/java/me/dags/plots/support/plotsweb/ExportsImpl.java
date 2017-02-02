package me.dags.plots.support.plotsweb;

import me.dags.exports.service.ExportService;
import org.spongepowered.api.Sponge;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
class ExportsImpl implements ExportHelper {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Optional<URL> getExportLink(Path path) {
        return Sponge.getServiceManager().provideUnchecked(ExportService.class).getExportURL(path);
    }
}
