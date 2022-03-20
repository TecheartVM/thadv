package techeart.thadv.content.world.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fmllegacy.RegistryObject;
import techeart.thadv.content.RegistryHandler;

import java.util.*;
import java.util.function.Supplier;

public class StructuresHandler
{
    private static final Set<CompleteStructureFeature<?>> REGISTERED = new HashSet<>();

    public static <C extends FeatureConfiguration> CompleteStructureFeature<C> register(
            String name, Supplier<StructureFeature<C>> factory, C featureConfig,
            int avgChunksBetweenStr, int minChunksBetweenStr)
    {
        CompleteStructureFeature<C> result = new CompleteStructureFeature<>(
                name, factory, featureConfig, avgChunksBetweenStr, minChunksBetweenStr
        );
        REGISTERED.add(result);
        return result;
    }

    //called in FMLCommonSetupEvent
    public static void registerStructures()
    {
        Map<StructureFeature<?>, StructureFeatureConfiguration> features = new HashMap<>();
        List<StructureFeature<?>> noiseAffecting = new ArrayList<>();

        REGISTERED.forEach(csf -> {
            StructureFeature<?> structure = csf.getStructure();

            StructureFeature.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);
            if(csf.transformsLand) noiseAffecting.add(structure);
            features.put(structure, csf.spacingConfig);

            csf.registerConfiguredFeature();
        });

        StructureFeature.NOISE_AFFECTING_FEATURES = ImmutableList.<StructureFeature<?>>builder()
                .addAll(StructureFeature.NOISE_AFFECTING_FEATURES).addAll(noiseAffecting).build();

        StructureSettings.DEFAULTS = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder()
                .putAll(StructureSettings.DEFAULTS).putAll(features).build();

        BuiltinRegistries.NOISE_GENERATOR_SETTINGS.entrySet().forEach(settings -> {
            Map<StructureFeature<?>, StructureFeatureConfiguration> structures = settings.getValue().structureSettings().structureConfig();
            if(structures instanceof ImmutableMap)
            {
                Map<StructureFeature<?>, StructureFeatureConfiguration> temp = new HashMap<>(structures);
                temp.putAll(features);
                settings.getValue().structureSettings().structureConfig = temp;
            }
            else structures.putAll(features);
        });
    }

//    private static Method getCodecMethod;
    //called at EventHandler.onWorldLoad()
    public static void addDimensionalSpacing(ServerLevel level)
    {
//            //excluding 'terraforged' chunk generator
//            try
//            {
//                if(getCodecMethod == null)
//                    getCodecMethod = ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "func_230347_a_");
//                ResourceLocation chunkGenerator = Registry.CHUNK_GENERATOR.getKey(
//                        (Codec<? extends ChunkGenerator>) getCodecMethod.invoke(level.getChunkSource().getGenerator())
//                );
//                if(chunkGenerator != null && chunkGenerator.getNamespace().equals("terraforged")) return;
//            }
//            catch(Exception e) { }

        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();

        //excluding vanilla flat world generator
        if(chunkGenerator instanceof FlatLevelSource && level.dimension().equals(Level.OVERWORLD))
            return;

        Map<StructureFeature<?>, StructureFeatureConfiguration> temp =
                new HashMap<>(chunkGenerator.getSettings().structureConfig());
        for(RegistryObject<StructureFeature<?>> structure : RegistryHandler.STRUCTURES.getEntries())
        {
            temp.putIfAbsent(structure.get(), StructureSettings.DEFAULTS.get(structure.get()));
        }
        chunkGenerator.getSettings().structureConfig = temp;
    }

    public static void generateStructures(BiomeLoadingEvent event)
    {
        REGISTERED.forEach(csf -> {
            if(csf.canGenerate(event))
                event.getGeneration().getStructures().add(csf::getConfiguredFeature);
        });
    }
}
