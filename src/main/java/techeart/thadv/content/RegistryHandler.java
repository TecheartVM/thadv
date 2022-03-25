package techeart.thadv.content;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.thadv.content.block.BlockCoreTrap;
import techeart.thadv.content.block.entity.BlockEntityCoreTrap;
import techeart.thadv.content.entity.entities.boss.EntityBossStoneGuardian;
import techeart.thadv.content.entity.entities.misc.EntityCarvedRune;
import techeart.thadv.content.entity.entities.misc.EntityEruptionSource;
import techeart.thadv.content.entity.entities.projectile.EntityLavaBall;
import techeart.thadv.content.items.ItemPowerCrystal;
import techeart.thadv.content.world.structure.CompleteStructureFeature;
import techeart.thadv.content.world.structure.StructuresHandler;
import techeart.thadv.content.world.structure.structures.overworld.StructureForge;

public class RegistryHandler
{
    public static class Items
    {
        public static final DeferredRegister<Item> ALL = DeferredRegister.create(ForgeRegistries.ITEMS, MainClass.MODID);
        //init
        //items
        public static final RegistryObject<Item> POWER_CRYSTAL = ALL.register("power_crystal", ItemPowerCrystal::new);

        //blocks
        public static final RegistryObject<Item> CORE_TRAP = ALL.register("block_core_trap", () ->
                new BlockItem(Blocks.CORE_TRAP.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
    }

    public static class Blocks
    {
        public static final DeferredRegister<Block> ALL = DeferredRegister.create(ForgeRegistries.BLOCKS, MainClass.MODID);
        //init
        //common blocks

        //blocks with entities
        public static final RegistryObject<Block> CORE_TRAP = ALL.register("block_core_trap", BlockCoreTrap::new);
    }

    public static class BlockEntities
    {
        public static final DeferredRegister<BlockEntityType<?>> ALL = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MainClass.MODID);
        //init
        public static final RegistryObject<BlockEntityType<BlockEntityCoreTrap>> CORE_TRAP =
                BlockEntities.ALL.register("core_trap", () -> BlockEntityCoreTrap.TYPE);
    }

    public static class Entities
    {
        public static final DeferredRegister<EntityType<?>> ALL = DeferredRegister.create(ForgeRegistries.ENTITIES, MainClass.MODID);
        //init
        //common entities

        //bosses
        public static final RegistryObject<EntityType<EntityBossStoneGuardian>> STONE_GUARDIAN =
                ALL.register("stone_guardian", () -> EntityBossStoneGuardian.TYPE);

        //projectiles
        public static final RegistryObject<EntityType<EntityLavaBall>> LAVA_BALL =
                ALL.register("lava_ball", () -> EntityLavaBall.TYPE);

        //misc
        public static final RegistryObject<EntityType<EntityEruptionSource>> LAVA_ERUPTION =
                ALL.register("lava_eruption", () -> EntityEruptionSource.TYPE);
        public static final RegistryObject<EntityType<EntityCarvedRune>> CARVED_RUNE =
                ALL.register("carved_rune", () -> EntityCarvedRune.TYPE);
    }

    public static class Structures
    {
        public static final DeferredRegister<StructureFeature<?>> ALL = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, MainClass.MODID);
        //init
        //overworld
        public static final CompleteStructureFeature<NoneFeatureConfiguration> ANCIENT_FORGE =
                StructuresHandler.register("ancient_forge", StructureForge::new, FeatureConfiguration.NONE, 100, 60);
    }

    public static void register(IEventBus bus)
    {
        Items.ALL.register(bus);
        Blocks.ALL.register(bus);
        BlockEntities.ALL.register(bus);
        Entities.ALL.register(bus);
        Structures.ALL.register(bus);
    }
}
