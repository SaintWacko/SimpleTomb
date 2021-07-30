package com.lothrazar.simpletomb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lothrazar.simpletomb.event.CommandEvents;
import com.lothrazar.simpletomb.event.PlayerTombEvents;
import com.lothrazar.simpletomb.proxy.ClientUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(ModTomb.MODID)
public class ModTomb {

  public static final PlayerTombEvents GLOBAL = new PlayerTombEvents();
  public static final String MODID = "simpletomb";
  public static final Logger LOGGER = LogManager.getLogger();

  public ModTomb() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
    ConfigTomb.setup(FMLPaths.CONFIGDIR.get().resolve(MODID + ".toml"));
    MinecraftForge.EVENT_BUS.register(new CommandEvents());
  }

  private void setupClient(final FMLClientSetupEvent event) {
    ClientUtils.setup();
  }

  private void setup(final FMLCommonSetupEvent event) {
    MinecraftForge.EVENT_BUS.register(GLOBAL);
  }
}
