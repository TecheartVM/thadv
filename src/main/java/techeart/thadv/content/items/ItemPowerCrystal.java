package techeart.thadv.content.items;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import techeart.thadv.content.entities.EntityCarvedRune;
import techeart.thadv.content.gui.GuiSelectRune;
import techeart.thadv.content.misc.Rune;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class ItemPowerCrystal extends Item
{
    public ItemPowerCrystal() { super(new Properties().tab(CreativeModeTab.TAB_TOOLS).durability(64).setNoRepair()); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        if(level.isClientSide() && player.isShiftKeyDown())
            Minecraft.getInstance().setScreen(new GuiSelectRune(player, player.getItemInHand(hand)));
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext ctx)
    {
        if(ctx.getLevel().isClientSide()) return InteractionResult.SUCCESS;

        BlockPos clickedPos = ctx.getClickedPos();
        boolean b = EntityCarvedRune.canFaceBeCarved(ctx.getLevel(), clickedPos, ctx.getClickedFace());
        if(b)
        {
            AABB searchArea = new AABB(clickedPos);
            List<EntityCarvedRune> entitiesInBlock = ctx.getLevel().getEntitiesOfClass(EntityCarvedRune.class, searchArea);
            EntityCarvedRune entity;
            if(entitiesInBlock.isEmpty())
            {
                entity = new EntityCarvedRune(ctx.getLevel(), clickedPos);
                ctx.getLevel().addFreshEntity(entity);
            }
            else entity = entitiesInBlock.get(0);

            entity.setFace(ctx.getClickedFace(), Rune.TRAVEL);
            BlockState blockState = ctx.getLevel().getBlockState(clickedPos);
            playCarvingEffects((ServerLevel) ctx.getLevel(), clickedPos, blockState, ctx.getClickedFace());

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void playCarvingEffects(ServerLevel level, BlockPos pos, BlockState blockState, Direction face)
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
}
