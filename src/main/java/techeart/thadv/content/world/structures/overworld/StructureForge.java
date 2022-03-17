package techeart.thadv.content.world.structures.overworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import techeart.thadv.content.MainClass;

public class StructureForge extends StructureFeature<NoneFeatureConfiguration>
{
    private static final int SPAWN_Y = 30;
    //how many pieces outward from center can a recursive jigsaw structure spawn
    private static final int JIGSAW_LEVELS = 12;

    public StructureForge() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public GenerationStep.Decoration step() { return GenerationStep.Decoration.UNDERGROUND_STRUCTURES; }

//    @Override
//    protected boolean isFeatureChunk(ChunkGenerator generator, BiomeSource biomeSource, long seed,
//                                     WorldgenRandom random, ChunkPos chunkPos, Biome biome, ChunkPos chunkPos1,
//                                     NoneFeatureConfiguration config, LevelHeightAccessor levelHeight)
//    {
//        return true;
//    }

    @Override
    public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() { return Start::new; }

    public static class Start extends StructureStart<NoneFeatureConfiguration>
    {
        public Start(StructureFeature<NoneFeatureConfiguration> feature, ChunkPos chunkPos, int references, long seed)
        {
            super(feature, chunkPos, references, seed);
        }

        @Override
        public void generatePieces(RegistryAccess registry, ChunkGenerator generator,
                                   StructureManager structureManager, ChunkPos chunkPos, Biome biome,
                                   NoneFeatureConfiguration config, LevelHeightAccessor heightAccessor)
        {
            //chunk center
            BlockPos structurePos = new BlockPos(chunkPos.getBlockX(7), SPAWN_Y, chunkPos.getBlockZ(7));
            JigsawPlacement.addPieces(
                    registry,
                    new JigsawConfiguration(
                            () -> registry.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY)
                                    .get(MainClass.RLof("overworld/forge/start")),
                            JIGSAW_LEVELS
                    ),
                    PoolElementStructurePiece::new,
                    generator,
                    structureManager,
                    structurePos,
                    this,
                    this.random,
                    false, //false means that pieces will not intersect (I guess)
                    false, //false means that structure will be generated  at given blockPos's Y level
                                    //instead of the world surface
                    heightAccessor
            );

            //centering the structure
            Vec3i structureCenter = this.pieces.get(0).getBoundingBox().getCenter();
            int xOffset = structurePos.getX() - structureCenter.getX();
            int zOffset = structurePos.getZ() - structureCenter.getZ();
            this.pieces.forEach(p -> p.move(xOffset, 0, zOffset));

            this.getBoundingBox(); //setting structure's bounding box
        }
    }
}
