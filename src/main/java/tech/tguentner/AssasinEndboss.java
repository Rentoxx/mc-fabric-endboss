package tech.tguentner;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tguentner.component.ModDataComponents;
import tech.tguentner.entity.ModEntities;
import tech.tguentner.item.ModItems;

public class AssasinEndboss implements ModInitializer {

	public static final String MOD_ID = "assasin-endboss";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModDataComponents.registerModDataComponents();
		ModEntities.registerModEntities();
		LOGGER.info("Assasin-Endboss gestartet!");
	}
}