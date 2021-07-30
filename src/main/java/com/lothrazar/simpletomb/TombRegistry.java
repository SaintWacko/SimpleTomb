package com.lothrazar.simpletomb;

import com.lothrazar.simpletomb.block.BlockTomb;
import com.lothrazar.simpletomb.block.ModelTomb;
import com.lothrazar.simpletomb.block.TileEntityTomb;
import com.lothrazar.simpletomb.item.GraveKeyItem;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TombRegistry {

  public static final SimpleParticleType GRAVE_SMOKE = new SimpleParticleType(false);
  public static final SimpleParticleType ROTATING_SMOKE = new SimpleParticleType(false);
  public static final SimpleParticleType SOUL = new SimpleParticleType(false);
  @ObjectHolder(ModTomb.MODID + ":tombstone")
  public static BlockEntityType<TileEntityTomb> TOMBSTONETILEENTITY;
  @ObjectHolder(ModTomb.MODID + ":grave_key")
  public static GraveKeyItem GRAVE_KEY;
  //four blocks
  @ObjectHolder(ModTomb.MODID + ":grave_cross")
  public static BlockTomb GRAVE_CROSS;
  @ObjectHolder(ModTomb.MODID + ":grave_normal")
  public static BlockTomb GRAVE_NORMAL;
  @ObjectHolder(ModTomb.MODID + ":tombstone")
  public static BlockTomb TOMBSTONE;
  @ObjectHolder(ModTomb.MODID + ":grave_simple")
  public static BlockTomb GRAVE_SIMPLE;

  @SubscribeEvent
  public static void registerBlocks(Register<Block> event) {
    IForgeRegistry<Block> r = event.getRegistry();
    r.register(new BlockTomb(Block.Properties.of(Material.STONE), ModelTomb.GRAVE_SIMPLE).setRegistryName("grave_simple"));
    r.register(new BlockTomb(Block.Properties.of(Material.STONE), ModelTomb.GRAVE_NORMAL).setRegistryName("grave_normal"));
    r.register(new BlockTomb(Block.Properties.of(Material.STONE), ModelTomb.GRAVE_CROSS).setRegistryName("grave_cross"));
    r.register(new BlockTomb(Block.Properties.of(Material.STONE), ModelTomb.GRAVE_TOMB).setRegistryName("tombstone"));
  }

  @SubscribeEvent
  public static void registerItems(Register<Item> event) {
    IForgeRegistry<Item> r = event.getRegistry();
    r.register(new GraveKeyItem(new Item.Properties()).setRegistryName(ModTomb.MODID, "grave_key"));
  }

  @SubscribeEvent
  public static void onTileEntityRegistry(final RegistryEvent.Register<BlockEntityType<?>> event) {
    IForgeRegistry<BlockEntityType<?>> r = event.getRegistry();
    r.register(BlockEntityType.Builder.of(TileEntityTomb::new, new BlockTomb[] {
        TombRegistry.GRAVE_SIMPLE,
        TombRegistry.GRAVE_NORMAL,
        TombRegistry.GRAVE_CROSS,
        TombRegistry.TOMBSTONE,
    }).build(null).setRegistryName("tombstone"));
  }

  @SubscribeEvent
  public static void registerParticleTypes(RegistryEvent.Register<ParticleType<?>> event) {
    IForgeRegistry<ParticleType<?>> r = event.getRegistry();
    TombRegistry.GRAVE_SMOKE.setRegistryName(ModTomb.MODID, "grave_smoke");
    r.register(TombRegistry.GRAVE_SMOKE);
    TombRegistry.ROTATING_SMOKE.setRegistryName(ModTomb.MODID, "rotating_smoke");
    r.register(TombRegistry.ROTATING_SMOKE);
    TombRegistry.SOUL.setRegistryName(ModTomb.MODID, "soul");
    r.register(TombRegistry.SOUL);
  }
}
