package techeart.thadv.content.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import techeart.thadv.content.misc.Rune;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiSelectRune extends Screen
{
    private static final int LIST_WIDTH = 192;
    private static final int LIST_HEIGHT = 256;
    private static final int TILE_SIZE = 34;
    private static final int TILE_INTERVAL = 4;
    private static final int ROW_INTERVAL = 16;
    private static final int PADDING_TOP = 48;
    private static final int PADDING_BOTTOM = 32;
    private static final int TILES_IN_ROW = 4;

    private static final int ROW_WIDTH = TILE_SIZE * TILES_IN_ROW + TILE_INTERVAL * (TILES_IN_ROW - 1);
    private static final int LIST_HEIGHT_REAL = LIST_HEIGHT - PADDING_TOP - PADDING_BOTTOM;

    private final Player player;
    private final ItemStack item;
    private final List<Rune> displayableRunes;
    private final List<RuneButton> runeButtons = new ArrayList<>();

    public GuiSelectRune(Player player, ItemStack item)
    {
        super(NarratorChatListener.NO_TITLE);
        this.player = player;
        this.item = item;
        displayableRunes = new ArrayList<>(Rune.getKnownRunes(player));
    }

    @Override
    protected void init()
    {
        final int xInitial = (width - ROW_WIDTH) / 2;
        int x = xInitial;
        int y = (height - LIST_HEIGHT) / 2 + PADDING_TOP;

        for(int i = 0; i < displayableRunes.size(); i++)
        {
            runeButtons.add(
                    this.addRenderableWidget(
                            new RuneButton(x, y, TILE_SIZE, TILE_SIZE + font.lineHeight, font, displayableRunes.get(i))
                    )
            );
            if((i + 1) % TILES_IN_ROW == 0)
            {
                x = xInitial;
                y += TILE_SIZE + ROW_INTERVAL;
            }
            else x += TILE_SIZE + TILE_INTERVAL;
        }
    }

    @Override
    public void render(PoseStack poseStack, int p_96563_, int p_96564_, float p_96565_)
    {
        renderBackground(poseStack);
        setFocused(null);
        super.render(poseStack, p_96563_, p_96564_, p_96565_);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static class RuneButton extends Button
    {
        private final Font font;
        private final Rune rune;

        public RuneButton(int x, int y, int w, int h, Font font, Rune rune)
        {
            super(x, y, w, h, new TranslatableComponent(rune.getName()), button -> {

            });
            this.font = font;
            this.rune = rune;
        }

        @Override
        public void renderButton(PoseStack poseStack, int p_93747_, int p_93748_, float p_93749_)
        {
            //selection
            if(isHovered()) fill(poseStack, x, y-2, x+width, y+height, 0x61d0d0d0);
            //rune
            RenderSystem.setShaderTexture(0, rune.getResourceLocation());
            blit(poseStack, x+1, y, this.getBlitOffset(), 0.0f, 0.0f, width-2, width-2, width-2, width-2);
            //rune name
            String text = rune.getName();
            font.draw(poseStack, text, x+1 + (width - font.width(text)) * 0.5f, y + width-2, 0x00ffff);
        }

        @Override
        public void playDownSound(SoundManager sm)
        {
            sm.play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_CLUSTER_PLACE, 1.0F));
        }
    }
}
