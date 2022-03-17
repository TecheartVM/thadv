package techeart.thadv.content;

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
import techeart.thadv.content.entities.EntityBossStoneGuardian;
import techeart.thadv.content.gui.GuiBossEventBar;
import techeart.thadv.content.gui.GuiHotbarRune;
import techeart.thadv.content.gui.GuiTooltipRune;
import techeart.thadv.content.network.PacketHandler;
import techeart.thadv.content.world.structures.StructuresHandler;
import techeart.thadv.utils.DispenserBehaviourHandler;
import techeart.thadv.utils.RenderHandler;

@Mod("thadv")
public class MainClass
{
    public static final String MODID = "thadv";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final GuiBossEventBar HUD_BOSS_HEALTH_OVERLAY = new GuiBossEventBar();
    public static final GuiHotbarRune HUD_HOTBAR_RUNE = new GuiHotbarRune();

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
        DispenserBehaviourHandler.register();

        event.enqueueWork(() -> {
            StructuresHandler.registerStructures();
        });
    }

    private void doClientStuff(final FMLClientSetupEvent event)
    {
        HUD_BOSS_HEALTH_OVERLAY.init();
        HUD_HOTBAR_RUNE.init();

        GuiTooltipRune.init();

        RenderHandler.registerEntityRenderers();
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
}
