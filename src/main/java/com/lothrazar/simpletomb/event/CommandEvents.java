package com.lothrazar.simpletomb.event;

import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.event.PlayerTombEvents.GraveData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandEvents {

  private static final String ARG_PLAYER = "player";

  public static enum TombCommands {

    RESTORE, LIST;

    @Override
    public String toString() {
      return this.name().toLowerCase();
    }
  }

  @SubscribeEvent
  public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
    //
    CommandDispatcher<CommandSource> r = event.getDispatcher();
    r.register(LiteralArgumentBuilder.<CommandSource> literal(ModTomb.MODID)
        .requires((p) -> {
          return p.hasPermissionLevel(3);
        })
        .then(Commands.literal(TombCommands.RESTORE.toString())
            .then(Commands.argument(ARG_PLAYER, GameProfileArgument.gameProfile()).suggests((cs, b) -> buildPlayerArg(cs, b))
                .executes(x -> {
                  return exeRestore(x, getPlayerIdArg(x));
                })))
        .then(Commands.literal(TombCommands.LIST.toString())
            .then(Commands.argument(ARG_PLAYER, GameProfileArgument.gameProfile()).suggests((cs, b) -> buildPlayerArg(cs, b))
                .executes(x -> {
                  return exeList(x, getPlayerIdArg(x));
                })))
    //
    );
  }

  private CompletableFuture<Suggestions> buildPlayerArg(CommandContext<CommandSource> cs, SuggestionsBuilder b) {
    return ISuggestionProvider.suggest(cs.getSource().getServer().getPlayerList().getPlayers().stream().map(p -> p.getGameProfile().getName()), b);
  }

  private UUID getPlayerIdArg(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
    return GameProfileArgument.getGameProfiles(ctx, ARG_PLAYER).stream().findFirst().orElse(null).getId();
  }

  private int exeList(CommandContext<CommandSource> ctx, UUID id) throws CommandSyntaxException {
    ModTomb.LOGGER.info("EX list " + ctx.getInput());
    ServerPlayerEntity user = ctx.getSource().asPlayer();
    GraveData found = ModTomb.GLOBAL.findGrave(id);
    if (found != null) {
      TranslationTextComponent msg = new TranslationTextComponent("Found: #" + found.playerGraves.size());
      //      msg.appendString("# " + found.playerGraves.size());
      for (CompoundNBT gd : found.playerGraves) {
        //
        msg.appendString("" + gd.toString() + "\r\n");
      }
      msg.setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD));
      user.sendMessage(msg, user.getUniqueID());
    }
    return 1;
  }

  private int exeRestore(CommandContext<CommandSource> ctx, UUID id) throws CommandSyntaxException {
    ServerPlayerEntity user = ctx.getSource().asPlayer();
    //    String sub = arguments.get(1);
    //    for (String type : types) {
    //      if (type.equalsIgnoreCase(sub)) {
    //        ModTomb.LOGGER.info("Execute " + sub);
    //        for (GameProfile gameprofile : players) {
    //          //
    //          return CommandTomb.execute(ctx, gameprofile.getId(), user);
    //        }
    //      }
    //    }
    //    if (arguments.size() < 2) {
    //      badCommandMsg(player);
    //      return 0;
    //    }
    //    //loop on all registered commands
    //    for (ITombCommand cmd : COMMANDS) {
    //      if (sub.equalsIgnoreCase(cmd.getName())) {
    //        //ok go
    //        //do i need op
    //        if (cmd.needsOp()) {
    //          //ok check me
    //          boolean isOp = ctx.getSource().hasPermissionLevel(1);
    //          if (!isOp) {
    //            //player needs op but does not have it
    //            //            player.getDisplayName()
    //            ModTomb.LOGGER.info("Player [" + player.getUniqueID() + "," + player.getDisplayName() + "] attempted command "
    //                + sub + " but does not have the required permissions");
    //            UtilChat.sendFeedback(ctx, "commands.help.failed");
    //            return 1;
    //          }
    //        }
    //        return cmd.execute(ctx, arguments.subList(2, arguments.size()), player);
    //      }
    //    }
    //    badCommandMsg(player);
    return 0;
  }
  //
  //  private void badCommandMsg(ServerPlayerEntity player) {
  //    //.setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD))
  //    player.sendMessage(new TranslationTextComponent(ModTomb.MODID + ".commands.null"), player.getUniqueID());
  //    player.sendMessage(new TranslationTextComponent("[" + String.join(", ", SUBCOMMANDS) + "]"), player.getUniqueID());
  //  }
}
