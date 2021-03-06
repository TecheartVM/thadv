package techeart.thadv.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techeart.thadv.content.entity.entities.basic.EntityMultipartPart;
import techeart.thadv.content.MainClass;
import techeart.thadv.content.entity.entities.boss.EntityBossStoneGuardian;
import techeart.thadv.content.items.ItemPowerCrystal;
import techeart.thadv.content.world.structure.StructuresHandler;

@Mod.EventBusSubscriber
public class EventHandler
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
        if(event.getWorld().isClientSide()) MainClass.HUD_BOSS_HEALTH_OVERLAY.reset();
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if(event.side.isServer()) ServerLevelEvent.LevelEventData.tick((ServerLevel) event.world);
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event)
    {
        if(event.getWorld() instanceof ServerLevel level)
        {
            //load server level events
            ServerLevelEvent.LevelEventData.loadOrCreate(level);
            //processing custom structures
            StructuresHandler.addDimensionalSpacing(level);
        }
    }

    @SubscribeEvent
    public static void onBiomeLoad(BiomeLoadingEvent event)
    {
        StructuresHandler.generateStructures(event);
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(event.getScrollDelta() == 0) return;
        Player player = mc.player;
        if(ItemPowerCrystal.onScroll(player, event.getScrollDelta()))
            event.setCanceled(true);
    }
}
