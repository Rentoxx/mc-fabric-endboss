package tech.tguentner;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import tech.tguentner.entity.ModEntities;

public class AssasinEndbossClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.HOMING_SNOWBALL, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.ORBITING_PROJECTILE, FlyingItemEntityRenderer::new);

	}
}