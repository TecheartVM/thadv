package techeart.thadv.content.entity.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import techeart.thadv.content.entity.entities.boss.EntityBossStoneGuardian;
import techeart.thadv.content.entity.entities.projectile.EntityLavaBall;

import java.util.Random;

public class GoalGuardianShoot extends Goal
{
    private static final int SHOOT_INTERVAL = 15;
    private static final int SHOT_COUNT = 5;
    private static final int SHOOT_START_DELAY = 30;
    private static final int ANIMATION_FINISH_TIME = 25;

    private int shootTimer = 0;
    private int shotCounter = 0;
    private int finishTimer = 0;
    private boolean finishing = false;
    private Vec3 lookPos;

    private final EntityBossStoneGuardian guardian;
    private final float bulletSpawnerYOffset;

    public GoalGuardianShoot(EntityBossStoneGuardian guardian, float bulletSpawnerYOffset)
    {
        this.guardian = guardian;
        this.bulletSpawnerYOffset = bulletSpawnerYOffset;
    }

    @Override
    public boolean canUse()
    {
        if(!guardian.isActive() || guardian.getState() != EntityBossStoneGuardian.State.SHOOT)
            return false;
        LivingEntity target = guardian.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() { return canUse() && !isFinished(); }

    @Override
    public void start()
    {
        shootTimer = SHOOT_START_DELAY;
        shotCounter = 0;
        finishTimer = ANIMATION_FINISH_TIME;
        finishing = false;
        lockLookPos();
    }

    @Override
    public void stop() { guardian.finishAttack(); }

    @Override
    public void tick()
    {
        guardian.getLookControl().setLookAt(lookPos);

        if(finishing)
        {
            if(finishTimer > 0) finishTimer--;
            return;
        }

        if(shootTimer > 0) shootTimer--;
        else
        {
            makeShot();
            shotCounter++;
            shootTimer = SHOOT_INTERVAL;

            if(shotCounter >= SHOT_COUNT) finishing = true;
        }
    }

    private void makeShot()
    {
        EntityLavaBall projectile = new EntityLavaBall(guardian.level, guardian);
        LivingEntity target = guardian.getTarget();
        double deltaX = target.getX() - guardian.getX();
        double deltaZ = target.getZ() - guardian.getZ();
        double d2 = guardian.getY() + bulletSpawnerYOffset - projectile.getY() - 1.1d;
        double d4 = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 0.2d;
        projectile.shoot(deltaX, d2 + d4, deltaZ, 0.7F, 2.0F);
        float f = 0.4F / (new Random().nextFloat() * 0.4F + 0.8F);
        guardian.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, f);
        guardian.playSound(SoundEvents.LAVA_AMBIENT, 1.0F, f);
        guardian.level.addFreshEntity(projectile);
    }

    private void lockLookPos() { lookPos = guardian.getTarget().position(); }

    private boolean isFinished() { return finishing && finishTimer <= 0; }
}
