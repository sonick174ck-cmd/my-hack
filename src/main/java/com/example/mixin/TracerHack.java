package com.example.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(net.minecraft.client.render.WorldRenderer.class)
public class TracerHack {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderWorld(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, net.minecraft.client.render.Camera camera, net.minecraft.client.render.GameRenderer gameRenderer, net.minecraft.client.render.LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        Vec3d camPos = camera.getPos();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShader(net.minecraft.client.render.GameRenderer::getPositionColorProgram);
        net.minecraft.client.render.BufferBuilder buffer = net.minecraft.client.render.Tessellator.getInstance().getBuffer();
        buffer.begin(net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINES, net.minecraft.client.render.VertexFormats.POSITION_COLOR);
        for (PlayerEntity target : client.world.getPlayers()) {
            if (target != client.player) {
                double x = target.getX() - camPos.x;
                double y = target.getY() - camPos.y + 1.0;
                double z = target.getZ() - camPos.z;
                buffer.vertex(matrices.peek().getPositionMatrix(), 0, 0, 0).color(255, 0, 0, 255).next();
                buffer.vertex(matrices.peek().getPositionMatrix(), (float)x, (float)y, (float)z).color(255, 0, 0, 255).next();
            }
        }
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
    }
}
