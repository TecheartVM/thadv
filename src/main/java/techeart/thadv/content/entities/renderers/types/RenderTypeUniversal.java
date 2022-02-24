package techeart.thadv.content.entities.renderers.types;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RenderTypeUniversal extends RenderType
{
    private RenderTypeUniversal(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_)
    {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
        throw new IllegalStateException("This class must not be instantiated");
    }

    public static RenderType create(String name, ResourceLocation texture, Transparency t, boolean culling, boolean lightmap, boolean overlay)
    {
        return create(name, texture, 0, 0, t, culling, lightmap, overlay, false);
    }
    public static RenderType create(String name, ResourceLocation texture, float xOffset, float yOffset, Transparency t, boolean culling, boolean lightmap, boolean overlay)
    {
        return create(name, texture, xOffset, yOffset, t, culling, lightmap, overlay, false);
    }
    public static RenderType create(String name, ResourceLocation texture, float xOffset, float yOffset, Transparency t, boolean culling, boolean lightmap, boolean overlay, boolean affectsOutline)
    {
        boolean blur = false, mipmap = false; //TODO
        return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEnergySwirlShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, blur, mipmap))
                        .setTexturingState(new RenderStateShard.OffsetTexturingStateShard(xOffset, yOffset))
                        .setTransparencyState(t.getState())
                        .setCullState(new RenderStateShard.CullStateShard(culling))
                        .setLightmapState(new RenderStateShard.LightmapStateShard(lightmap))
                        .setOverlayState(new RenderStateShard.OverlayStateShard(overlay))
                        .createCompositeState(affectsOutline)
        );
    }

    public enum Transparency
    {
        NONE(NO_TRANSPARENCY),
        ADDITIVE(ADDITIVE_TRANSPARENCY),
        LIGHTNING(LIGHTNING_TRANSPARENCY),
        GLINT(GLINT_TRANSPARENCY),
        CRUMBLING(CRUMBLING_TRANSPARENCY),
        TRANSLUCENT(TRANSLUCENT_TRANSPARENCY);
        private final TransparencyStateShard state;
        Transparency(TransparencyStateShard state) { this.state = state; }
        public TransparencyStateShard getState() { return state; }
    }
}
