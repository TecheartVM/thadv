package techeart.thadv.content.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import techeart.thadv.content.items.ItemPowerCrystal;
import techeart.thadv.content.misc.Rune;

@OnlyIn(Dist.CLIENT)
public class GuiTooltipRune
{
    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(new GuiTooltipRune());
        MinecraftForgeClient.registerTooltipComponentFactory(Tooltip.class, Render::new);
    }

    @SubscribeEvent
    public void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event)
    {
        if(event.isCanceled()) return;
        ItemStack hoveredStack = event.getStack();
        if(!(hoveredStack.getItem() instanceof ItemPowerCrystal)) return;
        Rune rune = ItemPowerCrystal.getSelectedRune(hoveredStack);
        if(rune == null) return;
        event.getTooltipElements().add(1, Either.right(new Tooltip(rune)));
    }

    protected static class Render implements ClientTooltipComponent
    {
        private final Rune rune;

        public Render(Tooltip tooltip) { this.rune = tooltip.getRune(); }

        @Override
        public int getHeight() { return 18; }

        @Override
        public int getWidth(Font p_169952_) { return 16; }

        @Override
        public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int zIndex, TextureManager textureManager)
        {
            if(rune == null) return;

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, rune.getResourceLocation());
            GuiComponent.blit(poseStack, x, y - 2, zIndex, 0, 0, 16, 16, 16, 16);
        }
    }

    protected static class Tooltip implements TooltipComponent
    {
        private final Rune rune;

        protected Tooltip(Rune rune) { this.rune = rune; }

        public Rune getRune() { return rune; }
    }
}
