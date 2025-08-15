// src/main/java/tech/tguentner/entity/model/SkelettMagierModel.java
package tech.tguentner.entity.model;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import tech.tguentner.AssasinEndboss;
import tech.tguentner.entity.SkelettMagierEntity;

public class SkelettMagierModel extends GeoModel<SkelettMagierEntity> {

    @Override
    public Identifier getModelResource(SkelettMagierEntity animatable) {
        // Verweist auf die .geo.json Datei
        return Identifier.of(AssasinEndboss.MOD_ID, "geo/skelett_magier.geo.json");
    }

    @Override
    public Identifier getTextureResource(SkelettMagierEntity animatable) {
        // Verweist auf die .png Datei
        return Identifier.of(AssasinEndboss.MOD_ID, "textures/entity/skelett_magier.png");
    }

    @Override
    public Identifier getAnimationResource(SkelettMagierEntity animatable) {
        // Verweist auf die .animation.json Datei
        return Identifier.of(AssasinEndboss.MOD_ID, "animations/skelett_magier.animation.json");
    }
}