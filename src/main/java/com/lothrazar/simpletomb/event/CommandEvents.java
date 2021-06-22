package com.lothrazar.simpletomb.event;

import com.lothrazar.cyclic.util.UtilChat;
import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.command.CommandGetGrave;
import com.lothrazar.simpletomb.command.CommandList;
import com.lothrazar.simpletomb.command.ITombCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandEvents {

  public static final List<ITombCommand> COMMANDS = new ArrayList<>();
  public static final List<String> SUBCOMMANDS = new ArrayList<>();

  @SubscribeEvent
  public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
    COMMANDS.add(new CommandGetGrave());
    COMMANDS.add(new CommandList());
    //
    if (SUBCOMMANDS.size() == 0) {
      for (ITombCommand cmd : COMMANDS) {
        SUBCOMMANDS.add(cmd.getName());
      }
    }
    //
    CommandDispatcher<CommandSource> r = event.getDispatcher();
    r.register(LiteralArgumentBuilder.<CommandSource> literal(ModTomb.MODID)
        .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(this::execute))
        .executes(this::execute));
  }

  private int execute(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
    ServerPlayerEntity player = ctx.getSource().asPlayer();
    List<String> arguments = Arrays.asList(ctx.getInput().split("\\s+"));
    if (arguments.size() < 2) {
      badCommandMsg(player);
      return 0;
    }
    String sub = arguments.get(1);
    //loop on all registered commands
    for (ITombCommand cmd : COMMANDS) {
      if (sub.equalsIgnoreCase(cmd.getName())) {
        //ok go
        //do i need op
        if (cmd.needsOp()) {
          //ok check me
          boolean isOp = ctx.getSource().hasPermissionLevel(1);
          if (!isOp) {
            //player needs op but does not have it
            //            player.getDisplayName()
            ModTomb.LOGGER.info("Player [" + player.getUniqueID() + "," + player.getDisplayName() + "] attempted command "
                + sub + " but does not have the required permissions");
            UtilChat.sendFeedback(ctx, "commands.help.failed");
            return 1;
          }
        }
        return cmd.execute(ctx, arguments.subList(2, arguments.size()), player);
      }
    }
    badCommandMsg(player);
    return 0;
  }

  private void badCommandMsg(ServerPlayerEntity player) {
    //.setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD))
    player.sendMessage(new TranslationTextComponent(ModTomb.MODID + ".commands.null"), player.getUniqueID());
    player.sendMessage(new TranslationTextComponent("[" + String.join(", ", SUBCOMMANDS) + "]"), player.getUniqueID());
  }
}
