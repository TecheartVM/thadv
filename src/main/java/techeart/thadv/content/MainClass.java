package techeart.thadv.content;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import software.bernie.geckolib3.GeckoLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import techeart.thadv.api.EntityMultipartPart;
import techeart.thadv.content.entities.EntityBossStoneGuardian;
import techeart.thadv.content.entities.renderers.RendererEruptionSource;
import techeart.thadv.content.entities.renderers.RendererLavaBall;
import techeart.thadv.content.entities.renderers.RendererStoneGuardian;
import techeart.thadv.content.gui.GuiBossEventBar;
import techeart.thadv.content.network.PacketHandler;
import techeart.thadv.utils.ServerLevelEvent;

@Mod("thadv")
public class MainClass
{
    public static final String MODID = "thadv";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final GuiBossEventBar HUD_BOSS_HEALTH_OVERLAY = new GuiBossEventBar();

    public MainClass()
    {
        GeckoLib.initialize();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        RegistryHandler.register(FMLJavaModLoadingContext.get().getModEventBus());

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation RLof(String objName) { return new ResourceLocation(MODID, objName); }

    public static String pathOf(String objName) { return RLof(objName).toString(); }

    private void setup(final FMLCommonSetupEvent event)
    {
        PacketHandler.register();
    }

    private void doClientStuff(final FMLClientSetupEvent event)
    {
        HUD_BOSS_HEALTH_OVERLAY.init();

        EntityRenderers.register(RegistryHandler.STONE_GUARDIAN.get(), RendererStoneGuardian::new);
        EntityRenderers.register(RegistryHandler.LAVA_BALL.get(), RendererLavaBall::new);
        EntityRenderers.register(RegistryHandler.LAVA_ERUPTION.get(), RendererEruptionSource::new);
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event)
        {
            event.put(RegistryHandler.STONE_GUARDIAN.get(), EntityBossStoneGuardian.createAttributes().build());
        }
    }

    @Mod.EventBusSubscriber
    public static class EventHandler
    {
        @SubscribeEvent
        public static void onProjectileImpact(ProjectileImpactEvent event)
        {
            if(event.getProjectile() instanceof ThrownPotion potion)
            {
                HitResult hit = event.getRayTraceResult();
                if(hit.getType() == HitResult.Type.ENTITY)
                {
                    Entity e = ((EntityHitResult)hit).getEntity();
                    if(e instanceof EntityMultipartPart)
                    {
                        if(((EntityMultipartPart) e).getParent() instanceof EntityBossStoneGuardian guardian)
                        {
                            if(guardian.hurtByPotion(potion))
                            {
                                event.setCanceled(true);
                                potion.remove(Entity.RemovalReason.DISCARDED);
                            }
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onWorldUnload(WorldEvent.Unload event)
        {
            //TODO: some testing required
            if(event.getWorld().isClientSide()) HUD_BOSS_HEALTH_OVERLAY.reset();
        }

        @SubscribeEvent
        public static void onWorldTick(TickEvent.WorldTickEvent event)
        {
            if(event.side.isServer()) ServerLevelEvent.LevelEventData.tick((ServerLevel) event.world);
        }

        @SubscribeEvent
        public static void onWorldLoad(WorldEvent.Load event)
        {
            //load server level events
            if(!event.getWorld().isClientSide())
            {
                ServerLevelEvent.LevelEventData.loadOrCreate((ServerLevel) event.getWorld());
            }
        }
    }
}
