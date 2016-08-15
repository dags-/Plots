package me.dags.plots.generator;

import me.dags.plots.plot.PlotProvider;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

/**
 * @author dags <dags@dags.me>
 */
public interface PlotGenerator extends WorldGeneratorModifier, GenerationPopulator, BiomeGenerator {

    PlotProvider plotProvider();

    void onLoadWorld(World world);
}
