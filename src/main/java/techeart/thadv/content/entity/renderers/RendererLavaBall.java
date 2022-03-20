package techeart.thadv.content.entity.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import techeart.thadv.content.entity.entities.projectile.EntityLavaBall;

public class RendererLavaBall extends EntityRenderer<EntityLavaBall>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "block/lava_still");
    private static final RenderType RENDER_TYPE = RenderType.translucentNoCrumbling();

    public RendererLavaBall(EntityRendererProvider.Context context) { super(context); }

    @Override
    public void render(EntityLavaBall entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight)
    {
        super.render(entity, entityYaw, partialTicks, pose, buffer, packedLight);
        float hitboxSize = entity.getType().getDimensions().width;
        Vec3 temp = entity.getDeltaMovement().normalize();
        Vector3f moveDir = new Vector3f((float)temp.x, 0, (float)temp.z);
        renderCube(0.2f, 1, hitboxSize, pose, buffer, packedLight, entity.tickCount + partialTicks, moveDir);
    }

    private void renderCube(float cubeSize, float textureScale, float hitboxSize, PoseStack pose, MultiBufferSource buffer, int packedLight, float ticks, Vector3f moveDir)
    {
        VertexConsumer consumer = buffer.getBuffer(RENDER_TYPE);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(TEXTURE);
        pose.pushPose();
        pose.translate(0.0D, hitboxSize * 0.5f, 0.0D);
        ticks *= 10;
        pose.mulPose(Quaternion.fromXYZ(moveDir));
        pose.mulPose(Vector3f.XP.rotationDegrees(-(ticks % 360)));

        Matrix4f matrix4f = pose.last().pose();

        float radius = cubeSize * 0.5f;
        float pixels = cubeSize * textureScale * 10;

        Vector3f vertexPos = new Vector3f(-radius, -radius, radius);
        Vector3f vertexRotAxis = new Vector3f(0, 0, -1);
        Vector3f polygonRotAxis = new Vector3f(0, 1, 0);
        int mult = 1;
        Vector3f uv = new Vector3f(-pixels * 0.5f, pixels * 0.5f, 0);
        float uvYOffset = pixels;

        for (int j = 0; j < 6; j++)
        {
            //draw single polygon
            for(int i = 0; i < 4; i++)
            {
                consumer.vertex(matrix4f, -vertexPos.x(), -vertexPos.y(), -vertexPos.z())
                        .color(255, 255, 255, 255)
                        .uv(sprite.getU(uv.x() + pixels/2), sprite.getV(uv.y() + pixels/2 + uvYOffset))
                        .uv2(packedLight)
                        .normal(0, -1, 0)
                        .endVertex();
                vertexPos.transform(new Quaternion(vertexRotAxis, 90, true));
                uv.transform(new Quaternion(new Vector3f(0, 0, -1), 90, true));
            }

            Vector3f temp = vertexRotAxis.copy();
            vertexPos.transform(new Quaternion(polygonRotAxis, 90*mult, true));
            vertexRotAxis.transform(new Quaternion(polygonRotAxis, 90*mult, true));
            polygonRotAxis = temp;
            uvYOffset += pixels;
            mult *= -1;
        }
        pose.popPose();
    }

    @Override
    protected int getBlockLightLevel(EntityLavaBall entity, BlockPos pos) { return 15; }

    @Override
    public ResourceLocation getTextureLocation(EntityLavaBall entity) { return TEXTURE; }
}
