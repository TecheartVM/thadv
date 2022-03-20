package techeart.thadv.content.entity.entities.basic;

import net.minecraft.world.entity.Entity;

import java.util.List;

public interface IAdvancedAttackMob
{
    default void onAttackStarted() {}
    default void onAttackPerformed(List<Entity> entities) {}
    default boolean finishAttack() { return true; }
}
