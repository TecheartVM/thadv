package techeart.thadv.content.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import techeart.thadv.content.MainClass;

import java.util.UUID;

public class ModBossEvent
{
    private final UUID id;
    protected Component name;
    protected float progress;
    //style
    public static final ResourceLocation DEFAULT_BAR_TEXTURE = MainClass.RLof("textures/gui/default_boss_bar.png");;
    public static final int DEFAULT_BAR_WIDTH = 182;
    public static final int DEFAULT_BAR_HEIGHT = 5;
    public static final int DEFAULT_COLOR_PRIMARY = 0xB82D15;
    public static final int DEFAULT_COLOR_SECONDARY = 0xBB9753;

    protected ResourceLocation barTexture;
    protected int barWidth;
    protected int barHeight;
    protected int colorPrimary;
    protected int colorSecondary;

    public ModBossEvent(UUID id, Component name)
    {
        this.id = id;
        this.name = name;
        progress = 1.0F;

        barTexture = DEFAULT_BAR_TEXTURE;
        barWidth = DEFAULT_BAR_WIDTH;
        barHeight = DEFAULT_BAR_HEIGHT;
        colorPrimary = DEFAULT_COLOR_PRIMARY;
        colorSecondary = DEFAULT_COLOR_SECONDARY;
    }

    public UUID getId() {
        return this.id;
    }

    public Component getName() {
        return this.name;
    }
    public void setName(Component name) {
        this.name = name;
    }

    public float getProgress() {
        return this.progress;
    }
    public void setProgress(float value) {
        this.progress = value;
    }

    //style
    public int getColorPrimary() { return colorPrimary; }
    public int getColorSecondary() { return colorSecondary; }
    public void setColors(int primary, int secondary)
    {
        colorPrimary = primary;
        colorSecondary = secondary;
    }

    public ResourceLocation getBarTexture() { return barTexture; }
    public int getBarWidth() { return barWidth; }
    public int getBarHeight() { return barHeight; }
    public void setBarTexture(ResourceLocation texture, int barWidth, int barHeight)
    {
        barTexture = texture;
        this.barWidth = barWidth;
        this.barHeight = barHeight;
    }
}
