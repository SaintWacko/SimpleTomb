package com.lothrazar.simpletomb.event;

import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.data.PlayerTombRecords;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

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
                .then(Commands.argument("selected", IntegerArgumentType.integer())
                    .executes(x -> {
                      int index = IntegerArgumentType.getInteger(x, "selected");
                      return exeRestore(x, getPlayerIdArg(x), index);
                    }))))
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
    ServerPlayerEntity user = ctx.getSource().asPlayer();
    PlayerTombRecords found = ModTomb.GLOBAL.findGrave(id);
    if (found != null) {
      TranslationTextComponent msg = new TranslationTextComponent("Found: #" + found.playerGraves.size());
      msg.appendString("====");
      //      msg.appendString("# " + found.playerGraves.size());
      for (CompoundNBT gd : found.playerGraves) {
        msg.appendString("" + gd.toString() + "\r\n");
        msg.appendString("====");
      }
      msg.setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD));
      user.sendMessage(msg, user.getUniqueID());
    }
    return 1;
  }

  private int exeRestore(CommandContext<CommandSource> ctx, UUID id, int index) throws CommandSyntaxException {
    ServerPlayerEntity user = ctx.getSource().asPlayer();
    TranslationTextComponent msg = new TranslationTextComponent("Restoring [" + index + "] for player " + id);
    user.sendMessage(msg, user.getUniqueID());
    PlayerTombRecords found = ModTomb.GLOBAL.findGrave(id);
    if (found != null) {
      CompoundNBT grave = found.playerGraves.get(index);
      ModTomb.LOGGER.error("found " + grave);
      BlockPos pos = PlayerTombRecords.getPos(grave);
      String dim = PlayerTombRecords.getDim(grave);
      List<ItemStack> drops = PlayerTombRecords.getDrops(grave);
      ModTomb.LOGGER.error("items contained " + drops.size());
      //TODO: is this dupe code from location class
      RegistryKey<World> dimKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, ResourceLocation.tryCreate(dim));
      ServerWorld targetWorld = ctx.getSource().getWorld().getServer().getWorld(dimKey);
      BlockState state = PlayerTombEvents.getRandomGrave(targetWorld, Direction.NORTH);
      boolean wasPlaced = WorldHelper.placeGrave(targetWorld, pos, state);
      if (wasPlaced) {
        //fill it up
        TileEntity tile = targetWorld.getTileEntity(pos);
        IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null);
        //        ItemHandlerHelper.ins
        for (ItemStack d : drops) {
          ItemHandlerHelper.insertItemStacked(itemHandler, d.copy(), false);
        }
      }
    }
    user.sendMessage(msg, user.getUniqueID());
    return 0;
  }
  //
  //  private void badCommandMsg(ServerPlayerEntity player) {
  //    //.setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD))
  //    player.sendMessage(new TranslationTextComponent(ModTomb.MODID + ".commands.null"), player.getUniqueID());
  //    player.sendMessage(new TranslationTextComponent("[" + String.join(", ", SUBCOMMANDS) + "]"), player.getUniqueID());
  //  }
}
