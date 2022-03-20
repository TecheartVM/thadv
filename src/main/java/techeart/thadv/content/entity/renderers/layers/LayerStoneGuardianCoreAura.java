package techeart.thadv.content.entity.renderers.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import techeart.thadv.content.entity.entities.boss.EntityBossStoneGuardian;
import techeart.thadv.content.entity.models.ModelStoneGuardian;

@OnlyIn(Dist.CLIENT)
public class LayerStoneGuardianCoreAura extends GlowLayer<EntityBossStoneGuardian>
{
    private static final float DEFAULT_ALPHA = 0.3f;
    private static final float ALPHA_INCREMENT = 0.001f;

    private float alpha;

    public LayerStoneGuardianCoreAura(IGeoRenderer<EntityBossStoneGuardian> entityRenderer)
    {
        super(entityRenderer, ModelStoneGuardian.CORE_AURA_TEXTURE);
        alpha = DEFAULT_ALPHA;
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, EntityBossStoneGuardian entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if(entity.isCooled() && alpha > 0) alpha -= ALPHA_INCREMENT;
        else if(alpha < DEFAULT_ALPHA) alpha += ALPHA_INCREMENT;
        renderCopyModel((GeoModelProvider<EntityBossStoneGuardian>) getEntityModel(), texture, matrixStack, buffer, packedLight, entity, partialTicks, alpha, alpha, alpha);
    }
}
