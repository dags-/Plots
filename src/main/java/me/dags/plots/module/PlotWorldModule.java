package me.dags.plots.module;

import me.dags.plots.Plots;
import me.dags.plots.plot.PlotWorld;
import org.spongepowered.api.registry.CatalogRegistryModule;

import java.util.Collection;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class PlotWorldModule implements CatalogRegistryModule<PlotWorld> {

    @Override
    public Optional<PlotWorld> getById(String id) {
        return Plots.core().plotWorldExact(id);
    }

    @Override
    public Collection<PlotWorld> getAll() {
        return Plots.core().allWorlds();
    }
}
