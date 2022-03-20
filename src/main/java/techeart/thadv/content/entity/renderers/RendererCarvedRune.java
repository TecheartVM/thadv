package techeart.thadv.content.entity.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import techeart.thadv.content.entity.entities.misc.EntityCarvedRune;
import techeart.thadv.content.misc.Rune;

import java.util.Map;

public class RendererCarvedRune extends EntityRenderer<EntityCarvedRune>
{
    public RendererCarvedRune(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override
    public void render(EntityCarvedRune entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight)
    {
        super.render(entity, entityYaw, partialTicks, pose, buffer, packedLight);
        pose.pushPose();
        pose.translate(0.0f, 0.5f, 0.0f);
        for(Map.Entry<Direction, Rune> e : entity.getCarvedFaces().entrySet())
            renderFace(e.getKey(), entity, e.getValue().getResourceLocation(), pose, buffer, packedLight);
        pose.popPose();
    }

    private void renderFace(Direction face, EntityCarvedRune entity, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        VertexConsumer builder = buffer.getBuffer(getRenderType(texture));
        poseStack.pushPose();
        if(face.getAxis().isVertical()) renderFaceHorizontal(face, entity, poseStack, builder, packedLight);
        else renderFaceVertical(face, poseStack, builder, packedLight);
        poseStack.popPose();
    }

    private void renderFaceVertical(Direction face, PoseStack poseStack, VertexConsumer builder, int packedLight)
    {
        Vector3f normal = face.step();
        Vector3f right = face.getClockWise().step();
        Vector3f vertexBias = new Vector3f(right.x() * 0.5f, 0.5f, right.z() * 0.5f);
        Vector3f rotation = normal.copy();
        rotation.mul(90f);

        poseStack.translate(normal.x() * 0.501d, normal.y() * 0.501d, normal.z() * 0.501d);

        renderQuad(builder, poseStack, normal, vertexBias, rotation, packedLight);
    }

    private void renderFaceHorizontal(Direction face, EntityCarvedRune entity, PoseStack poseStack, VertexConsumer builder, int packedLight)
    {
        Direction entityDir = face == Direction.UP ? entity.getDirection() : entity.getBottomRuneDir();
        Vector3f normal = face.step();
        Vector3f vertexBias = entityDir.step();
        vertexBias.add(entityDir.getCounterClockWise().step());
        vertexBias.mul(0.5f);
        Vector3f rotation = normal.copy();
        rotation.mul(90f);

        poseStack.translate(normal.x() * 0.501d, normal.y() * 0.501d, normal.z() * 0.501d);

        if(face == Direction.DOWN) vertexBias.transform(Quaternion.fromXYZDegrees(rotation));
        renderQuad(builder, poseStack, normal, vertexBias, rotation, packedLight);
    }

    private static final int[] uv = { 0, 0, 1, 1, 0, 1, 1, 0 };
    private static void renderQuad(VertexConsumer builder, PoseStack poseStack, Vector3f normal, Vector3f vertexBias, Vector3f rotation, int packedLight)
    {
        poseStack.pushPose();

        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();

        for(int i = 0; i < 4; i++)
        {
            builder.vertex(matrix4f, vertexBias.x(), vertexBias.y(), vertexBias.z())
                    .color(255, 255, 255, 255)
                    .uv(uv[i], uv[i+4])
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(packedLight)
                    .normal(matrix3f, normal.x(), normal.y(), normal.z())
                    .endVertex();
            if(i < 3) vertexBias.transform(Quaternion.fromXYZDegrees(rotation));
        }

        poseStack.popPose();
    }

//    private void renderFace(Direction face, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
//    {
//        VertexConsumer builder = buffer.getBuffer(getRenderType(texture));
//        poseStack.pushPose();
//        Vector3f normal = face.step();
//        Vector3f vertexBias = new Vector3f(1 - Math.abs(normal.x()), 1 - Math.abs(normal.y()), 1 - Math.abs(normal.z()));
//        vertexBias.mul(-0.5f);
//        Vector3f rotation = normal.copy();
//        rotation.mul(90f);
//        Matrix4f matrix4f = poseStack.last().pose();
//        Matrix3f matrix3f = poseStack.last().normal();
//
//        poseStack.translate(normal.x() * 0.501d, normal.y() * 0.501d, normal.z() * 0.501d);
//
//        builder.vertex(matrix4f, vertexBias.x(), vertexBias.y(), vertexBias.z())
//                .color(255, 255, 255, 255)
//                .uv(0, 1)
//                .overlayCoords(OverlayTexture.NO_OVERLAY)
//                .uv2(packedLight)
//                .normal(matrix3f, normal.x(), normal.y(), normal.z())
//                .endVertex();
//        vertexBias.transform(Quaternion.fromXYZDegrees(rotation));
//        builder.vertex(matrix4f, vertexBias.x(), vertexBias.y(), vertexBias.z())
//                .color(255, 255, 255, 255)
//                .uv(1, 1)
//                .overlayCoords(OverlayTexture.NO_OVERLAY)
//                .uv2(packedLight)
//                .normal(matrix3f, normal.x(), normal.y(), normal.z())
//                .endVertex();
//        vertexBias.transform(Quaternion.fromXYZDegrees(rotation));
//        builder.vertex(matrix4f, vertexBias.x(), vertexBias.y(), vertexBias.z())
//                .color(255, 255, 255, 255)
//                .uv(1, 0)
//                .overlayCoords(OverlayTexture.NO_OVERLAY)
//                .uv2(packedLight)
//                .normal(matrix3f, normal.x(), normal.y(), normal.z())
//                .endVertex();
//        vertexBias.transform(Quaternion.fromXYZDegrees(rotation));
//        builder.vertex(matrix4f, vertexBias.x(), vertexBias.y(), vertexBias.z())
//                .color(255, 255, 255, 255)
//                .uv(0, 0)
//                .overlayCoords(OverlayTexture.NO_OVERLAY)
//                .uv2(packedLight)
//                .normal(matrix3f, normal.x(), normal.y(), normal.z())
//                .endVertex();
//
//        poseStack.popPose();
//    }

    private static RenderType getRenderType(ResourceLocation texture)
    {
        return RenderType.eyes(texture);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCarvedRune entity) { return null; }
}
