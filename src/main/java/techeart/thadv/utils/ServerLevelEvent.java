package techeart.thadv.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import techeart.thadv.content.MainClass;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: testing & optimisation
public abstract class ServerLevelEvent
{
    private static final Map<ResourceKey<Level>, LevelEventData> DATA_MAP = new HashMap<>();

    private final ServerLevel level;

    public ServerLevelEvent(ServerLevel level) { this.level = level; }
    public ServerLevelEvent(ServerLevel level, CompoundTag nbt) { this.level = level; }

    public void run()
    {
        ResourceKey<Level> key = getLevel().dimension();
        if(!DATA_MAP.containsKey(key)) DATA_MAP.put(key, new LevelEventData());
        DATA_MAP.get(key).add(this);
    }
    public abstract boolean isFinished();
    protected void tick() { }
    protected abstract CompoundTag save(@Nonnull CompoundTag nbt);

    public ServerLevel getLevel() { return level; }

    public static class LevelEventData extends SavedData
    {
        private final List<ServerLevelEvent> registeredEvents = new ArrayList<>();

        @Override
        public CompoundTag save(@Nonnull CompoundTag nbt)
        {
            nbt.putInt("EventCount", registeredEvents.size());
            for(int i = 0; i < registeredEvents.size(); i++)
            {
                ServerLevelEvent e = registeredEvents.get(i);
                if(!e.isFinished())
                {
                    nbt.putString("EventClass" + i, e.getClass().getName());
                    e.save(nbt);
                }
            }
            return nbt;
        }

        public boolean add(ServerLevelEvent sc)
        {
            boolean res = registeredEvents.add(sc);
            if(res) setDirty();
            return res;
        }

        public void tick()
        {
            if(registeredEvents.isEmpty()) return;
            List<ServerLevelEvent> toRemove = new ArrayList<>();
            for(ServerLevelEvent sc : registeredEvents)
            {
                if(sc.isFinished()) toRemove.add(sc);
                else sc.tick();
            }
            if(!toRemove.isEmpty()) registeredEvents.removeAll(toRemove);
        }

        public static void tick(ServerLevel level) { if(DATA_MAP.containsKey(level.dimension())) DATA_MAP.get(level.dimension()).tick(); }

        public static void loadOrCreate(ServerLevel level)
        {
            DATA_MAP.put(
                    level.dimension(),
                    level.getDataStorage().computeIfAbsent(tag -> load(level, tag), LevelEventData::new, getFileId(level.dimensionType()))
            );
        }

        private static LevelEventData load(ServerLevel level, CompoundTag nbt)
        {
            LevelEventData loaded = new LevelEventData();
            for(int i = 0; i < nbt.getInt("EventCount"); i++)
            {
                String className = nbt.getString("EventClass" + i);
                if(className.equals("")) break;
                try
                {
                    Class<?> clazz = Class.forName(className);
                    Constructor<?> constructor = clazz.getConstructor(ServerLevel.class, CompoundTag.class);
                    loaded.add((ServerLevelEvent) constructor.newInstance(level, nbt));
                }
                catch(ClassNotFoundException e)
                {
                    MainClass.LOGGER.error("An error occurred while loading active server level events: Event class not found - '" + className + "'.");
                }
                catch(InstantiationException | IllegalAccessException e)
                {
                    MainClass.LOGGER.error("An error occurred while loading active server level event: Can't instantiate event class - '" + className + "'.");
                }
                catch(NoSuchMethodException | InvocationTargetException e)
                {
                    MainClass.LOGGER.error("An error occurred while loading active server level event: Can't receive required constructor of event class - '" + className + "'.");
                }
            }
            return loaded;
        }

        private static String getFileId(DimensionType dimension) { return MainClass.MODID + "_events_" + dimension.getFileSuffix(); }
    }
}
