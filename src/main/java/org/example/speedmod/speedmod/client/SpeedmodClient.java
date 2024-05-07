package org.example.speedmod.speedmod.client;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.entity.attribute.EntityAttributes;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Objects;

public class SpeedmodClient implements ClientModInitializer {
    private float scaledSpeed = 0.1f; // Default speed
    private boolean speedChanged = false; // Flag to check if speed was changed

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(ClientCommandManager.literal("speed")
                .then(ClientCommandManager.argument("speed", FloatArgumentType.floatArg(1, 20))
                        .executes(this::executeSpeed))
                .then(ClientCommandManager.literal("reset")
                        .executes(this::resetSpeed))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (MinecraftClient.getInstance().player != null && speedChanged) {
                // Continuously set the player's speed
                Objects.requireNonNull(MinecraftClient.getInstance().player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(scaledSpeed);
            }
        });
    }

    private int executeSpeed(CommandContext<FabricClientCommandSource> context) {
        float newSpeed = FloatArgumentType.getFloat(context, "speed");
        assert MinecraftClient.getInstance().player != null;
        System.out.println("New speed: " + newSpeed); // Check if the new speed value is correct
        // Scale the input speed to fit within the valid range (0.05 - 1.0)
        scaledSpeed = newSpeed / 20.0f * 0.95f + 0.05f;
        System.out.println("Scaled speed: " + scaledSpeed); // Check if the scaled speed value is correct

        // Set the player's movement speed attributes
        Objects.requireNonNull(MinecraftClient.getInstance().player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(scaledSpeed);

        MinecraftClient.getInstance().player.sendMessage(Text.of("§8[§2SpeedMod§8] §7Walking and swimming speed has been set to §a" + newSpeed), false);
        speedChanged = true;
        return 1;
    }

    private int resetSpeed(CommandContext<FabricClientCommandSource> context) {
        resetSpeedToDefault();
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(Text.of("§8[§2SpeedMod§8] §7Walking and swimming speed has been reset"), false);
        return 1;
    }

    private void resetSpeedToDefault() {
        scaledSpeed = 0.1f; // Reset to default speed
        speedChanged = false; // Stop continuously changing the speed
    }
}
