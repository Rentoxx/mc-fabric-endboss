// src/main/java/tech/tguentner/entity/SkelettMagierEntity.java
package tech.tguentner.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SkelettMagierEntity extends HostileEntity implements GeoEntity {
    // Dieser Cache ist notwendig, damit GeckoLib funktioniert.
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SkelettMagierEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    // Hier registrieren wir die Attribute des Mobs (Leben, Geschwindigkeit, etc.)
    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0f);
    }

    // Hier definieren wir die KI (Goals) des Mobs.
    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 1.0));
        // TODO: Später eine Angriffs-KI hinzufügen

        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    // --- GECKOLIB METHODEN ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        // Wir erstellen einen Controller und lassen Java den Typ (<>) selbst herausfinden.
        // Da "this" ein SkelettMagierEntity ist, wird der Controller korrekt typisiert.
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    // Die Signatur der Methode kann jetzt auch vereinfacht werden.
    private PlayState predicate(AnimationState<SkelettMagierEntity> state) {
        // Für den Anfang spielen wir einfach eine "idle" Animation in einer Schleife ab.
        // Ändere "animation.skelett_magier.idle" zu dem Namen deiner Idle-Animation in Blockbench.
        state.getController().setAnimation(RawAnimation.begin().thenLoop("animation.skelett_magier.walk"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}