package techeart.thadv.content.world.structures;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fmllegacy.RegistryObject;
import techeart.thadv.content.MainClass;
import techeart.thadv.content.RegistryHandler;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompleteStructureFeature<C extends FeatureConfiguration>
{
    protected static Random random = new Random();

    protected final String name;
    protected final StructureFeatureConfiguration spacingConfig;
    protected final C featureConfig;

    protected final RegistryObject<StructureFeature<C>> structureRO;
    protected ConfiguredStructureFeature<?, ?> configuredFeature;

    protected boolean transformsLand = false;
    protected Function<BiomeLoadingEvent, Boolean> canGenerate = ble -> true;

    public CompleteStructureFeature(String name, Supplier<StructureFeature<C>> factory, C featureConfig, int avgChunksBetweenStr, int minChunksBetweenStr)
    {
        this.name = name;
        this.featureConfig = featureConfig;

        int seedModifier = random.nextInt(Integer.MAX_VALUE - 1000000000) + 1000000000;
        spacingConfig = new StructureFeatureConfiguration(avgChunksBetweenStr, minChunksBetweenStr, seedModifier);

        structureRO = RegistryHandler.STRUCTURES.register(name, factory);
    }

    public CompleteStructureFeature<C> transformsLand()
    {
        transformsLand = true;
        return this;
    }

    public CompleteStructureFeature<C> generateWhen(Function<BiomeLoadingEvent, Boolean> canGenerate)
    {
        this.canGenerate = canGenerate;
        return this;
    }

    public String getName() { return name; }

    public RegistryObject<StructureFeature<C>> getStructureRO() { return structureRO; }

    public StructureFeature<C> getStructure() { return structureRO.get(); }

    public ConfiguredStructureFeature<?, ?> getConfiguredFeature() { return configuredFeature; }

    protected void registerConfiguredFeature()
    {
        configuredFeature = getStructure().configured(featureConfig);
        Registry<ConfiguredStructureFeature<?, ?>> registry = BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE;
        Registry.register(registry, MainClass.RLof("configured_" + name), configuredFeature);
    }

    public boolean canGenerate(BiomeLoadingEvent biomeLoadingEvent) { return canGenerate.apply(biomeLoadingEvent); }
}
