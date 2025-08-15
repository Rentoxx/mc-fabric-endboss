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

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModItems.registerModItems();
		ModDataComponents.registerModDataComponents();
		ModEntities.registerModEntities();
		AssasinEndboss.LOGGER.info("Item ID: {}", Registries.ITEM.getId(ModItems.MAGISCHER_STAB));
		LOGGER.info("Assasin-Endboss gestartet!");
	}
}