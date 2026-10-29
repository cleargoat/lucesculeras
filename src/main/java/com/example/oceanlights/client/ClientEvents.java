package com.example.oceanlights.client;

import com.example.oceanlights.OceanLights;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = OceanLights.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    /**
     * /oceanlights <color>        yellow, orange, red, blue, pink, ... o hex de 6 digitos (ej. ff8800)
     * /oceanlights rainbow        arcoiris
     * /oceanlights off            apaga la luz
     * /oceanlights intensity <v>  0.1 - 4
     */
    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        List<String> names = new ArrayList<>(LightState.NAMES.keySet());
        names.add("rainbow");
        names.add("arcoiris");

        d.register(Commands.literal("oceanlights")
                .then(Commands.literal("off").executes(c -> off()))
                .then(Commands.literal("intensity")
                        .then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 4f))
                                .executes(c -> intensity(FloatArgumentType.getFloat(c, "valor")))))
                .then(Commands.argument("color", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(names, builder))
                        .executes(ClientEvents::color)));
    }

    private static void msg(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.sendSystemMessage(Component.literal(text));
    }

    private static int color(CommandContext<CommandSourceStack> ctx) {
        String text = StringArgumentType.getString(ctx, "color");
        if (LightState.setColor(text)) {
            msg("Ocean Lights: " + text);
            return 1;
        }
        msg("Color no valido. Prueba: yellow, orange, red, blue, pink, white, green, cyan, purple, rainbow o un hex como ff8800");
        return 0;
    }

    private static int off() {
        LightState.setEnabled(false);
        msg("Ocean Lights: apagado");
        return 1;
    }

    private static int intensity(float v) {
        LightState.setIntensity(v);
        msg("Ocean Lights: intensidad " + v);
        return 1;
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || !LightState.isEnabled()) return;
        if (Minecraft.getInstance().level == null) return;
        float time = (event.getRenderTick() + event.getPartialTick()) / 20f;
        LightRenderer.render(event.getProjectionMatrix(), time);
    }
}
