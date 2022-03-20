package techeart.thadv.content.entity.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import techeart.thadv.content.MainClass;
import techeart.thadv.content.entity.entities.misc.EntityEruptionSource;

public class RendererEruptionSource extends EntityRenderer<EntityEruptionSource>
{
    private static final ResourceLocation TEXTURE_BASE = MainClass.RLof("textures/entities/eruption_source.png");
    private static final ResourceLocation TEXTURE_GLOW = MainClass.RLof("textures/entities/eruption_source_glow.png");
    private static final ResourceLocation TEXTURE_FIRE = new ResourceLocation("minecraft", "block/campfire_fire");
    private static final RenderType RENDER_TYPE_BASE = RenderType.entityCutoutNoCull(TEXTURE_BASE);
    private static final RenderType RENDER_TYPE_GLOW = RenderType.entityTranslucent(TEXTURE_GLOW);
    private static final RenderType RENDER_TYPE_FIRE = RenderType.translucentNoCrumbling();

    public RendererEruptionSource(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override
    public void render(EntityEruptionSource entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        poseStack.pushPose();
        VertexConsumer builder = buffer.getBuffer(RENDER_TYPE_BASE);
        drawRect(builder, poseStack, 0.01f, packedLight);
        builder = buffer.getBuffer(RENDER_TYPE_GLOW);
        drawRect(builder, poseStack, 0.01f, 240);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, 0.5f, 0);
        Quaternion q = this.entityRenderDispatcher.cameraOrientation();
        poseStack.mulPose(new Quaternion(0, q.j(), 0, q.r()));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        builder = buffer.getBuffer(RENDER_TYPE_FIRE);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(TEXTURE_FIRE);
        drawRect(builder, poseStack, new Vector3f(0, 0, 1), sprite, 16.0f, 1.0f, 240);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEruptionSource entity) { return TEXTURE_BASE; }

    //TODO: optimization

    private static void drawRect(VertexConsumer builder, PoseStack poseStack, float yOffset, int packedLight)
    {
        builder.vertex(poseStack.last().pose(), -0.5f, yOffset, -0.5f)
                .color(255, 255, 255, 255)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        builder.vertex(poseStack.last().pose(), 0.5f, yOffset, -0.5f)
                .color(255, 255, 255, 255)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        builder.vertex(poseStack.last().pose(), 0.5f, yOffset, 0.5f)
                .color(255, 255, 255, 255)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        builder.vertex(poseStack.last().pose(), -0.5f, yOffset, 0.5f)
                .color(255, 255, 255, 255)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();
    }

    private static void drawRect(VertexConsumer builder, PoseStack poseStack, Vector3f normal, TextureAtlasSprite sprite, float resolution, float scale, int packedLight)
    {
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();

        Vector3f vertexBias = new Vector3f(1, 1, 1);
        vertexBias.sub(normal);
        vertexBias.mul(scale * (-0.5f));
        Vector3f rotation = normal;
        rotation.mul(90);

        builder.vertex(matrix4f, vertexBias.x(), vertexBias.y(), vertexBias.z())
                .color(255, 255, 255, 255)
                .uv(sprite.getU(0), sprite.getV(resolution))
                .uv2(packedLight)
                .normal(matrix3f, normal.x(), normal.y(), normal.z())
                .endVertex();
        vertexBias.transform(Quaternion.fromXYZDegrees(rotation));
        builder.vertex(matrix4f, vertexBias.x(), vertexBias.y(), vertexBias.z())
                .color(255, 255, 255, 255)
                .uv(sprite.getU(resolution), sprite.getV(resolution))
                .uv2(packedLight)
                .normal(matrix3f, normal.x(), normal.y(), normal.z())
                .endVertex();
        vertexBias.transform(Quaternion.fromXYZDegrees(rotation));
        builder.vertex(matrix4f, vertexBias.x(), vertexBias.y(), vertexBias.z())
                .color(255, 255, 255, 255)
                .uv(sprite.getU(resolution), sprite.getV(0))
                .uv2(packedLight)
                .normal(matrix3f, normal.x(), normal.y(), normal.z())
                .endVertex();
        vertexBias.transform(Quaternion.fromXYZDegrees(rotation));
        builder.vertex(matrix4f, vertexBias.x(), vertexBias.y(), vertexBias.z())
                .color(255, 255, 255, 255)
                .uv(sprite.getU(0), sprite.getV(0))
                .uv2(packedLight)
                .normal(matrix3f, normal.x(), normal.y(), normal.z())
                .endVertex();
    }
}
