package com.example.oceanlights.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

/** Dibuja la luz de color sobre el terreno ya renderizado, usando la profundidad de la pantalla. */
public final class LightRenderer {
    private static final float RANGE = 32f;
    private static final float COS_INNER = (float) Math.cos(Math.toRadians(14));
    private static final float COS_OUTER = (float) Math.cos(Math.toRadians(42));

    private LightRenderer() {
    }

    public static void render(Matrix4f proj, float timeSeconds) {
        ShaderInstance sh = ClientSetup.lightShader;
        if (sh == null || !LightState.isEnabled()) return;

        RenderTarget rt = Minecraft.getInstance().getMainRenderTarget();
        float[] c = LightState.currentColor(timeSeconds);

        sh.setSampler("DepthSampler", rt.getDepthTextureId());
        sh.safeGetUniform("InvProj").set(new Matrix4f(proj).invert());
        sh.safeGetUniform("Proj").set(proj);
        // Luz un poco a la derecha y abajo de la camara, como si la llevaras en la mano
        sh.safeGetUniform("LightPos").set(0.35f, -0.30f, -0.20f);
        sh.safeGetUniform("LightColor").set(c[0], c[1], c[2], LightState.getIntensity());
        sh.safeGetUniform("LightParams").set(RANGE, COS_INNER, COS_OUTER, 0f);

        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(() -> sh);

        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        b.vertex(-1, -1, 0).endVertex();
        b.vertex(1, -1, 0).endVertex();
        b.vertex(1, 1, 0).endVertex();
        b.vertex(-1, 1, 0).endVertex();
        BufferUploader.drawWithShader(b.end());

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }
}
