package techeart.thadv.api.mixin;

import com.google.common.collect.Lists;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.entity.PartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

@Mixin(Level.class)
public abstract class MixinLevel
{
    @Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
            at = @At("HEAD"), cancellable = true)
    protected void onGetEntities(@Nullable Entity ignoredEntity, AABB box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir)
    {
        ((Level)(Object)this).getProfiler().incrementCounter("getEntities");
        List<Entity> list = Lists.newArrayList();
        this.getEntities().get(box, (entity) -> {
            if (entity != ignoredEntity && predicate.test(entity))
                list.add(entity);

            if (entity.isMultipartEntity())
            {
                PartEntity<?>[] parts = entity.getParts();
                if(parts != null)
                    for(PartEntity<?> part : entity.getParts())
                        if (entity != ignoredEntity && predicate.test(part))
                            list.add(part);
            }
        });

        cir.setReturnValue(list);
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
            at = @At("HEAD"), cancellable = true)
    protected <T extends Entity> void onGetEntities(EntityTypeTest<Entity, T> eType, AABB box, Predicate<? super T> predicate, CallbackInfoReturnable<List<T>> cir)
    {
        ((Level)(Object)this).getProfiler().incrementCounter("getEntities");
        List<T> list = Lists.newArrayList();
        this.getEntities().get(eType, box, (entity) -> {
            if (predicate.test(entity))
                list.add(entity);

            if (entity.isMultipartEntity())
            {
                PartEntity<?>[] parts = entity.getParts();
                if(parts != null)
                    for(PartEntity<?> part : entity.getParts())
                    {
                        T t = eType.tryCast(part);
                        if (t != null && predicate.test(t)) {
                            list.add(t);
                        }
                    }
            }
        });

        cir.setReturnValue(list);
    }

    @Shadow()
    protected abstract LevelEntityGetter<Entity> getEntities();
}
