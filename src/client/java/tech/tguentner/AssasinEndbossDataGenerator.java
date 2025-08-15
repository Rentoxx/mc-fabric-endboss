package tech.tguentner;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import tech.tguentner.datagen.ModEnglishLanguageProvider;
import tech.tguentner.datagen.ModGermanLanguageProvider;
import tech.tguentner.datagen.ModModelProvider;

public class AssasinEndbossDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(ModGermanLanguageProvider::new);
		pack.addProvider(ModEnglishLanguageProvider::new);
	}
}
