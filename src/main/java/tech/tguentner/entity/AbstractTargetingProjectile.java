// src/main/java/tech/tguentner/entity/AbstractTargetingProjectile.java
package tech.tguentner.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

/**
 * Eine Basisklasse für Projektile, die ein Ziel finden können.
 * Enthält die gemeinsame Logik für die Zielsuche.
 */
public abstract class AbstractTargetingProjectile extends SnowballEntity {

    protected AbstractTargetingProjectile(EntityType<? extends SnowballEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Findet die nächste lebende Entität (außer dem Besitzer) in einem bestimmten Radius.
     * @param searchRadius Der Radius, in dem gesucht wird.
     * @return Die nächste Entität oder null, wenn keine gefunden wurde.
     */
    protected LivingEntity findClosestTarget(double searchRadius) {
        World world = this.getWorld();
        Entity owner = this.getOwner();
        // Erweitere die BoundingBox um den Suchradius in alle Richtungen
        Box searchBox = this.getBoundingBox().expand(searchRadius);

        // Hole alle potenziellen Ziele im Suchbereich
        List<LivingEntity> potentialTargets = world.getNonSpectatingEntities(LivingEntity.class, searchBox);

        LivingEntity closestTarget = null;
        double minDistanceSq = Double.MAX_VALUE; // Beginne mit einer unendlich großen Distanz

        for (LivingEntity potentialTarget : potentialTargets) {
            // Überspringe das Ziel, wenn es der Besitzer ist oder nicht am Leben ist
            if (!potentialTarget.isAlive() || potentialTarget.equals(owner)) {
                continue;
            }

            double distanceSq = this.squaredDistanceTo(potentialTarget);
            if (distanceSq < minDistanceSq) {
                minDistanceSq = distanceSq;
                closestTarget = potentialTarget;
            }
        }
        return closestTarget;
    }
}