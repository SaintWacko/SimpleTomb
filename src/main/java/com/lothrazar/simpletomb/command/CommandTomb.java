package com.lothrazar.simpletomb.command;

import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.event.PlayerTombEvents.GraveData;
import com.mojang.brigadier.context.CommandContext;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandTomb {

  public static int execute(CommandContext<CommandSource> ctx, UUID id, PlayerEntity player) {
    if (ModTomb.GLOBAL.grv.containsKey(id)) {
      GraveData found = ModTomb.GLOBAL.grv.get(id);
      TranslationTextComponent msg = new TranslationTextComponent("");
      msg.appendString("# " + found.playerGraves.size());
      msg.setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD));
      for (CompoundNBT gd : found.playerGraves) {
        //
        msg.appendString("" + gd.toString() + "\r\n");
      }
      player.sendMessage(msg, player.getUniqueID());
    }
    return 0;
  }
}
