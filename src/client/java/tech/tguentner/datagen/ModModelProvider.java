package tech.tguentner.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import tech.tguentner.item.ModItems;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        // Diese Methode ist für Blöcke. Da wir noch keine haben, bleibt sie leer.
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        // Diese Zeile sagt: "Erstelle für unser Item MAGISCHER_STAB ein
        // Standard-2D-Item-Model ('Models.GENERATED')."
        itemModelGenerator.register(ModItems.MAGISCHER_STAB, Models.GENERATED);
    }
}