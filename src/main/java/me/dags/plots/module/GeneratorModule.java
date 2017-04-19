package me.dags.plots.module;

import me.dags.plots.Plots;
import me.dags.plots.generator.GeneratorProperties;
import org.spongepowered.api.registry.CatalogRegistryModule;

import java.util.Collection;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class GeneratorModule implements CatalogRegistryModule<GeneratorProperties> {

    @Override
    public Optional<GeneratorProperties> getById(String id) {
        return Plots.core().baseGenerator(id);
    }

    @Override
    public Collection<GeneratorProperties> getAll() {
        return Plots.core().allGenerators();
    }
}
