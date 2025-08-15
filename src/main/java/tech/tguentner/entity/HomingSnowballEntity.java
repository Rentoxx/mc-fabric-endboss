package tech.tguentner.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

// ERBT JETZT VON UNSERER NEUEN KLASSE
public class HomingSnowballEntity extends AbstractTargetingProjectile {

    public HomingSnowballEntity(EntityType<? extends HomingSnowballEntity> entityType, World world) {
        super(entityType, world);
    }

    public HomingSnowballEntity(World world, LivingEntity owner) {
        this(ModEntities.HOMING_SNOWBALL, world);
        this.setOwner(owner);
        this.setPosition(owner.getEyePos());
    }

    @Override
    public void tick() {
        super.tick();

        // Client-seitige Partikel bleiben unverändert gut
        if (this.getWorld().isClient) {
            Vec3d center = this.getPos();
            Vector3f particleColor = new Vector3f(0.8f, 0.2f, 1.0f); // Lila/Pink
            this.getWorld().addParticle(
                    new DustParticleEffect(particleColor, 0.8f),
                    center.getX(), center.getY(), center.getZ(),
                    0.0, 0.0, 0.0
            );
        }

        // Server-seitige Zielsuch-Logik
        if (!this.getWorld().isClient()) {
            // Die Methode kommt jetzt von der Oberklasse!
            LivingEntity target = findClosestTarget(10.0);
            if (target != null) {
                Vec3d currentVelocity = this.getVelocity();
                Vec3d directionToTarget = target.getEyePos().subtract(this.getPos()).normalize();

                double homingSpeed = 0.9;
                Vec3d idealVelocity = directionToTarget.multiply(homingSpeed);

                double lerpFactor = 0.2;
                Vec3d newVelocity = currentVelocity.lerp(idealVelocity, lerpFactor);

                this.setVelocity(newVelocity);
            }
        }
    }

    // Die findClosestTarget-Methode wird komplett entfernt, da sie geerbt wird.
}