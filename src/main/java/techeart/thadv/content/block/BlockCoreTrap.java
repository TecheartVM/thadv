package techeart.thadv.content.block;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import techeart.thadv.content.block.entity.BlockEntityCoreTrap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCoreTrap extends BaseEntityBlock
{
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty CAMOUFLAGE = BooleanProperty.create("camouflage");

    public BlockCoreTrap()
    {
        super(
                BlockBehaviour.Properties.of(Material.METAL)
                        .requiresCorrectToolForDrops()
                        .destroyTime(20.0f)
                        .explosionResistance(120.0f)
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .noOcclusion()  //removing 'xray' effect
                        .sound(SoundType.COPPER)
        );
        registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(CAMOUFLAGE, false));
    }

    protected boolean setCamouflage(@Nonnull BlockEntityCoreTrap entity, Player player, InteractionHand hand)
    {
        Level level = entity.getLevel();
        if(level == null) return false;
        BlockPos pos = entity.getBlockPos();
        BlockState state = entity.getBlockState();

        ItemStack stack = player.getItemInHand(hand);
        ItemStack stackCopy = stack.copy();
        stackCopy.setCount(1);
        if(entity.setCamouflage(stackCopy))
        {
            if(!level.isClientSide())
            {
                if(!player.isCreative()) stack.shrink(1);
                level.setBlockAndUpdate(pos, state.setValue(CAMOUFLAGE, true));
                BlockState camouflage = entity.getCamouflageState();
                if(camouflage != null)
                    level.playSound(null, pos, camouflage.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return true;
        }
        return false;
    }

    protected boolean removeCamouflage(BlockEntityCoreTrap entity, Player player)
    {
        Level level = entity.getLevel();
        if(level == null) return false;

        BlockPos pos = entity.getBlockPos();
        BlockState state = entity.getBlockState();

        BlockState camouflage = entity.getCamouflageState();
        ItemStack drop = entity.breakCamouflage();
        if(!drop.isEmpty())
        {
            if(camouflage != null)
                spawnDestroyParticles(level, player, pos, camouflage);
            if(!level.isClientSide())
            {
                level.setBlockAndUpdate(pos, state.setValue(CAMOUFLAGE, false));
                if(!player.isCreative())
                {
                    Vec3 dropPos = new Vec3(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
                    dropPos = dropPos.add(player.position().subtract(dropPos).normalize().scale(0.7f));
                    level.addFreshEntity(new ItemEntity(level, dropPos.x(), dropPos.y(), dropPos.z(), drop));
                }

            }
            else level.getLightEngine().checkBlock(pos);
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        BlockEntityCoreTrap entity = getEntity(level, pos);
        if(entity != null)
        {
            setCamouflage(entity, player, hand);
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hitResult);
    }

    @Override
    public boolean removedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid)
    {
        if(!(player.isCreative() && !player.isShiftKeyDown()))
        {
            BlockEntityCoreTrap entity = getEntity(level, pos);
            if(entity != null && removeCamouflage(entity, player))
                return false; //skipping default block destroying procedure so no loot table drop will be spawned
        }
        return super.removedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos)
    {
        BlockState camouflage = getCamouflageState(state, level, pos);
        if(camouflage != null) state = camouflage;
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType)
    {
        return createTickerHelper(entityType, BlockEntityCoreTrap.TYPE, BlockEntityCoreTrap::tick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(LIT).add(CAMOUFLAGE); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BlockEntityCoreTrap(pos, state); }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state)
    {
        BlockState camouflage = getCamouflageState(state, level, pos);
        if(camouflage != null) state = camouflage;
        super.spawnDestroyParticles(level, player, pos, state);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity)
    {
        BlockState camouflage = getCamouflageState(state, level, pos);
        if(camouflage != null) return camouflage.getSoundType();
        return super.getSoundType(state, level, pos, entity);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos)
    {
        if(state.hasProperty(LIT) && state.getValue(LIT)) return 15;
        BlockState camouflage = getCamouflageState(state, level, pos);
        if(camouflage == null) return 0;
        return super.getLightEmission(camouflage, level, pos);
    }

    @Nullable
    public static BlockEntityCoreTrap getEntity(Level level, BlockPos pos)
    {
        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof BlockEntityCoreTrap coreTrap) return coreTrap;
        return null;
    }

    @Nullable
    protected BlockState getCamouflageState(BlockState trapState, BlockGetter level, BlockPos pos)
    {
        if(trapState.hasProperty(CAMOUFLAGE) && trapState.getValue(CAMOUFLAGE))
            if(level.getBlockEntity(pos) instanceof BlockEntityCoreTrap coreTrap)
                return coreTrap.getCamouflageState();
        return null;
    }
}
