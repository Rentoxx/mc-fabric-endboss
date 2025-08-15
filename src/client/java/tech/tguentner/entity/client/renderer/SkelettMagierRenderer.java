// In: src/client/java/tech/tguentner/entity/client/renderer/SkelettMagierRenderer.java
package tech.tguentner.entity.client.renderer; // <-- Pfad angepasst

import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import tech.tguentner.entity.SkelettMagierEntity;
import tech.tguentner.entity.model.SkelettMagierModel;

public class SkelettMagierRenderer extends GeoEntityRenderer<SkelettMagierEntity> {
    public SkelettMagierRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new SkelettMagierModel());
        this.shadowRadius = 0.4f;
    }
}