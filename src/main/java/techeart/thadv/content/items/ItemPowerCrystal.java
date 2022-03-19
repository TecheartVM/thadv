package techeart.thadv.content.items;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import techeart.thadv.content.RegistryHandler;
import techeart.thadv.content.entities.EntityCarvedRune;
import techeart.thadv.content.gui.GuiSelectRune;
import techeart.thadv.content.misc.Rune;
import techeart.thadv.content.network.PacketHandler;
import techeart.thadv.content.network.packets.PacketPowerCrystalConfigure;
import techeart.thadv.utils.DispenserBehaviourHandler;
import techeart.thadv.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ItemPowerCrystal extends Item
{
    private static final String RUNE_TAG = "RuneHashCode";

    static
    {
        DispenserBehaviourHandler.registerBehaviour(RegistryHandler.POWER_CRYSTAL::get, (source, stack) -> {
            Direction dir = source.getBlockState().getValue(DispenserBlock.FACING);
            Rune rune = getSelectedRune(stack);
            if(carveBlock(source.getLevel(), source.getPos().relative(dir), dir.getOpposite(), rune))
            {
                if (stack.hurt(1, source.getLevel().random, (ServerPlayer)null))
                    stack.setCount(0);
            }
            return stack;
        });
    }

    public ItemPowerCrystal()
    {
        super(new Properties().tab(CreativeModeTab.TAB_TOOLS).durability(64).setNoRepair());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        //unlike 'useOn', this method gives priority to the offhand
        //this means that if you hold crystals in both hands and try to open they GUI it will open GUI for offhand crystal
        //so this check is changing this priority
        if(hand == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof ItemPowerCrystal)
            hand = InteractionHand.MAIN_HAND;

        if(level.isClientSide() && player.isShiftKeyDown())
            openSelectRuneGui(player, hand);
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext ctx)
    {
        Level level = ctx.getLevel();
        BlockPos clickedPos = ctx.getClickedPos();
        Direction face = ctx.getClickedFace();
        Player player = ctx.getPlayer();
        boolean playerShifting = player != null && player.isShiftKeyDown();

        if(playerShifting && clearRune(level, clickedPos, face))
        {
            level.playSound(null, clickedPos, SoundEvents.LARGE_AMETHYST_BUD_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        if(level.isClientSide()) return InteractionResult.SUCCESS;

        if(carveBlock(level, clickedPos, face, getSelectedRune(ctx.getItemInHand()), player))
        {
            Utils.hurtAndBreakItem(player, ctx.getHand());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    protected static boolean clearRune(Level level, BlockPos pos, Direction face)
    {
        AABB searchArea = new AABB(pos);
        List<EntityCarvedRune> entitiesInBlock = level.getEntitiesOfClass(EntityCarvedRune.class, searchArea);
        if(!entitiesInBlock.isEmpty())
        {
            boolean flag = false;
            for(EntityCarvedRune e : entitiesInBlock)
                if(e.clearFace(face))
                    flag = true;
            return flag;
        }
        return false;
    }

    public static boolean carveBlock(Level level, BlockPos pos, Direction face, Rune rune)
    {
        return carveBlock(level, pos, face, rune, null);
    }

    public static boolean carveBlock(Level level, BlockPos pos, Direction face, Rune rune, Player player)
    {
        if(!EntityCarvedRune.canFaceBeCarved(level, pos, face)) return false;
        if(level.isClientSide()) return true;

        AABB searchArea = new AABB(pos);
        List<EntityCarvedRune> entitiesInBlock = level.getEntitiesOfClass(EntityCarvedRune.class, searchArea);
        EntityCarvedRune entity;
        Direction dir = face.getAxis().isVertical() ? (player != null ? player.getDirection() : Direction.NORTH) : face;
        if(entitiesInBlock.isEmpty())
        {
            entity = new EntityCarvedRune(level, pos, dir);
            level.addFreshEntity(entity);
        }
        else
        {
            entity = entitiesInBlock.get(0);
            if(face == Direction.UP) entity.setDirection(dir);
        }

        if(face == Direction.DOWN) entity.setBottomRuneDir(dir.getOpposite());

        entity.setFace(face, rune);
        BlockState blockState = level.getBlockState(pos);
        playCarvingEffects((ServerLevel) level, pos, blockState, face);

        return true;
    }

    protected static void playCarvingEffects(ServerLevel level, BlockPos pos, BlockState blockState, Direction face)
    {
        level.playSound(null, pos, blockState.getSoundType().getHitSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
        Vector3f v = new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        Vector3f v1 = face.step();
        v1.mul(0.5f);
        v.add(v1);
        Random random = new Random();
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, blockState), v.x(), v.y(), v.z(), 5,
                random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D,
                0.15f
        );
    }

    protected static CompoundTag getOrAssignTag(ItemStack crystal)
    {
        if(!crystal.hasTag() || !crystal.getTag().contains(RUNE_TAG))
        {
            CompoundTag tag = new CompoundTag();
            tag.putInt(RUNE_TAG, Rune.identify(0).hashCode());
            crystal.setTag(tag);
        }
        return crystal.getTag();
    }

    @Nullable
    public static Rune getSelectedRune(ItemStack crystal)
    {
        if(!(crystal.getItem() instanceof ItemPowerCrystal)) return null;
        int runeHash = getOrAssignTag(crystal).getInt(RUNE_TAG);
        return Rune.identify(runeHash);
    }

    public static void configure(ItemStack crystal, Rune rune) { getOrAssignTag(crystal).putInt(RUNE_TAG, rune.hashCode()); }

    @OnlyIn(Dist.CLIENT)
    private static void openSelectRuneGui(Player player, InteractionHand hand)
    {
        Minecraft.getInstance().setScreen(new GuiSelectRune(player, hand));
    }

    @Nullable
    @Override
    public SoundEvent getEquipSound() { return SoundEvents.LARGE_AMETHYST_BUD_PLACE; }

    @OnlyIn(Dist.CLIENT)
    public static boolean onScroll(Player player, double scrollDelta)
    {
        if(player != null)
        {
            if(player.isShiftKeyDown())
            {
                for(InteractionHand hand : InteractionHand.values())
                {
                    ItemStack stack = player.getItemInHand(hand);
                    if(stack.getItem() instanceof ItemPowerCrystal)
                    {
                        Rune rune = ItemPowerCrystal.getSelectedRune(stack);
                        //TODO: possibly, some optimization
                        PacketHandler.sendToServer(
                                new PacketPowerCrystalConfigure(hand, Rune.cycle(rune, scrollDelta > 0))
                        );
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
