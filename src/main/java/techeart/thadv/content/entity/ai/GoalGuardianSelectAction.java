package techeart.thadv.content.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import techeart.thadv.content.entity.entities.boss.EntityBossStoneGuardian;

import java.util.EnumMap;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class GoalGuardianSelectAction extends Goal
{
    private static final int ATTACK_COOLDOWN_P1 = 35;
    private static final int ATTACK_COOLDOWN_P2 = 12;

    private static final int TOKEN_ACCUM_TIME_P1 = 20;
    private static final int TOKEN_ACCUM_TIME_P2 = 10;

    private static final float DISTANCE_WEIGHT_MULT = 5.0f;
    private static final float SHIELD_WEIGHT_MULT = 3.0f;
    private static final float TOKENS_WEIGHT_MULT = 0.5f;
    private static final float RANDOM_WEIGHT_MULT = 4.0f;

    private final EntityBossStoneGuardian guardian;

    private int attackCooldown = 0;
    private int noActionTicks = 0;
    private int shieldHits = 0;

    private EnumMap<Attack, Integer> tokens;

    public GoalGuardianSelectAction(EntityBossStoneGuardian executor) { guardian = executor; }

    @Override
    public void tick()
    {
        LivingEntity target = guardian.getTarget();
        if(target == null) return;

        if(guardian.isStunned())
        {
            guardian.setState(EntityBossStoneGuardian.State.STUNNED);
            return;
        }
        if(guardian.isAttacking())
        {
            attackCooldown = getAttackCooldown();
            return;
        }
        if(guardian.shouldBeInRage())
        {
            guardian.setState(EntityBossStoneGuardian.State.RAGE);
            return;
        }
        if(attackCooldown > 0)
        {
            attackCooldown--;
            return;
        }

        int phase = guardian.getPhase();

        Attack attack = chooseAttack(target);
        if(attack != null)
        {
            attack.run(guardian);
            if(attack.isShieldStripping) shieldHits = 0;
            else if(target.isBlocking()) shieldHits++;
            noActionTicks = 0;

            int divisor = tokens.get(attack) / Math.max(attack.minTokens, 1);
            if(attack.isStrong)
            {
                if(phase < 2) divisor += 2;
            }
            else accrueTokens(1);

            operateTokens(attack, divisor, Integer::divideUnsigned);
        }
        else noActionTicks++;

        if(noActionTicks % (phase > 1 ? TOKEN_ACCUM_TIME_P2 : TOKEN_ACCUM_TIME_P1) == 0) accrueTokens(1);
    }

    private void accrueTokens(int count) { for (Attack a : Attack.values()) operateTokens(a, count, Integer::sum); }

    private boolean operateTokens(Attack a, int t, BiFunction<? super Integer, ? super Integer, ? extends Integer> operation)
    {
        if(!a.isMatchingPhase(guardian)) return false;
        Integer i = tokens.get(a);
        if(i == null) i = 0;
        tokens.put(a, operation.apply(i, t));
        return true;
    }

    @Override
    public boolean canUse()
    {
        if(!guardian.isActive()) return false;
        LivingEntity target = guardian.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start()
    {
        attackCooldown = getAttackCooldown();
        shieldHits = 0;
        tokens = new EnumMap<>(Attack.class);
        for (Attack a : Attack.values()) { tokens.put(a, 0); }
    }

    private Attack chooseAttack(LivingEntity target)
    {
        float distToTarget = guardian.distanceTo(target);
        Attack result = null;
        float maxWeight = 0.0f;
        float shieldHitsWeight = shieldHits * SHIELD_WEIGHT_MULT;

        for (Attack a : Attack.values())
        {
            if(!a.isMatchingPhase(guardian)) continue;
            if(tokens.get(a) < a.minTokens) continue;
            if(distToTarget > a.maxDist || distToTarget < a.minDist) continue;

            float wDist = Math.abs((a.maxDist + a.minDist)/2 - distToTarget);
            wDist = (wDist <= 1 ? 1 : 1/wDist) * DISTANCE_WEIGHT_MULT;
            float wTokens = (tokens.get(a) - a.minTokens) * TOKENS_WEIGHT_MULT;
            float wShield = a.isShieldStripping ? shieldHitsWeight : 0;
            float wRandom = new Random().nextFloat() * RANDOM_WEIGHT_MULT;

            float wTotal = wDist + wTokens + wShield + wRandom;
            if(wTotal > maxWeight)
            {
                maxWeight = wTotal;
                result = a;
            }
        }

        return result;
    }

    private int getAttackCooldown()
    {
        return guardian.getPhase() > 1 ? ATTACK_COOLDOWN_P2 : ATTACK_COOLDOWN_P1;
    }

    private enum Attack
    {
        SWEEP(0, 4.0f, false, 1, false, p -> true, g -> g.setState(EntityBossStoneGuardian.State.getSweep())),
        STOMP(0, 9.0f, true, 5, true, p -> true, g -> g.setState(EntityBossStoneGuardian.State.STOMP)),
        INTERMENT(0, 3.5f, true, 4, false, p -> p >= 2, g -> g.setState(EntityBossStoneGuardian.State.INTERMENT)),
        RAM(5.0f, 15.0f, false, 4, false, p -> p >= 2, g -> g.setState(EntityBossStoneGuardian.State.RAM)),
        SMASH(0, 6.0f, true, 8, true, p -> p < 2, g -> g.setState(EntityBossStoneGuardian.State.SMASH)),
        SMASH_2(0, 12.0f, true, 8, true, p -> p > 1, g -> g.setState(EntityBossStoneGuardian.State.SMASH)),
        SHOOT(2.0f, 12.0f, false,4, false, p -> true, g -> g.setState(EntityBossStoneGuardian.State.SHOOT));

        public final float minDist;
        public final float maxDist;
        public final boolean isShieldStripping;
        public final int minTokens;
        public final boolean isStrong;
        private final Function<Integer, Boolean> phaseCondition;
        private final Consumer<EntityBossStoneGuardian> method;
        Attack(float minDist, float maxDist, boolean isShieldStripping, int minTokens, boolean isStrong,
               Function<Integer, Boolean> phaseCondition, Consumer<EntityBossStoneGuardian> method)
        {
            this.minDist = minDist;
            this.maxDist = maxDist;
            this.isShieldStripping = isShieldStripping;
            this.minTokens = minTokens;
            this.isStrong = isStrong;
            this.phaseCondition = phaseCondition;
            this.method = method;
        }

        public boolean isMatchingPhase(EntityBossStoneGuardian guardian) { return phaseCondition.apply(guardian.getPhase()); }

        public void run(EntityBossStoneGuardian guardian) { method.accept(guardian); }
    }
}
