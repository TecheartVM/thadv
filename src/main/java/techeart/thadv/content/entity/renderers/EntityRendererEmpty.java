package techeart.thadv.content.entity.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class EntityRendererEmpty<E extends Entity> extends EntityRenderer<E>
{
    public EntityRendererEmpty(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public void render(E entity, float p_114486_, float p_114487_, PoseStack poseStack, MultiBufferSource multiBufferSource, int p_114490_) {
        super.render(entity, p_114486_, p_114487_, poseStack, multiBufferSource, p_114490_);
    }

    @Override
    public ResourceLocation getTextureLocation(E entity) { return null; }
}
