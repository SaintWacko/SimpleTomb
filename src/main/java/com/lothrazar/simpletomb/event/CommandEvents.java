package com.lothrazar.simpletomb.event;

import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.command.CommandTomb;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandEvents {

  public static final String[] types = {
      "list", "restore"
  };

  @SubscribeEvent
  public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
    //
    CommandDispatcher<CommandSource> r = event.getDispatcher();
    r.register(LiteralArgumentBuilder.<CommandSource> literal(ModTomb.MODID)
        .requires((p) -> {
          return p.hasPermissionLevel(3);
        })
        .then(Commands.argument("type", StringArgumentType.greedyString()).suggests((s, b) -> {
          return ISuggestionProvider.suggest(types, b);
        })
            .then(Commands.argument("targets", EntityArgument.players()))
            .executes(this::execute)));
  }

  private int execute(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
    ServerPlayerEntity user = ctx.getSource().asPlayer();
    List<String> arguments = Arrays.asList(ctx.getInput().split("\\s+"));
    Collection<GameProfile> players = GameProfileArgument.getGameProfiles(ctx, "player");
    String sub = arguments.get(1);
    for (String type : types) {
      if (type.equalsIgnoreCase(sub)) {
        ModTomb.LOGGER.info("Execute " + sub);
        for (GameProfile gameprofile : players) {
          //
          return CommandTomb.execute(ctx, gameprofile.getId(), user);
        }
      }
    }
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
