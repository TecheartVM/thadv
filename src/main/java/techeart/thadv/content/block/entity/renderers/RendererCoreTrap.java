package techeart.thadv.content.block.entity.renderers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.system.MemoryStack;
import techeart.thadv.content.block.BlockCoreTrap;
import techeart.thadv.content.block.entity.BlockEntityCoreTrap;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class RendererCoreTrap implements BlockEntityRenderer<BlockEntityCoreTrap>
{
    protected static final Minecraft mc = Minecraft.getInstance();

    private final BlockRenderDispatcher blockRenderer;

    public RendererCoreTrap(BlockEntityRendererProvider.Context ctx)
    {
        this.blockRenderer = ctx.getBlockRenderDispatcher();
    }

    @Override
    public void render(BlockEntityCoreTrap entity, float partialTicks, @Nonnull PoseStack poseStack, MultiBufferSource buffer, int packedLight, int overlay)
    {
        if(entity.getLevel() == null) return;

        BlockState state = entity.getBlockState();
        VertexConsumer builder = buffer.getBuffer(RenderType.translucent()); //translucent render type

        if(state.hasProperty(BlockCoreTrap.CAMOUFLAGE) && state.getValue(BlockCoreTrap.CAMOUFLAGE))
        {
            ResourceLocation rl = entity.getCamouflageTexture(blockRenderer);
            if(rl == null) return;
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(rl);
            builder = new RetexturingVertexConsumer(builder, sprite);
        }

        blockRenderer.getModelRenderer().tesselateBlock(
                entity.getLevel(),
                getModel(state),
                state,
                entity.getBlockPos(),
                poseStack,
                builder,
                false,
                new Random(),
                state.getSeed(entity.getBlockPos()),
                overlay,
                EmptyModelData.INSTANCE
        );
    }

    protected BakedModel getModel(BlockState state) { return mc.getBlockRenderer().getBlockModel(state); }

    protected static class RetexturingVertexConsumer implements VertexConsumer
    {
        private final VertexConsumer base;
        private final TextureAtlasSprite newSprite;

        public RetexturingVertexConsumer(VertexConsumer base, TextureAtlasSprite newTexture)
        {
            this.base = base;
            newSprite = newTexture;
        }

        @Override
        public void putBulkData(PoseStack.Pose matrixEntry, BakedQuad bakedQuad,
                                float[] baseBrightness, float red, float green, float blue, int[] lightmapCoords,
                                int overlayCoords, boolean readExistingColor
        ) {
            int[] aint = bakedQuad.getVertices();
            Vec3i faceNormal = bakedQuad.getDirection().getNormal();
            Vector3f normal = new Vector3f((float)faceNormal.getX(), (float)faceNormal.getY(), (float)faceNormal.getZ());
            Matrix4f matrix4f = matrixEntry.pose();
            normal.transform(matrixEntry.normal());
            int intSize = DefaultVertexFormat.BLOCK.getIntegerSize();
            int vertexCount = aint.length / intSize;

            TextureAtlasSprite oldSprite = bakedQuad.getSprite();

            try (MemoryStack memorystack = MemoryStack.stackPush())
            {
                ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
                IntBuffer intbuffer = bytebuffer.asIntBuffer();

                for(int i = 0; i < vertexCount; ++i)
                {
                    intbuffer.clear();
                    intbuffer.put(aint, i * 8, 8);
                    float f = bytebuffer.getFloat(0);
                    float f1 = bytebuffer.getFloat(4);
                    float f2 = bytebuffer.getFloat(8);
                    float cr;
                    float cg;
                    float cb;
                    if(readExistingColor)
                    {
                        float r = (float) (bytebuffer.get(12) & 255) / 255.0F;
                        float g = (float) (bytebuffer.get(13) & 255) / 255.0F;
                        float b = (float) (bytebuffer.get(14) & 255) / 255.0F;
                        cr = r * baseBrightness[i] * red;
                        cg = g * baseBrightness[i] * green;
                        cb = b * baseBrightness[i] * blue;
                    }
                    else
                    {
                        cr = baseBrightness[i] * red;
                        cg = baseBrightness[i] * green;
                        cb = baseBrightness[i] * blue;
                    }

                    int lightmapCoord = this.applyBakedLighting(lightmapCoords[i], bytebuffer);
                    float u = bytebuffer.getFloat(16);
                    float v = bytebuffer.getFloat(20);

                    u = newSprite.getU(oldSprite.getUOffset(u));
                    v = newSprite.getV(oldSprite.getVOffset(v));

                    Vector4f pos = new Vector4f(f, f1, f2, 1.0F);
                    pos.transform(matrix4f);
                    this.applyBakedNormals(normal, bytebuffer, matrixEntry.normal());
                    this.vertex(pos.x(), pos.y(), pos.z(), cr, cg, cb, 1.0f, u, v, overlayCoords, lightmapCoord, normal.x(), normal.y(), normal.z());
                }
            }
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) { return base.vertex(x, y, z); }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) { return base.color(r, g, b, a); }

        @Override
        public VertexConsumer uv(float u, float v) { return base.uv(u, v); }

        @Override
        public VertexConsumer overlayCoords(int u, int v) { return base.overlayCoords(u, v); }

        @Override
        public VertexConsumer uv2(int u, int v) { return base.uv2(u, v); }

        @Override
        public VertexConsumer normal(float x, float y, float z) { return base.normal(x, y, z); }

        @Override
        public void endVertex() { base.endVertex(); }

        @Override
        public void defaultColor(int r, int g, int b, int a) { base.defaultColor(r, g, b, a); }

        @Override
        public void unsetDefaultColor() { base.unsetDefaultColor(); }
    }
}
