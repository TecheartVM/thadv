package techeart.thadv.content.block.entity;

import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import techeart.thadv.content.RegistryHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlockEntityCoreTrap extends BlockEntity
{
    public static final BlockEntityType<BlockEntityCoreTrap> TYPE =
            BlockEntityType.Builder.of(BlockEntityCoreTrap::new, RegistryHandler.Blocks.CORE_TRAP.get()).build(null);

    private static final Material[] CAMOUFLAGE_MATERIALS = {Material.STONE, Material.METAL, Material.HEAVY_METAL };

    private ItemStack camouflage = null;

    private BlockItem cachedCamouflageItem = null;
    private ResourceLocation cachedCamouflageRL = null;

    public BlockEntityCoreTrap(BlockPos blockPos, BlockState blockState)
    {
        super(TYPE, blockPos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityCoreTrap entity)
    {
//        if(level.isClientSide()) return;

    }

    public static boolean isSuitableCamouflage(@Nonnull Block block)
    {
        BlockState state = block.defaultBlockState();
        if(!state.canOcclude()) return false;
        if(block.hasDynamicShape()) return false;
        if(block.getRenderShape(state) != RenderShape.MODEL) return false;
        if(block instanceof EntityBlock) return false;
        return Arrays.asList(CAMOUFLAGE_MATERIALS).contains(state.getMaterial());
    }

    public boolean hasCamouflage() { return !getCamouflage().isEmpty(); }

    public boolean setCamouflage(@Nonnull BlockState camouflage)
    {
        return setCamouflage(new ItemStack(new BlockItem(camouflage.getBlock(), new Item.Properties())));
    }

    public boolean setCamouflage(ItemStack camouflage)
    {
        if(camouflage != null)
        {
            if(hasCamouflage()) return false;
            if(camouflage.isEmpty()) return false;
            if(camouflage.getItem() instanceof BlockItem blockItem)
            {
                if(!isSuitableCamouflage(blockItem.getBlock())) return false;
            }
            else return false;
        }

        this.camouflage = camouflage;
        cachedCamouflageItem = null;
        cachedCamouflageRL = null;
        return true;
    }

    @Nonnull
    public ItemStack breakCamouflage()
    {
        if(!hasCamouflage()) return ItemStack.EMPTY;
        ItemStack drop = getCamouflage();
        setCamouflage((ItemStack) null);
        return drop;
    }

    @Nonnull
    public ItemStack getCamouflage() { return camouflage == null ? ItemStack.EMPTY : camouflage; }

    @Nullable
    public BlockItem getCamouflageItem()
    {
        if(cachedCamouflageItem != null) return cachedCamouflageItem;
        cachedCamouflageItem = getCamouflage().isEmpty() ? null : (BlockItem) getCamouflage().getItem();
        return cachedCamouflageItem;
    }

    @Nullable
    public BlockState getCamouflageState()
    {
        BlockItem item = getCamouflageItem();
        if(item == null) return null;
        return item.getBlock().defaultBlockState();
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public ResourceLocation getCamouflageTexture(BlockRenderDispatcher renderer)
    {
        if(cachedCamouflageRL != null) return cachedCamouflageRL;

        BlockState state = getCamouflageState();
        if(state == null) return null;
        BakedModel model = renderer.getBlockModel(state);
        List<BakedQuad> unculledQuads = model.getQuads(state, null, new Random(), EmptyModelData.INSTANCE);
        if(unculledQuads.isEmpty())
        {
            unculledQuads = model.getQuads(state, Direction.UP, new Random(), EmptyModelData.INSTANCE);
            if(unculledQuads.isEmpty()) return null;
        }
        BakedQuad quad = unculledQuads.get(0);
        cachedCamouflageRL = quad.getSprite().getName();
        return cachedCamouflageRL;
    }

    @Override
    public CompoundTag save(CompoundTag nbt)
    {
        if(hasCamouflage())
            nbt.put("Camouflage", getCamouflage().save(new CompoundTag()));
        return super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        if(nbt.contains("Camouflage"))
            setCamouflage(ItemStack.of(nbt.getCompound("Camouflage")));
        super.load(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() { return save(new CompoundTag()); }

    @Override
    public void handleUpdateTag(CompoundTag tag) { load(tag); }
}
