package techeart.thadv.content.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
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
import java.util.function.Function;

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
    private boolean syncRequired = false;

    private Direction direction = Direction.NORTH;
    private Direction directionBottom = Direction.NORTH;
    private final EnumMap<Direction, Rune> carvedFaces = new EnumMap<>(Direction.class);

    public EntityCarvedRune(EntityType<?> type, Level level) { super(type, level); }

    public EntityCarvedRune(Level level, BlockPos pos, Direction dir)
    {
        super(TYPE, level);
        setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        setDirection(dir);
    }

    //faces manipulation
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

        boolean changes = carvedFaces.keySet().removeIf(face -> !canFaceBeCarved(level, blockPosition(), face));
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
        super.setPos(Math.floor(x) + 0.5D, Math.floor(y), Math.floor(z) + 0.5D);
    }

    @Nonnull
    @Override
    protected AABB makeBoundingBox()
    {
        Vec3 posCenter = new Vec3(blockPosition().getX() + 0.5D, blockPosition().getY(), blockPosition().getZ() + 0.5D);
        return TYPE.getDimensions().makeBoundingBox(posCenter);
    }

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

    @Override
    public float rotate(@Nonnull Rotation rotation)
    {
        float f = Mth.wrapDegrees(this.getDirection().toYRot());
        switch(rotation)
        {
            case CLOCKWISE_180:
                rotateFaces(Direction::getOpposite);
                return f + 180.0F;
            case COUNTERCLOCKWISE_90:
                rotateFaces(Direction::getCounterClockWise);
                return f + 270.0F;
            case CLOCKWISE_90:
                rotateFaces(Direction::getClockWise);
                return f + 90.0F;
            default:
                return f;
        }
    }

    @Override
    public float mirror(@Nonnull Mirror mirror)
    {
        float f = Mth.wrapDegrees(this.getDirection().toYRot());
        switch(mirror)
        {
            case LEFT_RIGHT:
            {
                Rune left = getFace(getDirection().getCounterClockWise());
                Rune right = getFace(getDirection().getClockWise());
                if(right != null) setFace(getDirection().getCounterClockWise(), right);
                if(left != null) setFace(getDirection().getClockWise(), left);
                return -f;
            }
            case FRONT_BACK:
            {
                Rune front = getFace(getDirection());
                Rune back = getFace(getDirection().getOpposite());
                if(back != null) setFace(getDirection(), back);
                setDirection(getDirection().getOpposite());
                setBottomRuneDir(getBottomRuneDir().getOpposite());
                if(front != null) setFace(getDirection(), front);
                return 180.0F - f;
            }
            default:
                return f;
        }
    }

    public void setDirection(@Nullable Direction dir)
    {
        direction = (dir == null || dir.getAxis().isVertical()) ? Direction.NORTH : dir;
        setYRot(direction.toYRot());
        setSyncRequired();
    }

    public void setBottomRuneDir(@Nullable Direction dir)
    {
        directionBottom = (dir == null || dir.getAxis().isVertical()) ? Direction.NORTH : dir;
        setSyncRequired();
    }

    @Override
    @Nonnull
    public Direction getDirection() { return direction; }

    public Direction getBottomRuneDir() { return directionBottom; }

    protected void rotateFaces(@Nonnull Function<Direction, Direction> rotMethod)
    {
        EnumMap<Direction, Rune> result = new EnumMap<>(Direction.class);
        carvedFaces.forEach((face, rune) -> {
            if(face.getAxis().isVertical()) result.put(face, rune);
            else result.put(rotMethod.apply(face), rune);
        });
        setFaces(result);
        setDirection(rotMethod.apply(getDirection()));
        setBottomRuneDir(rotMethod.apply(getBottomRuneDir()));
    }

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dimensions) { return TYPE.getHeight() * 0.5f; }

    //save & load
    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        nbt.putInt("Direction", getDirection().ordinal());
        if(getFace(Direction.DOWN) != null) nbt.putInt("DirectionBottom", getBottomRuneDir().ordinal());
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
        setDirection(Direction.values()[nbt.getInt("Direction")]);
        if(nbt.contains("DirectionBottom"))
            setBottomRuneDir(Direction.values()[nbt.getInt("DirectionBottom")]);
        carvedFaces.clear();
        int count = nbt.getInt("RuneCount");
        for(int i = 0; i < count; i++)
        {
            Direction face = Direction.values()[nbt.getInt("Face" + i)];
            Rune rune = Rune.load("Rune" + i, nbt);
            if(rune != null) carvedFaces.put(face, rune);
        }
    }
}
