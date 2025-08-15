// src/main/java/tech/tguentner/entity/goals/SkelettMagierSpamAttackGoal.java
package tech.tguentner.entity.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.world.World;
import tech.tguentner.entity.HomingSnowballEntity;
import tech.tguentner.entity.SkelettMagierEntity;

import java.util.EnumSet;

public class SkelettMagierSpamAttackGoal extends Goal {
    private final SkelettMagierEntity mob;
    private LivingEntity target;

    private final double mobSpeed;
    private final float attackRadius;
    private final float attackRadiusSq;

    // --- ANGEPASSTE VARIABLEN ---
    private int castCooldown;               // Zeit ZWISCHEN den einzelnen Schüssen
    private int attackDuration;             // NEU: Wie lange die gesamte Angriffsphase dauert

    private boolean isFullyFinished = false;

    public SkelettMagierSpamAttackGoal(SkelettMagierEntity mob, double mobSpeed, int attackInterval, float attackRadius) {
        this.mob = mob;
        this.mobSpeed = mobSpeed;
        // attackInterval wird jetzt nicht mehr direkt hier verwendet, aber wir lassen ihn im Konstruktor für später
        this.attackRadius = attackRadius;
        this.attackRadiusSq = attackRadius * attackRadius;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || !this.mob.getVisibilityCache().canSee(target)) {
            return false;
        }

        double distanceSq = this.mob.squaredDistanceTo(target.getX(), target.getY(), target.getZ());
        if (distanceSq > this.attackRadiusSq) {
            return false;
        }

        // PRÜFUNG GEÄNDERT: Nutze den Cooldown von der Entity
        return this.mob.spamAttackCooldown <= 0;
    }

    @Override
    public boolean shouldContinue() {

        if (this.isFullyFinished) {
            return false;
        }

        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || !this.mob.getVisibilityCache().canSee(target)) {
            return false;
        }
        return this.mob.squaredDistanceTo(target) <= this.attackRadiusSq;
    }

    @Override
    public void start() {
        this.target = this.mob.getTarget();
        if (this.target == null) return;

        this.isFullyFinished = false;

        this.mob.getNavigation().stop();
        this.mob.setAttacking(true);
        this.mob.setAttackState(SkelettMagierEntity.AttackState.PREPARE);

        this.castCooldown = 10; // 0.5s Vorbereitungszeit

        // NEU: Setze eine zufällige Angriffs-Dauer zwischen 5 und 10 Sekunden (100 - 200 Ticks)
        // 100 Ticks Basis + zufällig 0-100 Ticks extra
        this.attackDuration = 100 + this.mob.getRandom().nextInt(101);
    }

    @Override
    public void stop() {
        this.mob.setAttacking(false);
        this.mob.setAttackState(SkelettMagierEntity.AttackState.NONE);
        this.mob.spamAttackCooldown = 200; // Setzt die 10 Sekunden Cooldown
        this.mob.getNavigation().stop();
    }

    // --- KOMPLETT NEUE LOGIK ---
    @Override
    public void tick() {
        this.target = this.mob.getTarget();
        if (this.target == null) {
            this.stop();
            return;
        }
        this.mob.getLookControl().lookAt(this.target, 30.0f, 30.0f);

        // Timer für Animationen und Schüsse herunterzählen
        if (this.castCooldown > 0) {
            this.castCooldown--;
        }

        // Unsere neue Zustands-Maschine
        switch (this.mob.getAttackState()) {
            case PREPARE:
                // Wenn die Vorbereitungs-Animation vorbei ist, gehe zum Zaubern über
                if (this.castCooldown <= 0) {
                    this.mob.setAttackState(SkelettMagierEntity.AttackState.CAST);
                }
                break;

            case CAST:
                // Wenn die Angriffs-Dauer abgelaufen ist, gehe zur Abschluss-Animation
                if (this.attackDuration <= 0) {
                    this.mob.setAttackState(SkelettMagierEntity.AttackState.FINISH);
                    this.castCooldown = 10; // 0.5s für die Abschluss-Animation
                } else {
                    this.attackDuration--; // Zähle die Dauer nur im CAST-Modus herunter
                    // Wenn der Schuss-Cooldown abgelaufen ist, feuern!
                    if (this.castCooldown <= 0) {
                        spawnProjectile();
                        this.castCooldown = 15; // 0.75 Sekunden bis zum nächsten Schuss
                    }
                }
                break;

            case FINISH:
                // Wenn die Abschluss-Animation vorbei ist, beende das Goal (löst stop() aus)
                if (this.castCooldown <= 0) {
                    this.isFullyFinished = true;
                }
                break;

            case NONE:
                // Sollte nicht passieren, aber als Absicherung
                this.stop();
                break;
        }
    }

    private void spawnProjectile() {
        if (this.target == null) return;
        World world = this.mob.getWorld();
        if (world.isClient()) return;

        HomingSnowballEntity snowball = new HomingSnowballEntity(world, this.mob);
        double dX = this.target.getX() - this.mob.getX();
        double dY = this.target.getBodyY(0.5) - snowball.getY();
        double dZ = this.target.getZ() - this.mob.getZ();

        snowball.setVelocity(dX, dY, dZ, 1.5f, 1.0f);
        world.spawnEntity(snowball);
    }
}