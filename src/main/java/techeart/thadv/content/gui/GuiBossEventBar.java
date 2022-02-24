package techeart.thadv.content.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import techeart.thadv.content.MainClass;
import techeart.thadv.content.network.packets.PacketBossEventSync;
import techeart.thadv.utils.ColorUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class GuiBossEventBar
{
    private static final int NEXT_BAR_OFFSET = 5;

    private final Map<UUID, ModBossEventClient> events = new LinkedHashMap<>();

    public void init()
    {
        OverlayRegistry.registerOverlayAbove(
                ForgeIngameGui.BOSS_HEALTH_ELEMENT,
                MainClass.MODID + ": Boss Health",
                (gui, matrixStack, partialTicks, screenWidth, screenHeight) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if(!mc.options.hideGui)
                        render(mc, gui, matrixStack, partialTicks, screenWidth, screenHeight);
                }
        );
    }

    private void render(Minecraft mc, ForgeIngameGui gui, PoseStack matrixStack, float partialTicks, int screenWidth, int screenHeight)
    {
        if(events.isEmpty()) return;

        int guiWidth = mc.getWindow().getGuiScaledWidth();
        int barY = 12;
        for(ModBossEventClient e : events.values())
        {
            int barX = guiWidth / 2 - e.getBarWidth() / 2;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, e.getBarTexture());
            drawBar(gui, matrixStack, barX, barY, e);
            Component nameText = e.getName();
            int textWidth = mc.font.width(nameText);
            int shadowX = guiWidth / 2 - textWidth / 2;
            int shadowY = barY - 9;
            mc.font.drawShadow(matrixStack, nameText, (float)shadowX, (float)shadowY, 16777215);

            barY += mc.font.lineHeight + e.getBarHeight() + NEXT_BAR_OFFSET;
            if(barY >= mc.getWindow().getGuiScaledHeight() / 3) break;
        }
    }

    private void drawBar(ForgeIngameGui gui, PoseStack matrixStack, int x, int y, ModBossEventClient e)
    {
        int barW = e.getBarWidth();
        int barH = e.getBarHeight();
        //background
        gui.blit(matrixStack, x, y, 0, 0, barW, barH);
        //lerping bar
        float[] rgb = ColorUtils.getRGB(e.getColorSecondary());
        RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], 1.0F);
        int width = Mth.clamp((int)(e.getProgress() * barW), 0, barW);
        gui.blit(matrixStack, x, y, 0, barH, width, barH);
        //true value bar
        rgb = ColorUtils.getRGB(e.getColorPrimary());
        RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], 1.0F);
        width = Mth.clamp((int)(e.getTargetProgress() * barW), 0, barW);
        gui.blit(matrixStack, x, y, 0, barH + barH, width, barH);
    }

    public void reset() { events.clear(); }

    public void update(PacketBossEventSync pkt)
    {
        pkt.handle(new PacketBossEventSync.Handler()
        {
            @Override
            public void add(UUID eventId, Component nameText, float progress)
            {
                events.put(eventId, new ModBossEventClient(eventId, nameText, progress));
            }

            @Override
            public void remove(UUID eventId) { events.remove(eventId); }

            @Override
            public void setProgress(UUID eventId, float progress) { events.get(eventId).setProgress(progress); }

            @Override
            public void setColors(UUID eventId, int primary, int secondary) { events.get(eventId).setColors(primary, secondary); }

            @Override
            public void setName(UUID eventId, Component name) { events.get(eventId).setName(name); }

            @Override
            public void setTexture(UUID eventId, ResourceLocation location, int barWidth, int barHeight)
            {
                events.get(eventId).setBarTexture(location, barWidth, barHeight);
            }
        });
    }
}
