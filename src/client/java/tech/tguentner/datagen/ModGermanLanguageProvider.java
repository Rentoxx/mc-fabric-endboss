package tech.tguentner.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import tech.tguentner.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModGermanLanguageProvider extends FabricLanguageProvider {
    public ModGermanLanguageProvider(FabricDataOutput dataOutput,  CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "de_de", registryLookup);
    }


    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(ModItems.MAGISCHER_STAB, "Magischer Stab");
        translationBuilder.add("item.assasin-endboss.magischer_stab.tooltip.mode", "Modus: %s");
    }
}