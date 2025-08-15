package tech.tguentner.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

// ERBT JETZT VON UNSERER NEUEN KLASSE
public class OrbitingProjectileEntity extends AbstractTargetingProjectile {

    private enum State {
        INACTIVE,
        ARMED,
        FIRING
    }

    private static final TrackedData<Vector3f> ORBIT_OFFSET = DataTracker.registerData(OrbitingProjectileEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
    private static final TrackedData<Integer> STATE = DataTracker.registerData(OrbitingProjectileEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // Konstanten machen den Code lesbarer
    private static final int MAX_LIFETIME_TICKS = 200;
    private static final int INACTIVE_DURATION_TICKS = 100;

    private int lifeTicks = MAX_LIFETIME_TICKS;
    private int inactiveTicks = INACTIVE_DURATION_TICKS;

    public OrbitingProjectileEntity(EntityType<? extends OrbitingProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    public OrbitingProjectileEntity(World world, LivingEntity owner, Vec3d offset) {
        this(ModEntities.ORBITING_PROJECTILE, world);
        this.setOwner(owner);
        this.setOrbitOffset(offset);
        this.setState(State.INACTIVE);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ORBIT_OFFSET, new Vector3f(0.0f, 0.0f, 0.0f));
        builder.add(STATE, State.INACTIVE.ordinal());
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = this.getOwner();

        if (owner == null || !owner.isAlive() || this.lifeTicks-- <= 0) {
            this.discard();
            return;
        }

        // Führe die Logik für den aktuellen Zustand aus
        switch (this.getState()) {
            case INACTIVE -> tickInactiveState();
            case ARMED -> tickArmedState((LivingEntity) owner);
            case FIRING -> tickFiringState();
        }
    }

    private void tickInactiveState() {
        if (this.inactiveTicks-- <= 0) {
            this.setState(State.ARMED);
        }
        updateOrbitPosition((LivingEntity) this.getOwner());
        spawnParticles();
    }

    private void tickArmedState(LivingEntity owner) {
        // Alle 10 Ticks nach einem Ziel suchen
        if (this.age % 10 == 0) {
            // Die Methode kommt jetzt von der Oberklasse!
            LivingEntity target = findClosestTarget(25.0);
            if (target != null) {
                // Ziel gefunden -> Zustand wechseln und abfeuern
                Vec3d direction = target.getEyePos().subtract(this.getPos()).normalize();
                this.setVelocity(direction.multiply(1.5));
                this.setState(State.FIRING);
                return; // Beende die Methode hier, um nicht die Orbit-Position zu überschreiben
            }
        }
        updateOrbitPosition(owner);
        spawnParticles();
    }

    private void tickFiringState() {
        // In diesem Zustand fliegt das Projektil nur noch.
        // Die `super.tick()` kümmert sich um die Bewegung basierend auf der gesetzten Velocity.
        // Keine weitere Logik nötig.
    }

    private void updateOrbitPosition(LivingEntity owner) {
        Vec3d rotatedOffset = this.getOrbitOffset().rotateY((float) -Math.toRadians(owner.getYaw()));
        Vec3d targetPosition = owner.getEyePos().add(rotatedOffset);
        this.setPosition(targetPosition);
        this.setVelocity(Vec3d.ZERO);
    }

    private void spawnParticles() {
        if (getWorld().isClient) {
            if (this.getState() == State.INACTIVE) {
                getWorld().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            } else { // ARMED
                getWorld().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity target = entityHitResult.getEntity();
        Entity owner = this.getOwner();
        float damageAmount = 4.0f;
        DamageSource damageSource = this.getDamageSources().thrown(this, owner);
        target.damage(damageSource, damageAmount);
    }

    @Override
    public boolean canHit(Entity entity) {
        // Kann nur treffen, wenn es abgefeuert wurde und das Ziel nicht der Besitzer ist
        return this.getState() == State.FIRING && !entity.equals(this.getOwner());
    }

    // --- DataTracker Helper ---
    public void setOrbitOffset(Vec3d offset) {
        this.dataTracker.set(ORBIT_OFFSET, offset.toVector3f());
    }

    public Vec3d getOrbitOffset() {
        return new Vec3d(this.dataTracker.get(ORBIT_OFFSET));
    }

    private void setState(State state) {
        this.dataTracker.set(STATE, state.ordinal());
    }

    private State getState() {
        return State.values()[this.dataTracker.get(STATE)];
    }

    // Die findClosestTarget-Methode wird komplett entfernt (geerbt).
    // Die NBT-Methoden werden entfernt (wegen .disableSaving()).
}