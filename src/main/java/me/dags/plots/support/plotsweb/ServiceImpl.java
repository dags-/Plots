package me.dags.plots.support.plotsweb;

import me.dags.plotsweb.service.DataStore;
import me.dags.plotsweb.service.PlotsWebService;
import org.spongepowered.api.Sponge;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
class ServiceImpl implements ExportHelper {

    @Override
    public boolean isEnabled() {
        return Sponge.getServiceManager().provideUnchecked(PlotsWebService.class).running();
    }

    @Override
    public Optional<URL> lookup(Object obj) {
        return Sponge.getServiceManager().provideUnchecked(PlotsWebService.class).lookupURL(obj);
    }

    @Override
    public Optional<URL> getExportLink(Path path) {
        PlotsWebService service = Sponge.getServiceManager().provideUnchecked(PlotsWebService.class);
        DataStore store = service.newFileDataStore(path);
        return service.registerDataStore(store);
    }

    @Override
    public Optional<URL> getExportLink(String name, byte[] data) {
        PlotsWebService service = Sponge.getServiceManager().provideUnchecked(PlotsWebService.class);
        DataStore store = service.newMemoryDataStore(name, data);
        return service.registerDataStore(store);
    }
}
