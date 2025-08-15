// src/main/java/tech/tguentner/entity/SkelettMagierEntity.java
package tech.tguentner.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import tech.tguentner.entity.goals.SkelettMagierSpamAttackGoal;

public class SkelettMagierEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public int spamAttackCooldown = 0;
    public enum AttackState {
        NONE,
        PREPARE,
        CAST,
        FINISH
    }
    private static final TrackedData<Integer> ATTACK_STATE =
            DataTracker.registerData(SkelettMagierEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // NEU: Ein Timer für unsere Idle-Animationen
    private int idleAnimationCooldown = 0;

    public SkelettMagierEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }


    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ATTACK_STATE, AttackState.NONE.ordinal());
    }

    // NEU: Getter- und Setter-Methoden, die den DataTracker verwenden
    public AttackState getAttackState() {
        // Lese den Integer aus dem Tracker und wandle ihn zurück in den Enum-Wert
        return AttackState.values()[this.dataTracker.get(ATTACK_STATE)];
    }

    public void setAttackState(AttackState state) {
        // Schreibe den neuen Status in den Tracker. Der Server sendet das Update automatisch an die Clients.
        this.dataTracker.set(ATTACK_STATE, state.ordinal());
    }


    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0f);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this)); // Höchste Priorität, damit er nicht ertrinkt

        this.goalSelector.add(2, new SkelettMagierSpamAttackGoal(this, 1.0D, 10, 30.0f));


        this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.8D));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        // Ziel-Selektoren sind perfekt so
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, VillagerEntity.class, true));
    }

    // NEU: Die tick()-Methode, um unseren Timer herunterzuzählen
    @Override
    public void tick() {
        super.tick();

        // NEU: Zähle den Haupt-Angriffs-Cooldown hier herunter
        if (this.spamAttackCooldown > 0) {
            this.spamAttackCooldown--;
        }
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

    private PlayState bodyPredicate(AnimationState<SkelettMagierEntity> state) {
        // WICHTIG: Verwende jetzt den neuen Getter!
        if (this.getAttackState() != AttackState.NONE) {
            switch (this.getAttackState()) {
                case PREPARE:
                    state.getController().setAnimation(RawAnimation.begin().thenPlay("animation.skelett_magier.spam_prepare"));
                    break;
                case CAST:
                    state.getController().setAnimation(RawAnimation.begin().thenLoop("animation.skelett_magier.spam_cast_loop"));
                    break;
                case FINISH:
                    state.getController().setAnimation(RawAnimation.begin().thenPlay("animation.skelett_magier.spam_finish"));
                    break;
            }
            return PlayState.CONTINUE;
        }

        if (state.isMoving()) {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("animation.skelett_magier.walk"));
        } else {
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