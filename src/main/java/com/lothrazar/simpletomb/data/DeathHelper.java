package com.lothrazar.simpletomb.data;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathHelper {

  public static final DeathHelper INSTANCE = new DeathHelper();
  private final Map<UUID, LocationBlockPos> lastGraveList = new HashMap<>();

  public LocationBlockPos getLastGrave(Player player) {
    return lastGraveList.getOrDefault(player.getGameProfile().getId(), LocationBlockPos.ORIGIN);
  }

  public LocationBlockPos deleteLastGrave(Player player) {
    return lastGraveList.remove(player.getGameProfile().getId());
  }

  public LocationBlockPos putLastGrave(Player player, LocationBlockPos loc) {
    return lastGraveList.put(player.getGameProfile().getId(), loc);
  }
}
