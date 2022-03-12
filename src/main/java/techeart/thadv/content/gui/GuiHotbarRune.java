package techeart.thadv.content.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import techeart.thadv.content.MainClass;
import techeart.thadv.content.items.ItemPowerCrystal;
import techeart.thadv.content.misc.Rune;

@OnlyIn(Dist.CLIENT)
public class GuiHotbarRune
{
    //from Gui.class
    protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");

    public void init()
    {
        OverlayRegistry.registerOverlayAbove(
                ForgeIngameGui.HOTBAR_ELEMENT,
                MainClass.MODID + ": Hotbar Rune",
                (gui, poseStack, partialTicks, screenWidth, screenHeight) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if(!mc.options.hideGui)
                        render(mc, gui, poseStack, screenWidth, screenHeight);
                }
        );
    }

    protected void render(Minecraft mc, ForgeIngameGui gui, PoseStack poseStack, int screenWidth, int screenHeight)
    {
        Player player = mc.player;
        if(player == null) return;

        for(InteractionHand hand : InteractionHand.values())
        {
            ItemStack stack = player.getItemInHand(hand);
            if(stack.getItem() instanceof ItemPowerCrystal)
            {
                Rune rune = ItemPowerCrystal.getSelectedRune(stack);
                if(rune == null) continue;
                renderRune(rune, gui, poseStack, screenWidth, screenHeight, player.getMainArm() == HumanoidArm.RIGHT);
                return;
            }
        }
    }

    protected void renderRune(Rune rune, ForgeIngameGui gui, PoseStack poseStack, int screenWidth, int screenHeight, boolean rightMainHand)
    {
        int x = rightMainHand ? (screenWidth/2 + 91) : (screenWidth/2 - 91 - 29); //from Gui.class
        int y = screenHeight - 23;  //from Gui.class
        int frameTextureOffsetX = rightMainHand ? 53 : 24; //from Gui.class
        int runeOffsetX = rightMainHand ? 10 : 3;
        //render frame
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        gui.blit(poseStack, x, y, frameTextureOffsetX, 22, 29, 24); //from Gui.class
        //render rune
        RenderSystem.setShaderTexture(0, rune.getResourceLocation());
        GuiComponent.blit(poseStack, x + runeOffsetX, y + 4, 0, 0, 16, 16, 16, 16);
    }
}
