package tech.tguentner.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;
import net.minecraft.particle.DustParticleEffect;

import java.util.List;

public class HomingSnowballEntity extends SnowballEntity {

    public HomingSnowballEntity(EntityType<? extends HomingSnowballEntity> entityType, World world) {
        super(entityType, world);
    }

    // 2. Helfer-Konstruktor (korrigiert)
    public HomingSnowballEntity(World world, LivingEntity owner) {
        this(ModEntities.HOMING_SNOWBALL, world); // KORREKT: Ruft den Hauptkonstruktor auf
        this.setOwner(owner);
        this.setPosition(owner.getEyePos());
    }


    @Override
    public void tick() {
        // ############ BEGINN DES TICK-TESTS ############
        if (this.getWorld().isClient) {
            // Nur noch einen Partikel pro Tick für eine saubere Spur
            Vec3d center = this.getPos();
            Vector3f particleColor = new Vector3f(0.8f, 0.2f, 1.0f); // Lila/Pink

            this.getWorld().addParticle(
                    new DustParticleEffect(particleColor, 0.8f), // Größe etwas reduziert
                    center.getX(), center.getY(), center.getZ(),
                    0.0, 0.0, 0.0 // Keine eigene Geschwindigkeit, der Partikel bleibt wo er ist
            );
        }
        // ############ ENDE DES TICK-TESTS ############


        // Die ursprüngliche tick()-Methode und Server-Logik
        super.tick();
        if (!this.getWorld().isClient()) {
            LivingEntity target = findClosestTarget(10.0);
            if (target != null) {
                Vec3d currentVelocity = this.getVelocity();
                Vec3d directionToTarget = target.getEyePos().subtract(this.getPos()).normalize();

                // ############ BEGINN DES HOMING-UPDATES ############

                // Die Geschwindigkeit, die der Schneeball während der Zielsuche haben soll.
                // Pass diesen Wert an, wie schnell es sein soll. 0.9 ist ein guter Start.
                double homingSpeed = 0.9;
                Vec3d idealVelocity = directionToTarget.multiply(homingSpeed);

                // Der "Lerp"-Faktor. Höher = stärkere Kurve. 0.2 ist ein guter Mittelweg.
                double lerpFactor = 0.2;
                Vec3d newVelocity = currentVelocity.lerp(idealVelocity, lerpFactor);

                // ############ ENDE DES HOMING-UPDATES ############

                this.setVelocity(newVelocity);
            }
        }
    }

    private LivingEntity findClosestTarget(double searchRadius) {
        World world = this.getWorld();
        Entity owner = this.getOwner();
        Box searchBox = this.getBoundingBox().expand(searchRadius);
        List<LivingEntity> potentialTargets = world.getNonSpectatingEntities(LivingEntity.class, searchBox);
        LivingEntity closestTarget = null;
        double minDistanceSq = Double.MAX_VALUE;
        for (LivingEntity potentialTarget : potentialTargets) {
            if (potentialTarget.isAlive() && !potentialTarget.equals(owner)) {
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