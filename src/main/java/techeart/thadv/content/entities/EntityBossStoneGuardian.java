package techeart.thadv.content.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import techeart.thadv.api.EntityMultipart;
import techeart.thadv.api.EntityMultipartPart;
import techeart.thadv.api.IModEntity;
import techeart.thadv.content.entities.ai.*;
import techeart.thadv.content.world.ModBossEventServer;
import techeart.thadv.content.network.PacketHandler;
import techeart.thadv.content.network.packets.PacketGuardianStateSync;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class EntityBossStoneGuardian extends EntityMultipart implements IAnimatable, IAdvancedAttackMob, IModEntity, IEasySyncable
{
    public static final EntityType<EntityBossStoneGuardian> TYPE = EntityType.Builder.of(
            EntityBossStoneGuardian::new,
            MobCategory.MONSTER
    ).sized(2.0f, 3.5f).fireImmune().build("stone_guardian");

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.ARMOR, 30.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.ATTACK_SPEED, 1.0D);
    }

    //animations
    private final AnimationFactory animationFactory = new AnimationFactory(this);
    private final AnimationController<EntityBossStoneGuardian> movementController =
            new AnimationController<>(this, "movement", 4, event -> {
                event.getController().setAnimation(new AnimationBuilder().addAnimation(getState().getAnimation()));
                return PlayState.CONTINUE;
            });
    private final AnimationController<EntityBossStoneGuardian> coreController =
            new AnimationController<>(this, "core", 4, event -> {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("core", true));
                return PlayState.CONTINUE;
            });

    //health bar
    private final ModBossEventServer bossEvent = new ModBossEventServer(getDisplayName());

    public static final int MAX_HIT_ANGLE = 45;
    private static final int DEFAULT_STUN_TIME = 140;
    private static final float COOLED_INCOMING_DAMAGE_MULT = 1.6f;
    private static final float SNOWBALL_DAMAGE = 2.0f;
    private static final int DIE_TIME = 80;
    private static final float CORE_Y_OFFSET_DEFAULT = 2.35f;
    private static final float CORE_Y_OFFSET_SMASHING = 2.15f;
    private static final float CORE_Y_OFFSET_STUNNED = 1.55f;

    private final EntityMultipartPart corePart;

    private int activationTimer = 170;
    private int phase = 0;
    private boolean shouldBeInRage = false;
    private State state = State.INACTIVE;
    private Entity lastTarget;
    private int stunTimer = 0;
    private boolean cooled = false;
    private int dieTimer = DIE_TIME;
    private DamageSource deadBy;

    public EntityBossStoneGuardian(EntityType<? extends PathfinderMob> type, Level level)
    {
        super(type, level);
        this.maxUpStep = 1.0F;
        corePart = addPart(new EntityMultipartPart("core", this, 0.5f, 0.5f).withOffset(CORE_Y_OFFSET_DEFAULT));
    }

    private boolean createStatePredicate(EntityBossStoneGuardian guardian, State state)
    {
        return guardian.isActive() && !guardian.isStunned() && guardian.getState() == state;
    }

    @Override
    protected void registerGoals()
    {
        super.registerGoals();

        //being inactive
        goalSelector.addGoal(0, new GoalDoNothing<>(this, guardian -> guardian.isStunned() && guardian.isActive() || !guardian.isActive()));
        //rage
        goalSelector.addGoal(1, new GoalAdvancedAttack<>(this, 4.0f, 4.0f, 0.0f, 3.0f, guardian ->
                createStatePredicate(guardian, State.RAGE))
                .withKnockback(4.0f).forceKnock()
                .withDamageDelay(15).withFinishingTime(45)
        );
        //sweep attack
        goalSelector.addGoal(2, new GoalAdvancedAttack<>(this, 1.0f, 2.5f, 2.5f, 8.0f, guardian ->
                guardian.isActive() && !guardian.isStunned() && guardian.getState().isSweep())
                .withHeightOffset(1.0f)
                .withKnockback(5.0f).forceKnock()
                .withDamageDelay(15).withFinishingTime(10)
        );
        //stomp attack
        goalSelector.addGoal(2, new GoalGuardianStomp<>(this, 0.5f, 13.0f, 0f, 20, 0.8f,6.0f, guardian ->
                createStatePredicate(guardian, State.STOMP))
                .withKnockback(1.0f).knockUpwards().forceKnock()
                .withDamageDelay(18).withFinishingTime(20)
                .stripShield()
        );
        //smash attack
        goalSelector.addGoal(2, new GoalGuardianSmash(this, guardian -> createStatePredicate(guardian, State.SMASH)));
        //interment attack
        goalSelector.addGoal(2, new GoalGuardianInterment(this, guardian -> createStatePredicate(guardian, State.INTERMENT)));
        //ram attack
        goalSelector.addGoal(2, new GoalRam<>(this, 0.4f, 2.4f, guardian -> createStatePredicate(guardian, State.RAM))
                .inflateAttackArea(2.0D, 0, 2.0D));
        //shoot attack
        goalSelector.addGoal(2, new GoalGuardianShoot(this, 2.6f));
        //movement
        goalSelector.addGoal(2, new GoalMoveTowardsTarget<>(this, 0.3f, 5.0f, guardian ->
                guardian.isActive() && !guardian.isStunned() && !guardian.getState().isAttack()));
        //goal selector
        goalSelector.addGoal(3, new GoalGuardianSelectAction(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isActive() { return phase > 0; }

    public boolean isAttacking() { return state.isAttack(); }

    public State getState() { return state; }

    public void setState(State value)
    {
        if(level.isClientSide())
        {
            if(state != value)
            {
                if(value == State.SMASH) corePart.setOffset(CORE_Y_OFFSET_SMASHING);
                else if(state == State.SMASH) corePart.setOffset(CORE_Y_OFFSET_DEFAULT);
            }
        }
        else PacketHandler.sendToTracking(new PacketGuardianStateSync(value, this), this);
        state = value;
    }

    public int getPhase() { return phase; }

    @Override
    public boolean finishAttack()
    {
        if(isStunned()) return true;
        if(getState() == State.RAGE) shouldBeInRage = false;
        if(getTarget() != null && getTarget().isAlive()) setState(EntityBossStoneGuardian.State.WALK);
        else setState(EntityBossStoneGuardian.State.IDLE);
        return true;
    }

    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount)
    {
        if(!isActive()) return false;
        if(dieTimer < DIE_TIME) return false;
        if(isInvulnerableTo(source)) return false;

        if(isCooled()) amount *= COOLED_INCOMING_DAMAGE_MULT;

        Entity e = source.getEntity();
        boolean res = false;
        float healthBeforeHurt = getHealth();
        if(e == null) res = super.hurt(source, amount);
        else if(isVulnerableFor(e))
        {
            Entity directE = source.getDirectEntity();
            if(directE != null && directE.getType() == EntityType.SNOWBALL)
            {
                res = super.hurt(source, SNOWBALL_DAMAGE);
                if(res) callOnClients(this, "CoolingEffects");
            }
            else res = super.hurt(source, amount);
        }

        if(isDeadOrDying())
        {
            setHealth(1.0f);
            setState(State.DEATH);
            deadBy = source;
        }
        else if(res)
        {
            //check for subphase change
            float f = getMaxHealth() / 3;
            float health = getHealth();
            for(int i = 0; i < 2; i++)
            {
                if(health <= f && healthBeforeHurt > f)
                {
                    stun();
                    setCooled(false);
                    break;
                }
                f += f;
            }
        }

        return res;
    }

    @Override
    public boolean isInvulnerableTo(@Nonnull DamageSource source)
    {
        return isRemoved() || isInvulnerable() || source.isMagic() || source.isFire() || (source.isProjectile() && isStunned()) ||
                source == DamageSource.SWEET_BERRY_BUSH || source == DamageSource.CACTUS || source == DamageSource.CRAMMING;
    }

    public boolean hurtByPotion(ThrownPotion potion)
    {
        if(level.isClientSide()) return false;
        if(PotionUtils.getPotion(potion.getItem()) == Potions.WATER)
        {
            playCoolingEffects();
            Entity thrower = potion.getOwner();
            if(thrower != null && isVulnerableFor(thrower))
            {
                int stunTime = DEFAULT_STUN_TIME;
                if(potion.getItem().is(Items.LINGERING_POTION)) stunTime *= 1.2f;
                boolean flag = stun(stunTime);
                if(flag) level.explode(this, corePart.getX(), corePart.getY(), corePart.getZ(), 1.0f, Explosion.BlockInteraction.NONE);
                return flag;
            }
        }
        return false;
    }

    protected void playCoolingEffects()
    {
        if(level.isClientSide())
        {
            level.playLocalSound(corePart.getX(), corePart.getY(), corePart.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundSource.HOSTILE, 1.0f, 1.0f, false);
            double d = Math.PI / 5f;
            for(int i = 0; i < 10; i++)
                level.addParticle(ParticleTypes.CLOUD, corePart.getX(), corePart.getY(), corePart.getZ(), Math.sin(d * i) * 0.1D, 0.04D, Math.cos(d * i) * 0.1D);
        }
        else callOnClients(this, "CoolingEffects");
    }

    @Override
    public void handleEvent(String eventId)
    {
        switch(eventId)
        {
            case "CoolingEffects" -> playCoolingEffects();
            case "SetCooled" -> setCooled(true);
            case "ResetCooled" -> setCooled(false);
        }
    }

    public boolean stun() { return stun(DEFAULT_STUN_TIME); }

    public boolean stun(int ticks)
    {
        if(cooled || isDeadOrDying() || !isActive()) return false;
        setCooled(true);
        stunTimer = ticks;
        if(!level.isClientSide()) corePart.setOffset(CORE_Y_OFFSET_STUNNED);
        return true;
    }

    private void unStun() { if(!level.isClientSide()) corePart.setOffset(CORE_Y_OFFSET_DEFAULT); }

    public boolean isStunned()
    {
        if(level.isClientSide())
            return state == State.STUNNED;
        return stunTimer > 0;
    }

    public boolean isCooled() { return cooled || isStunned(); }

    private void setCooled(boolean value)
    {
        if(!level.isClientSide() && cooled != value)
            callOnClients(this, value ? "SetCooled" : "ResetCooled");
        cooled = value;
    }

    protected void nextPhase()
    {
        if(phase >= 2) return;
        phase++;
        shouldBeInRage = true;
    }

    public boolean shouldBeInRage() { return shouldBeInRage; }

    public boolean isVulnerableFor(@Nonnull Entity entity)
    {
        Vec3 facing = Vec3.directionFromRotation(0, yBodyRot);
        if(state == State.SHOOT) facing = facing.reverse();
        Vec3 sourceDir = position().subtract(entity.position());
        float angle = (float) Math.acos(facing.dot(sourceDir) / (facing.length() * sourceDir.length()));
        angle = Math.abs(angle * Mth.RAD_TO_DEG);
        return angle < MAX_HIT_ANGLE || (isStunned() && Math.abs(angle - 180) < MAX_HIT_ANGLE);
    }

    @Override
    public void setHealth(float value)
    {
        float halfHealth = getMaxHealth() / 2;
        if(getHealth() > halfHealth && value <= halfHealth) nextPhase();
        super.setHealth(value);
    }

    @Override
    public void aiStep()
    {
        if(state == State.DEATH)
        {
            if(dieTimer-- <= 0) setDead();
        }
        else
        {
            if(stunTimer > 0)
            {
                if(stunTimer == 1) unStun();
                stunTimer--;
            }
            super.aiStep();
        }
    }

    private void setDead()
    {
        this.remove(Entity.RemovalReason.KILLED);
        LivingEntity livingentity = this.getKillCredit();
        if (this.deathScore >= 0 && livingentity != null)
            livingentity.awardKillScore(this, this.deathScore, deadBy);
    }

    @Override
    protected void customServerAiStep()
    {
        super.customServerAiStep();
        if(state == State.INACTIVE)
        {
            if(getTarget() != null) setState(State.ACTIVATION);
            return;
        }
        else if(state == State.ACTIVATION)
        {
            if(activationTimer > 0) activationTimer--;
            else nextPhase();
            return;
        }

        if(lastTarget != getTarget())
        {
            lastTarget = getTarget();
            setState(lastTarget == null ? State.IDLE : State.WALK);
        }
        bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    public boolean causeFallDamage(float fallHeight, float mult, DamageSource damageSource) { return false; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public void push(double p_20286_, double p_20287_, double p_20288_) { }

    @Override
    public boolean isPushedByFluid() { return false; }

    @Override
    public void checkDespawn() { }

    //animations
    @Override
    public void registerControllers(AnimationData data)
    {
        data.addAnimationController(movementController);
        data.addAnimationController(coreController);
    }

    @Override
    public AnimationFactory getFactory() { return animationFactory; }

    //sounds
    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return SoundEvents.BLAZE_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.BLAZE_DEATH;
    }

    //health bar
    @Override
    public void startSeenByPlayer(@Nonnull ServerPlayer player)
    {
        super.startSeenByPlayer(player);
        setState(state);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(@Nonnull ServerPlayer player)
    {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    //save & load
    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Phase", getPhase());
        nbt.putInt("State", getState().ordinal());
        nbt.putInt("StunTime", stunTimer);
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        phase = nbt.getInt("Phase");
        setState(State.values()[nbt.getInt("State")]);
        stunTimer = nbt.getInt("StunTime");

        if (this.hasCustomName()) bossEvent.setName(getDisplayName());
    }

    //custom name
    @Override
    public void setCustomName(@Nullable Component text)
    {
        super.setCustomName(text);
        bossEvent.setName(getDisplayName());
    }

    public enum State
    {
        INACTIVE("inactive"),
        ACTIVATION("activation"),
        IDLE("idle"),
        STUNNED("stunned"),
        RAGE("rage"),
        WALK("walk"),
        RAM("run"),
        SWEEP_R("sweep_right"),
        SWEEP_L("sweep_left"),
        STOMP("stomp"),
        SMASH("smash"),
        INTERMENT("interment"),
        SHOOT("shoot"),
        DEATH("death");

        private final String animation;
        State(String animation) { this.animation = animation; }

        public boolean isSweep() { return this == SWEEP_L || this == SWEEP_R; }
        public boolean isAttack() { return this != IDLE && this != WALK && this != STUNNED && this != ACTIVATION && this != INACTIVE; }

        public String getAnimation() { return animation; }
        public static State getSweep() { return new Random().nextBoolean() ? SWEEP_L : SWEEP_R; }
    }
}
