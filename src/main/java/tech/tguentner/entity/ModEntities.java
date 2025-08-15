package tech.tguentner.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import tech.tguentner.AssasinEndboss;

public class ModEntities {

    // 1.21.0: kein RegistryKey nötig
    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String id, EntityType.Builder<T> builder) {
        Identifier identifier = Identifier.of(AssasinEndboss.MOD_ID, id);
        // In manchen Mappings heißt es builder.build(id). Falls build() ohne Param nicht existiert, nimm builder.build(id).
        return Registry.register(Registries.ENTITY_TYPE, identifier, builder.build());
    }

    public static final EntityType<HomingSnowballEntity> HOMING_SNOWBALL = register("homing_snowball",
            EntityType.Builder.<HomingSnowballEntity>create(HomingSnowballEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25F, 0.25F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(1)
    );

    public static final EntityType<OrbitingProjectileEntity> ORBITING_PROJECTILE = register("orbiting_projectile",
            EntityType.Builder.<OrbitingProjectileEntity>create(OrbitingProjectileEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25F, 0.25F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(1)
                    .disableSaving()
    );

    public static final EntityType<SkelettMagierEntity> SKELETT_MAGIER = register("skelett_magier",
            EntityType.Builder.create(SkelettMagierEntity::new, SpawnGroup.MONSTER) // MONSTER, da es ein feindlicher Mob ist
                    .dimensions(0.6f, 1.2f) // Typische Humanoiden-Größe
    );

    public static void registerModEntities() {
        AssasinEndboss.LOGGER.info("Registering Mod Entities for " + AssasinEndboss.MOD_ID);
        FabricDefaultAttributeRegistry.register(SKELETT_MAGIER, SkelettMagierEntity.setAttributes());

    }
}