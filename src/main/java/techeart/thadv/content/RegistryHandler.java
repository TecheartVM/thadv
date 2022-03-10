package techeart.thadv.content;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.thadv.content.entities.EntityBossStoneGuardian;
import techeart.thadv.content.entities.EntityCarvedRune;
import techeart.thadv.content.entities.EntityEruptionSource;
import techeart.thadv.content.entities.EntityLavaBall;
import techeart.thadv.content.items.ItemPowerCrystal;

public class RegistryHandler
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MainClass.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MainClass.MODID);

    public static final RegistryObject<Item> POWER_CRYSTAL = ITEMS.register("power_crystal", ItemPowerCrystal::new);

    public static final RegistryObject<EntityType<EntityBossStoneGuardian>> STONE_GUARDIAN = ENTITIES.register("stone_guardian", () -> EntityBossStoneGuardian.TYPE);
    public static final RegistryObject<EntityType<EntityLavaBall>> LAVA_BALL = ENTITIES.register("lava_ball", () -> EntityLavaBall.TYPE);
    public static final RegistryObject<EntityType<EntityEruptionSource>> LAVA_ERUPTION = ENTITIES.register("lava_eruption", () -> EntityEruptionSource.TYPE);
    public static final RegistryObject<EntityType<EntityCarvedRune>> CARVED_RUNE = ENTITIES.register("carved_rune", () -> EntityCarvedRune.TYPE);

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
        ENTITIES.register(bus);
    }
}
