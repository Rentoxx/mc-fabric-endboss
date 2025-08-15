package tech.tguentner.component;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import tech.tguentner.AssasinEndboss;

public class ModDataComponents {

    // 1. Definiere den neuen Component für unseren Modus.
    public static final ComponentType<Integer> MODE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(AssasinEndboss.MOD_ID, "mode"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT) // Sagt, wie der Wert gespeichert wird (als Integer)
                    .packetCodec(PacketCodecs.VAR_INT) // Sagt, wie er über das Netzwerk gesendet wird
                    .build()
    );

    // 2. Eine leere Methode, um die Klasse zu laden und den Component zu registrieren.
    public static void registerModDataComponents() {
        AssasinEndboss.LOGGER.info("Registering Mod Data Components for " + AssasinEndboss.MOD_ID);
    }
}