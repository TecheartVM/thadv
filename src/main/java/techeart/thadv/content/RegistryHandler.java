package techeart.thadv.content;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.thadv.content.entities.EntityBossStoneGuardian;
import techeart.thadv.content.entities.EntityEruptionSource;
import techeart.thadv.content.entities.EntityLavaBall;

public class RegistryHandler
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MainClass.MODID);

    public static final RegistryObject<EntityType<EntityBossStoneGuardian>> STONE_GUARDIAN = ENTITIES.register("stone_guardian", () -> EntityBossStoneGuardian.TYPE);
    public static final RegistryObject<EntityType<EntityLavaBall>> LAVA_BALL = ENTITIES.register("lava_ball", () -> EntityLavaBall.TYPE);
    public static final RegistryObject<EntityType<EntityEruptionSource>> LAVA_ERUPTION = ENTITIES.register("lava_eruption", () -> EntityEruptionSource.TYPE);

    public static void register(IEventBus bus)
    {
        ENTITIES.register(bus);
    }
}
