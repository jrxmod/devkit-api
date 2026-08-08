package io.github.jrxmod.devkit.core;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public final class DevkitCommands {
    private DevkitCommands() {}

    public static void register(String modId, Consumer<LiteralArgumentBuilder<ServerCommandSource>> configurator) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> root = LiteralArgumentBuilder.literal(modId);
            configurator.accept(root);
            dispatcher.register(root);
        });
    }

    public static ServerPlayerEntity player(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return ctx.getSource().getPlayerOrThrow();
    }

    public static int ok(CommandContext<ServerCommandSource> ctx, String message) {
        ctx.getSource().sendFeedback(() -> Text.literal(message), false);
        return 1;
    }

    public static int okBroadcast(CommandContext<ServerCommandSource> ctx, String message) {
        ctx.getSource().sendFeedback(() -> Text.literal(message), true);
        return 1;
    }
}
