// src/main/java/tech/tguentner/entity/SkelettMagierEntity.java
package tech.tguentner.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SkelettMagierEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // NEU: Ein Timer für unsere Idle-Animationen
    private int idleAnimationCooldown = 0;

    public SkelettMagierEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0f);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 1.0));
        // TODO: Später eine Angriffs-KI hinzufügen

        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    // NEU: Die tick()-Methode, um unseren Timer herunterzuzählen
    @Override
    public void tick() {
        super.tick();
        // Wir zählen den Cooldown nur auf der Client-Seite herunter, wo Animationen stattfinden.
        if (this.getWorld().isClient()) {
            if (this.idleAnimationCooldown > 0) {
                this.idleAnimationCooldown--;
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        // Controller 1: Verantwortlich für die Haupt-Körperanimationen (Gehen, Atmen)
        controllerRegistrar.add(new AnimationController<>(this, "body_controller", 0, this::bodyPredicate));

        // Controller 2: Verantwortlich NUR für die Kopf-Animationen
        controllerRegistrar.add(new AnimationController<>(this, "head_controller", 0, this::headPredicate));
    }

    // Predicate 1: Steuert nur die Körperanimationen
    private PlayState bodyPredicate(AnimationState<SkelettMagierEntity> state) {
        // Wenn der Mob sich bewegt, spiele die "walk"-Animation.
        if (state.isMoving()) {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("animation.skelett_magier.walk"));
        }
        // Ansonsten spiele die "idle" (Atmungs)-Animation.
        else {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("animation.skelett_magier.idle"));
        }
        return PlayState.CONTINUE;
    }

// src/main/java/tech/tguentner/entity/SkelettMagierEntity.java

    // Predicate 2: Steuert nur die Kopf-Animationen (FINALE, GETUNTE VERSION)
    private PlayState headPredicate(AnimationState<SkelettMagierEntity> state) {
        if (!state.isMoving()) {
            // Wenn eine Animation auf diesem Controller bereits läuft, lass sie zu Ende spielen.
            if (state.getController().getAnimationState() != AnimationController.State.STOPPED) {
                return PlayState.CONTINUE;
            }

            // Wenn der Cooldown noch läuft, machen wir nichts NEUES.
            if (this.idleAnimationCooldown > 0) {
                return PlayState.CONTINUE;
            }

            // Setze den Cooldown auf eine neue, längere Zufallszeit (z.B. zwischen 3 und 8 Sekunden).
            // 60 Ticks = 3 Sekunden. nextInt(100) = 0-99 Ticks (bis zu 5 Sek).
            // Ergebnis: Eine Pause von 3 bis 8 Sekunden.
            this.idleAnimationCooldown = this.random.nextInt(60) + 20;

            // Wähle zufällig eine Animation zum Abspielen aus.
            // Wir erhöhen die Zahl in nextInt(), um die Wahrscheinlichkeit für "Nichts tun" zu erhöhen.
            // nextInt(6) gibt uns Zahlen von 0-5. Nur bei 0 und 1 wird eine Animation gespielt.
            // Das ist eine 2/6 (oder 1/3) Chance für eine Aktion.
            switch (this.random.nextInt(3)) {
                case 0:
                    return state.setAndContinue(RawAnimation.begin().thenPlay("animation.skelett_magier.look_around"));
                case 1:
                    return state.setAndContinue(RawAnimation.begin().thenPlay("animation.skelett_magier.look_around_fast"));
                default: // Fälle 2, 3, 4, 5
                    // Tue nichts. Erzeugt längere, natürlichere Pausen.
                    return PlayState.CONTINUE;
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}