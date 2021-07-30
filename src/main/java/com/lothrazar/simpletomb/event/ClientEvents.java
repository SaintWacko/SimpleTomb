package com.lothrazar.simpletomb.event;

import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.data.LocationBlockPos;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.lothrazar.simpletomb.particle.ParticleGraveSmoke;
import com.lothrazar.simpletomb.particle.ParticleGraveSoul;
import com.lothrazar.simpletomb.particle.ParticleRotatingSmoke;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModTomb.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class ClientEvents {

  @SubscribeEvent(priority = EventPriority.LOW)
  public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
    ParticleEngine r = Minecraft.getInstance().particleEngine;
    r.register(TombRegistry.GRAVE_SMOKE, ParticleGraveSmoke.Factory::new);
    r.register(TombRegistry.ROTATING_SMOKE, ParticleRotatingSmoke.Factory::new);
    r.register(TombRegistry.SOUL, ParticleGraveSoul.Factory::new);
  }

  @SubscribeEvent
  public void render(RenderWorldLastEvent event) {
    LocalPlayer player = Minecraft.getInstance().player;
    if (player != null && player.level != null) {
      ItemStack stack = player.getMainHandItem();
      if (stack.getItem() == TombRegistry.GRAVE_KEY) {
        LocationBlockPos location = TombRegistry.GRAVE_KEY.getTombPos(stack);
        if (location != null && !location.isOrigin() &&
            location.dim.equalsIgnoreCase(WorldHelper.dimensionToString(player.level)) &&
            player.level.isInWorldBounds(location.toBlockPos())) {
          createBox(event.getMatrixStack(), location.x, location.y, location.z, 1.0D);
        }
      }
    }
  }

  @SuppressWarnings("deprecation")
  private static void createBox(PoseStack matrixStack, double x, double y, double z, double offset) {
    //    System.out.println("off "+z);
    offset=offset*2;
    Minecraft mc = Minecraft.getInstance();
    RenderSystem.disableTexture();
    RenderSystem.disableBlend();
    RenderSystem.disableDepthTest();
    Vec3 viewPosition = mc.gameRenderer.getMainCamera().getPosition();
    long c = (System.currentTimeMillis() / 15L) % 360L;
    float[] color = WorldHelper.getHSBtoRGBF(c / 360f, 1f, 1f);
    //    RenderSystem.pushMatrix();
    matrixStack.pushPose();
    // get a closer pos if too far
    Vec3 vec = new Vec3(x, y, z).subtract(viewPosition);
    if (vec.distanceTo(Vec3.ZERO) > 200d) { // could be 300
      vec = vec.normalize().scale(200d);
      x += vec.x;
      y += vec.y;
      z += vec.z;
    }
    x -= viewPosition.x();
    y -= viewPosition.y();
    z -= viewPosition.z();
    //    RenderSystem.multMatrix(matrixStack.last().pose()); // TODO: what is this. guess at projection
    RenderSystem.setProjectionMatrix(matrixStack.last().pose());
    Tesselator tessellator = Tesselator.getInstance();
    BufferBuilder renderer = tessellator.getBuilder();
    renderer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION); // GL11.GL_LINES
    RenderSystem.setShaderColor(color[0], color[1], color[2], 0f);
//        System.out.println("testy");
    RenderSystem.lineWidth(2.5f);
    renderer.vertex(x, y, z).endVertex();
    renderer.vertex(x + offset, y, z).endVertex();
    renderer.vertex(x, y, z).endVertex();
    renderer.vertex(x, y + offset, z).endVertex();
    renderer.vertex(x, y, z).endVertex();
    renderer.vertex(x, y, z + offset).endVertex();
    renderer.vertex(x + offset, y + offset, z + offset).endVertex();
    renderer.vertex(x, y + offset, z + offset).endVertex();
    renderer.vertex(x + offset, y + offset, z + offset).endVertex();
    renderer.vertex(x + offset, y, z + offset).endVertex();
    renderer.vertex(x + offset, y + offset, z + offset).endVertex();
    renderer.vertex(x + offset, y + offset, z).endVertex();
    renderer.vertex(x, y + offset, z).endVertex();
    renderer.vertex(x, y + offset, z + offset).endVertex();
    renderer.vertex(x, y + offset, z).endVertex();
    renderer.vertex(x + offset, y + offset, z).endVertex();
    renderer.vertex(x + offset, y, z).endVertex();
    renderer.vertex(x + offset, y, z + offset).endVertex();
    renderer.vertex(x + offset, y, z).endVertex();
    renderer.vertex(x + offset, y + offset, z).endVertex();
    renderer.vertex(x, y, z + offset).endVertex();
    renderer.vertex(x + offset, y, z + offset).endVertex();
    renderer.vertex(x, y, z + offset).endVertex();
    renderer.vertex(x, y + offset, z + offset).endVertex();
    tessellator.end();
    matrixStack.popPose();
    //    RenderSystem.popMatrix();
    RenderSystem.lineWidth(1f);
    RenderSystem.enableDepthTest();
    RenderSystem.enableBlend();
    RenderSystem.enableTexture();
    //        RenderSystem.color4f(1f, 1f, 1f, 1f);
  }
}
