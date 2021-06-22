package com.lothrazar.simpletomb.command;

import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.event.PlayerTombEvents.GraveData;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandList implements ITombCommand {

  @Override
  public boolean needsOp() {
    return false;
  }

  @Override
  public String getName() {
    return "list";
  }

  @Override
  public int execute(CommandContext<CommandSource> ctx, List<String> arguments, PlayerEntity player) {
    UUID id = player.getUniqueID();
    if (ModTomb.GLOBAL.grv.containsKey(id)) {
      GraveData found = ModTomb.GLOBAL.grv.get(id);
      TranslationTextComponent msg = new TranslationTextComponent("");
      msg.appendString("# " + found.playerGraves.size());
      msg.setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD));
      player.sendMessage(msg, player.getUniqueID());
      for (CompoundNBT gd : found.playerGraves) {
        //
        msg.appendString("" + gd.toString());
      }
    }
    return 0;
  }
}
