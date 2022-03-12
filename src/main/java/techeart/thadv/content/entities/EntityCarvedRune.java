package techeart.thadv.content.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import techeart.thadv.content.misc.Rune;
import techeart.thadv.content.network.PacketHandler;
import techeart.thadv.content.network.packets.PacketCarvedRuneFacesSync;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class EntityCarvedRune extends EntityDummy
{
    public static final EntityType<EntityCarvedRune> TYPE = EntityType.Builder.<EntityCarvedRune>of(
            EntityCarvedRune::new,
            MobCategory.MISC
    ).sized(1.0f, 1.0f).setShouldReceiveVelocityUpdates(false).noSummon()
            .clientTrackingRange(10).updateInterval(4).build("carved_rune");

    private static final Material[] CARVABLE_MATERIALS = { Material.STONE, Material.METAL, Material.HEAVY_METAL };

    private static final int VALIDATION_PERIOD = 10;

    private int validationTimer = 0;
    private BlockPos position;
    private boolean syncRequired = false;

    private final EnumMap<Direction, Rune> carvedFaces = new EnumMap<>(Direction.class);

    public EntityCarvedRune(EntityType<?> type, Level level) { super(type, level); }

    public EntityCarvedRune(Level level, BlockPos pos)
    {
        super(TYPE, level);
        setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public void setFace(@Nonnull Direction face, @Nonnull Rune rune)
    {
        carvedFaces.put(face, rune);
        if(level.isClientSide()) return;
        setSyncRequired();
    }

    public void setFaces(EnumMap<Direction, Rune> faces)
    {
        carvedFaces.clear();
        carvedFaces.putAll(faces);
        if(level.isClientSide() || carvedFaces.isEmpty()) return;
        setSyncRequired();
    }

    public boolean clearFace(@Nonnull Direction face)
    {
        boolean b = carvedFaces.remove(face) != null;
        if(!level.isClientSide() && carvedFaces.size() > 0 && b) setSyncRequired();
        return b;
    }

    //this method is not setting the 'syncRequired' value to true because
    //if after clearing all faces the 'setFace' method is not called
    //the entity will be discarded due to its uselessness
    public void clearFaces() { carvedFaces.clear(); }

    @Nullable
    public Rune getFace(Direction face) { return carvedFaces.get(face); }

    public Map<Direction, Rune> getCarvedFaces() { return Collections.unmodifiableMap(carvedFaces); }

    public Set<Direction> getFacesWithRunes(Rune... runes)
    {
        Set<Direction> res = EnumSet.noneOf(Direction.class);
        for(Map.Entry<Direction, Rune> e : carvedFaces.entrySet())
            for(Rune r : runes)
                if(e.getValue().equals(r))
                    res.add(e.getKey());
        return res;
    }

    public boolean hasRunes(@Nonnull Rune... runes)
    {
        if(runes.length == 0) return carvedFaces.size() > 0;
        for(Rune r : runes)
            if(!carvedFaces.containsValue(r))
                return false;
        return true;
    }

    //validating
    public static boolean canFaceBeCarved(Level level, BlockPos pos, Direction face)
    {
        BlockState state = level.getBlockState(pos);
        for(Material m : CARVABLE_MATERIALS)
            if(state.getMaterial().equals(m))
                return state.isFaceSturdy(level, pos, face);
        return false;
    }

    public void validateFaces()
    {
        if(level.isClientSide()) return;

        boolean changes = carvedFaces.keySet().removeIf(face -> !canFaceBeCarved(level, position, face));
        if(carvedFaces.isEmpty())
        {
            discard();
            onRemoved();
        }
        else if(changes) setSyncRequired();
    }

    @Override
    public void tick()
    {
        if(!isAlive()) return;
        if(!level.isClientSide())
        {
            if(validationTimer >= VALIDATION_PERIOD)
            {
                validateFaces();
                validationTimer = 0;
            }
            else validationTimer++;

            if(syncRequired) syncFacesChanges();
        }
    }

    protected void onRemoved() {  }

    //synchronization
    protected void syncFacesChanges()
    {
        if(level.isClientSide()) return;
        PacketHandler.sendToTracking(new PacketCarvedRuneFacesSync(carvedFaces, this), this);
        syncRequired = false;
    }

    public void setSyncRequired() { syncRequired = true; }
    public void setSyncRequired(boolean value) { syncRequired = value; }

    //positioning
    @Override
    public void setPos(double x, double y, double z)
    {
        position = new BlockPos(x, y, z);
        super.setPos(position.getX(), position.getY(), position.getZ());
        hasImpulse = true;
    }

    @Nonnull
    @Override
    protected AABB makeBoundingBox()
    {
        float hW = TYPE.getWidth() * 0.5f;
        Vec3 posCenter = new Vec3(position.getX() + hW, position.getY(), position.getZ() + hW);
        return TYPE.getDimensions().makeBoundingBox(posCenter);
    }

    public BlockPos getPos() { return position; }

    @Override
    public void move(MoverType moverType, Vec3 motionVec)
    {
        if(level.isClientSide() || !isAlive()) return;
        if(motionVec.lengthSqr() == 0.0D) return;
        discard();
        onRemoved();
    }

    @Override
    public void push(double x, double y, double z)
    {
        if(level.isClientSide() || !isAlive()) return;
        if(x*x + y*y + z*z == 0.0D) return;
        discard();
        onRemoved();
    }

    //save & load
    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        nbt.putInt("PosX", position.getX());
        nbt.putInt("PosY", position.getY());
        nbt.putInt("PosZ", position.getZ());
        nbt.putInt("RuneCount", carvedFaces.size());
        int count = 0;
        for(Map.Entry<Direction, Rune> e : carvedFaces.entrySet())
        {
            nbt.putInt("Face" + count, e.getKey().ordinal());
            e.getValue().save("Rune" + count, nbt);
            count++;
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        carvedFaces.clear();
        position = new BlockPos(nbt.getInt("PosX"), nbt.getInt("PosY"), nbt.getInt("PosZ"));
        int count = nbt.getInt("RuneCount");
        for(int i = 0; i < count; i++)
        {
            Direction face = Direction.values()[nbt.getInt("Face" + i)];
            Rune rune = Rune.load("Rune" + i, nbt);
            if(rune != null) carvedFaces.put(face, rune);
        }
        setSyncRequired();
    }
}
