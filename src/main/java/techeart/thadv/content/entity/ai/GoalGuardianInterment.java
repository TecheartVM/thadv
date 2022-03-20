package techeart.thadv.content.entity.ai;

import techeart.thadv.content.entity.entities.boss.EntityBossStoneGuardian;

import java.util.function.Function;

public class GoalGuardianInterment extends GoalAdvancedAttack<EntityBossStoneGuardian>
{
    private static final int SECOND_ATTACK_DELAY = 10;

    private int secondAttackTimer = 0;

    public GoalGuardianInterment(EntityBossStoneGuardian executor, Function<EntityBossStoneGuardian, Boolean> useCondition)
    {
        super(executor, 2.0f, 2.0f, 1.5f, 12.0f, useCondition);
        withDamageDelay(15);
        withFinishingTime(25);
        stripShield();
    }

    @Override
    protected void performAttack()
    {
        super.performAttack();
        secondAttackTimer = SECOND_ATTACK_DELAY;
    }

    @Override
    protected void tickFinishing()
    {
        if(secondAttackTimer > 0) secondAttackTimer--;
        else if(secondAttackTimer == 0)
        {
            performSecondAttack();
            secondAttackTimer = -1;
        }
        super.tickFinishing();
    }

    private void performSecondAttack()
    {
        GoalAdvancedAttack<EntityBossStoneGuardian> secondAttack = new GoalAdvancedAttack<>(
                executor, 4.0f, 3.0f, 1.5f, 4.0f, useCondition
        ).withKnockback(0.2f).knockUpwards().forceKnock();
        secondAttack.lockLookPos();
        secondAttack.performAttack();
    }
}
