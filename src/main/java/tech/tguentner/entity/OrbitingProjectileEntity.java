package tech.tguentner.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f; // Wichtig: Der richtige Import
import net.minecraft.particle.ParticleTypes;

import java.util.List;

public class OrbitingProjectileEntity extends SnowballEntity {

    enum State {
        INACTIVE,
        ARMED,
        FIRING
    }


    // 1. Definiere die Daten, die getrackt werden sollen.
    // Wir benutzen Vector3f, da Vec3d nicht direkt getrackt werden kann.
    private static final TrackedData<Vector3f> ORBIT_OFFSET = DataTracker.registerData(OrbitingProjectileEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
    private static final TrackedData<Integer> STATE = DataTracker.registerData(OrbitingProjectileEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private int lifeTicks = 200;

    private int inactiveTicks = 100;

    // Das private Feld 'orbitOffset' wird nicht mehr benötigt.

    public OrbitingProjectileEntity(EntityType<? extends SnowballEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    public OrbitingProjectileEntity(World world, LivingEntity owner, Vec3d offset) {
        this(ModEntities.ORBITING_PROJECTILE, world);
        this.setOwner(owner);
        // 3. Setze den Wert im DataTracker, anstatt das private Feld zu verwenden.
        this.setOrbitOffset(offset);
        this.setState(State.INACTIVE);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ORBIT_OFFSET, new Vector3f(0.0f, 0.0f, 0.0f));
        builder.add(STATE, State.INACTIVE.ordinal()); // Zustand standardmäßig auf INACTIVE setzen
    }

    // 4. Helfer-Methoden zum Setzen und Holen des Werts (mit Typ-Umwandlung)
    public void setOrbitOffset(Vec3d offset) {
        this.dataTracker.set(ORBIT_OFFSET, offset.toVector3f());
    }

    public Vec3d getOrbitOffset() {
        return new Vec3d(this.dataTracker.get(ORBIT_OFFSET));
    }

    @Override
    public void tick() {
        super.tick();
        Entity ownerAsEntity = this.getOwner();

        if (ownerAsEntity == null || !ownerAsEntity.isAlive() || this.lifeTicks-- <= 0) {
            this.discard();
            return;
        }

        // Zustand wechseln
        if (this.getState() == State.INACTIVE && this.inactiveTicks > 0) {
            this.inactiveTicks--;
            if (this.inactiveTicks == 0) {
                this.setState(State.ARMED);
            }
        }


        // --- START VON SCHRITT 4 ---
        // Zielsuche UND Abfeuern
        if (this.getState() == State.ARMED) {
            if (this.age % 10 == 0) {
                LivingEntity target = findClosestTarget(25.0);
                if (target != null) {
                    // ZIEL GEFUNDEN!
                    System.out.println("SCHRITT 4 ERFOLGREICH: Feuere auf " + target.getName().getString());

                    // 1. Richtung zur Position des Ziels berechnen und Geschwindigkeit setzen
                    Vec3d direction = target.getEyePos().subtract(this.getPos()).normalize();
                    this.setVelocity(direction.multiply(1.5)); // 1.5 ist eine gute Schneeball-Geschwindigkeit

                    // 2. Zustand auf FIRING setzen, damit es nicht nochmal schießt
                    this.setState(State.FIRING);
                }
            }
        }

        // Die "Entriegelung": Die Schwebe-Logik wird nur ausgeführt, wenn wir NICHT feuern.
        if (this.getState() != State.FIRING) {
            LivingEntity owner = (LivingEntity) ownerAsEntity;

            if (getWorld().isClient) {
                if (this.getState() == State.INACTIVE) {
                    getWorld().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
                } else { // ARMED
                    getWorld().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
                }
            }

            Vec3d rotatedOffset = this.getOrbitOffset().rotateY((float) -Math.toRadians(owner.getYaw()));
            Vec3d targetPosition = owner.getEyePos().add(rotatedOffset);

            this.setPosition(targetPosition);
            this.setVelocity(Vec3d.ZERO);
        }
    }

    // Bonus: Speichern und Laden, falls die Entität mal länger existiert (Chunk unload/load)
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        Vec3d offset = getOrbitOffset();
        nbt.putDouble("OffsetX", offset.x);
        nbt.putDouble("OffsetY", offset.y);
        nbt.putDouble("OffsetZ", offset.z);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("OffsetX")) {
            Vec3d offset = new Vec3d(nbt.getDouble("OffsetX"), nbt.getDouble("OffsetY"), nbt.getDouble("OffsetZ"));
            setOrbitOffset(offset);
        }
    }

    @Override
    public boolean canHit(Entity entity) {
        return this.getState() == State.FIRING && !entity.equals(this.getOwner());
    }

    @Override
    protected void onEntityHit(net.minecraft.util.hit.EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult); // Wichtig: Führt die Standard-Logik aus (z.B. Knockback)

        Entity target = entityHitResult.getEntity(); // Das getroffene Ziel
        Entity owner = this.getOwner();               // Der Spieler, der geschossen hat
        float damageAmount = 4.0f;                    // Wie viel Schaden es machen soll (2 Herzen)

        // Erstellt eine Schadensquelle, die vom Projektil und dem Besitzer ausgeht
        net.minecraft.entity.damage.DamageSource damageSource = this.getDamageSources().thrown(this, owner);

        // Verursacht den Schaden am Ziel
        target.damage(damageSource, damageAmount);
    }

    private void setState(State state) {
        this.dataTracker.set(STATE, state.ordinal());
    }

    private State getState() {
        return State.values()[this.dataTracker.get(STATE)];
    }


    private LivingEntity findClosestTarget(double radius) {
        World world = this.getWorld();
        Entity owner = this.getOwner();
        Box searchBox = this.getBoundingBox().expand(radius);

        // Schritt 1: Hole alle LivingEntities in der Box, ohne Filter.
        List<LivingEntity> allEntitiesInBox = world.getNonSpectatingEntities(LivingEntity.class, searchBox);

        LivingEntity closestTarget = null;
        double minDistanceSq = Double.MAX_VALUE;

        // Schritt 2: Gehe die Liste mit einer for-Schleife durch.
        for (LivingEntity potentialTarget : allEntitiesInBox) {

            // Schritt 3: Wende die Filter als if-Bedingung an.
            // Dies ersetzt den Lambda-Ausdruck.
            if (!potentialTarget.equals(owner) && potentialTarget.isAlive()) {

                // Schritt 4: Führe die Distanzberechnung nur für gültige Ziele aus.
                double distanceSq = this.squaredDistanceTo(potentialTarget);
                if (distanceSq < minDistanceSq) {
                    minDistanceSq = distanceSq;
                    closestTarget = potentialTarget;
                }
            }
        }
        return closestTarget;
    }

}