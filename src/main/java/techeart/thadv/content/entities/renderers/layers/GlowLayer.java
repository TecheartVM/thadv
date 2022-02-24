package techeart.thadv.content.entities.renderers.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class GlowLayer<T extends Entity & IAnimatable> extends GeoLayerRenderer<T>
{
    protected final ResourceLocation texture;

    public GlowLayer(IGeoRenderer<T> entityRendererIn, ResourceLocation texture)
    {
        super(entityRendererIn);
        this.texture = texture;
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        renderCopyModel((GeoModelProvider<T>) getEntityModel(), texture, matrixStack, buffer, packedLight, entity, partialTicks, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public RenderType getRenderType(ResourceLocation textureLocation)
    {
        return RenderType.eyes(texture);
    }
}
