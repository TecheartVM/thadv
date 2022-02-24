package techeart.thadv.content.entities.models;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import techeart.thadv.content.MainClass;
import techeart.thadv.content.entities.EntityBossStoneGuardian;

public class ModelStoneGuardian extends AnimatedGeoModel<EntityBossStoneGuardian>
{
    public static final ResourceLocation CORE_TEXTURE = MainClass.RLof("textures/entities/stone_guardian_core.png");
    public static final ResourceLocation CORE_AURA_TEXTURE = MainClass.RLof("textures/entities/stone_guardian_core_aura.png");

    @Override
    public ResourceLocation getModelLocation(EntityBossStoneGuardian object)
    {
        return MainClass.RLof("geo/stone_guardian.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBossStoneGuardian object)
    {
        return MainClass.RLof("textures/entities/stone_guardian.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityBossStoneGuardian animatable)
    {
        return MainClass.RLof("animations/stone_guardian.json");
    }
}
