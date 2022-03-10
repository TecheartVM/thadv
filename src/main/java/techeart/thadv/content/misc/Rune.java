package techeart.thadv.content.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import techeart.thadv.content.MainClass;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Rune
{
    private static final Set<Rune> REGISTERED = new HashSet<>();

    public static final Rune TRAVEL = register("travel");
    public static final Rune FIRE = register("fire");
    public static final Rune WATER = register("water");
    public static final Rune EARTH = register("earth");
    public static final Rune AIR = register("air");

    public static Rune register(String name, ResourceLocation rl)
    {
        Rune res = new Rune(REGISTERED.size(), name, rl);
        if(!REGISTERED.add(res))
        {
            MainClass.LOGGER.error("Duplicate ID is not allowed: " + name);
            return null;
        }
        return res;
    }

    private static Rune register(String name) { return register(name, MainClass.RLof("textures/runes/" + name + ".png")); }

    public static Set<Rune> getAllRunes() { return Collections.unmodifiableSet(REGISTERED); }

    public static Set<Rune> getKnownRunes(Player player) { return getAllRunes(); }

    //====================== Class itself ======================

    private final int id;
    private final String name;
    private final ResourceLocation rl;

    private Rune(int id, String name, ResourceLocation rl)
    {
        this.id = id;
        this.name = name;
        this.rl = rl;
    }

    public CompoundTag save(CompoundTag nbt)
    {
        nbt.putInt("ID", id);
        return nbt;
    }

    @Nullable
    public static Rune load(CompoundTag nbt)
    {
        int i = nbt.getInt("ID");
        for(Rune r : REGISTERED) if(r.id == i) return r;
        return null;
    }

    @Nullable
    public static Rune identify(int id)
    {
        for(Rune r : REGISTERED) if(r.id == id) return r;
        return null;
    }

    public String getName() { return name; }

    public ResourceLocation getResourceLocation() { return rl; }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Rune rune = (Rune) o;
        return id == rune.id;
    }

    @Override
    public int hashCode() { return id; }
}
