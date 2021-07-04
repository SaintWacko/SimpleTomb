package com.lothrazar.simpletomb.event;

import com.lothrazar.cyclic.ModCyclic;
import com.lothrazar.simpletomb.ConfigTomb;
import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.block.BlockTomb;
import com.lothrazar.simpletomb.block.TileEntityTomb;
import com.lothrazar.simpletomb.data.DeathHelper;
import com.lothrazar.simpletomb.data.LocationBlockPos;
import com.lothrazar.simpletomb.data.MessageType;
import com.lothrazar.simpletomb.helper.EntityHelper;
import com.lothrazar.simpletomb.helper.WorldHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.world.ExplosionEvent.Detonate;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.logging.log4j.Level;

public class PlayerTombEvents {

  public Map<UUID, GraveData> grv = new HashMap<>();
  private static final String TOMB_FILE_EXT = ".mctomb";
  private static final String TB_SOULBOUND_STACKS = "tb_soulbound_stacks";

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void onPlayerLogged(PlayerLoggedInEvent event) {
    if (EntityHelper.isValidPlayerMP(event.getPlayer())) {
      ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
      assert player.getServer() != null;
      CompoundNBT playerData = player.getPersistentData();
      CompoundNBT persistantData;
      if (playerData.contains(EntityHelper.NBT_PLAYER_PERSISTED)) {
        persistantData = playerData.getCompound(EntityHelper.NBT_PLAYER_PERSISTED);
      }
      else {
        persistantData = new CompoundNBT();
        playerData.put(EntityHelper.NBT_PLAYER_PERSISTED, persistantData);
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onDetonate(Detonate event) {
    event.getAffectedBlocks().removeIf(blockPos -> (event.getWorld().getBlockState(blockPos).getBlock() instanceof BlockTomb));
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    PlayerEntity player = event.getPlayer();
    if (EntityHelper.isValidPlayerMP(player) && !player.isSpectator()) {
      CompoundNBT persistentTag = EntityHelper.getPersistentTag(player);
      ListNBT stackList = persistentTag.getList(TB_SOULBOUND_STACKS, 10);
      for (int i = 0; i < stackList.size(); ++i) {
        //        ItemStack.
        ItemStack stack = ItemStack.read(stackList.getCompound(i));
        if (!stack.isEmpty()) {
          ItemHandlerHelper.giveItemToPlayer(player, stack);
        }
      }
      persistentTag.remove(TB_SOULBOUND_STACKS);
      player.container.detectAndSendChanges();
    }
  }

  private void storeSoulboundsOnBody(PlayerEntity player, List<ItemStack> keys) {
    CompoundNBT persistentTag = EntityHelper.getPersistentTag(player);
    ListNBT stackList = new ListNBT();
    persistentTag.put(TB_SOULBOUND_STACKS, stackList);
    for (ItemStack key : keys) {
      stackList.add(key.serializeNBT());
    }
    keys.clear();
  }
  //  private void storeIntegerStorageMap(PlayerEntity player) {
  //    //  for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
  //    //      ModTomb.LOGGER.info(i + " player inventory = " + player.inventory.getStackInSlot(i));
  //    //TODO: create an ITEMIDSLOT -> MAP
  //    //to remap those first
  //    //
  //    //
  //    // }
  //  }
  //
  //  @SubscribeEvent
  //  public void onPlayerDeath(LivingDeathEvent event) {
  //    if (!ConfigTomb.TOMBENABLED.get()) {
  //      return;
  //    }
  //    if (event.getEntityLiving() instanceof PlayerEntity) {
  //      PlayerEntity player = (PlayerEntity) event.getEntityLiving();
  //      storeIntegerStorageMap(player);
  //    }
  //  }

  @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
  public void onPlayerDrops(LivingDropsEvent event) {
    if (!ConfigTomb.TOMBENABLED.get()) {
      return;
    }
    if (!EntityHelper.isValidPlayer(event.getEntityLiving()) ||
        WorldHelper.isRuleKeepInventory((PlayerEntity) event.getEntityLiving())) {
      return;
    }
    saveBackup(event);
    placeTombstone(event);
  }

  @SubscribeEvent
  public void onSaveFile(PlayerEvent.SaveToFile event) {
    PlayerEntity player = event.getPlayer();
    File mctomb = new File(event.getPlayerDirectory(), player.getUniqueID() + TOMB_FILE_EXT);
    //
    //save player data to the file 
    if (grv.containsKey(player.getUniqueID())) {
      //yes i have data to save
      GraveData dataToSave = grv.get(player.getUniqueID());
      CompoundNBT data = dataToSave.write();
      try {
        FileOutputStream fileoutputstream = new FileOutputStream(mctomb);
        CompressedStreamTools.writeCompressed(data, fileoutputstream);
        fileoutputstream.close();
        ModCyclic.LOGGER.error("TOMB PlayerEvent.SaveToFile" + data);
        ModCyclic.LOGGER.error("# of tombs " + dataToSave.playerGraves.size());
      }
      catch (IOException e) {
        ModCyclic.LOGGER.error("IO", e);
      }
    }
  }

  @SubscribeEvent
  public void onLoadFile(PlayerEvent.LoadFromFile event) {
    PlayerEntity player = event.getPlayer();
    File mctomb = new File(event.getPlayerDirectory(), player.getUniqueID() + TOMB_FILE_EXT);
    if (mctomb.exists()) {
      try {
        FileInputStream fileinputstream = new FileInputStream(mctomb);
        CompoundNBT data = CompressedStreamTools.readCompressed(fileinputstream);
        fileinputstream.close();
        GraveData dataLoaded = new GraveData();
        dataLoaded.read(data, player.getUniqueID());
        if (grv.containsKey(player.getUniqueID())) {
          //overwrite list
          grv.put(player.getUniqueID(), dataLoaded);
        }
        else {
          //set list
          grv.put(player.getUniqueID(), dataLoaded);
        }
        ModCyclic.LOGGER.error("TOMB PlayerEvent.LoadFromFile " + data);
        ModCyclic.LOGGER.error("# of tombs " + dataLoaded.playerGraves.size());
      }
      catch (Exception e) {
        ModCyclic.LOGGER.error("IO", e);
      }
    }
    //LOAD player data
  }

  public static class GraveData {

    UUID playerId;
    public List<CompoundNBT> playerGraves = new ArrayList<>();

    public GraveData(UUID id, CompoundNBT first) {
      playerId = id;
      playerGraves.add(first);
    }

    public GraveData() {}

    public void read(CompoundNBT data, UUID playerId) {
      this.playerId = playerId;
      if (data.contains(ModTomb.MODID)) {
        ListNBT glist = data.getList(ModTomb.MODID, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < glist.size(); i++) {
          this.playerGraves.add(glist.getCompound(i));
        }
      }
    }

    public CompoundNBT write() {
      CompoundNBT data = new CompoundNBT();
      ListNBT glist = new ListNBT();
      for (CompoundNBT g : playerGraves) {
        glist.add(g);
      }
      data.put(ModTomb.MODID, glist);
      return data;
    }
  }

  private void saveBackup(LivingDropsEvent event) {
    ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
    //    ServerWorld world = player.getServerWorld();
    Iterator<ItemEntity> it = event.getDrops().iterator();
    ListNBT drops = new ListNBT();
    boolean isEmpty = true; //empty unless one non-key item found
    CompoundNBT tombstoneTag = new CompoundNBT();
    while (it.hasNext()) {
      ItemEntity entityItem = it.next();
      if (entityItem != null && !entityItem.getItem().isEmpty()) {
        ItemStack stack = entityItem.getItem();
        //        stuff.add(stack);
        drops.add(stack.write(new CompoundNBT()));
        if (stack.getItem() != TombRegistry.GRAVE_KEY) {
          isEmpty = false;
        }
      }
    }
    if (!isEmpty) {
      //NEW data model. write to string
      //timestamp 
      tombstoneTag.putLong("timestamp", System.currentTimeMillis());
      tombstoneTag.put("drops", drops);
      tombstoneTag.put("pos", NBTUtil.writeBlockPos(player.getPosition()));
      tombstoneTag.putString("dimension", player.world.getDimensionKey().getLocation().toString());
      UUID pid = player.getUniqueID();
      tombstoneTag.putString("playerid", pid.toString());
      tombstoneTag.putString("playername", player.getDisplayName().getString());
      //    world.getSavedData().ge
      //save to file
      if (grv.containsKey(pid)) {
        grv.get(pid).playerGraves.add(tombstoneTag);
      }
      else {
        grv.put(pid, new GraveData(pid, tombstoneTag));
      }
    }
  }

  private void placeTombstone(LivingDropsEvent event) {
    ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
    ServerWorld world = player.getServerWorld();
    Iterator<ItemEntity> it = event.getDrops().iterator();
    ArrayList<ItemStack> keys = new ArrayList<>();
    while (it.hasNext()) {
      ItemEntity entityItem = it.next();
      if (entityItem != null && !entityItem.getItem().isEmpty()) {
        ItemStack stack = entityItem.getItem();
        if (stack.getItem() == TombRegistry.GRAVE_KEY) {
          keys.add(stack.copy());
          it.remove();
        }
      }
    }
    List<ItemEntity> itemsPickedUpFromGround = pickupFromGround(player, keys);
    this.storeSoulboundsOnBody(player, keys);
    boolean hasDrop = event.getDrops().size() > 0 || itemsPickedUpFromGround.size() > 0;
    if (!hasDrop) {
      MessageType.MESSAGE_NO_LOOT_FOR_GRAVE.sendSpecialMessage(player);
      return;
    }
    BlockPos initPos = WorldHelper.getInitialPos(world, new BlockPos(player.getPosition()));
    LocationBlockPos spawnPos = WorldHelper.findGraveSpawn(player, initPos);
    if (spawnPos == null || spawnPos.toBlockPos() == null) {
      //found a block but its not air, cant use it
      MessageType.MESSAGE_NO_PLACE_FOR_GRAVE.sendSpecialMessage(player);
      ModTomb.LOGGER.log(Level.INFO, MessageType.MESSAGE_NO_PLACE_FOR_GRAVE.getTranslation());
      return;
    }
    Direction facing = player.getHorizontalFacing().getOpposite();
    BlockState state = getRandomGrave(world, facing);
    boolean wasPlaced = WorldHelper.placeGrave(world, spawnPos.toBlockPos(), state);
    if (!wasPlaced) {
      sendFailMessage(player);
      return;
    }
    TileEntity tile = world.getTileEntity(spawnPos.toBlockPos());
    IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null);
    if (!(tile instanceof TileEntityTomb)
        || itemHandler == null) {
      //either block failed to place, or tile entity wasnt started somehow
      sendFailMessage(player);
      return;
    }
    //else grave success
    TileEntityTomb grave = (TileEntityTomb) tile;
    grave.initTombstoneOwner(player);
    if (ConfigTomb.KEYGIVEN.get()) {
      ItemStack key = new ItemStack(TombRegistry.GRAVE_KEY);
      TombRegistry.GRAVE_KEY.setTombPos(key, spawnPos);
      setKeyName(player, key);
      keys.add(key);
    }
    this.storeSoulboundsOnBody(player, keys);
    // we know itemHandler is not null now
    for (ItemEntity entityItem : event.getDrops()) {
      if (!entityItem.getItem().isEmpty()) {
        ItemHandlerHelper.insertItemStacked(itemHandler, entityItem.getItem().copy(), false);
        entityItem.setItem(ItemStack.EMPTY);
      }
    }
    for (ItemEntity entityItem : itemsPickedUpFromGround) {
      ItemHandlerHelper.insertItemStacked(itemHandler, entityItem.getItem(), false);
      entityItem.setItem(ItemStack.EMPTY);
    }
    world.notifyBlockUpdate(spawnPos.toBlockPos(), state, state, 2);
    //it has been placed
    DeathHelper.INSTANCE.putLastGrave(player, spawnPos);
    if (ConfigTomb.TOMBLOG.get()) {
      ModTomb.LOGGER.info(MessageType.MESSAGE_NEW_GRAVE.getTranslation()
          + String.format("(%d, %d, %d) " + spawnPos.dim, spawnPos.x, spawnPos.y, spawnPos.z));
    }
    if (ConfigTomb.TOMBCHAT.get()) {
      MessageType.MESSAGE_NEW_GRAVE.sendSpecialMessage(player);
      MessageType.MESSAGE_JOURNEYMAP.sendSpecialMessage(player, spawnPos.x, spawnPos.y, spawnPos.z, spawnPos.dim);
    }
  }

  private void sendFailMessage(ServerPlayerEntity player) {
    MessageType.MESSAGE_FAIL_TO_PLACE_GRAVE.sendSpecialMessage(player);
    ModTomb.LOGGER.log(Level.INFO, MessageType.MESSAGE_FAIL_TO_PLACE_GRAVE.getTranslation());
  }

  private void setKeyName(ServerPlayerEntity player, ItemStack key) {
    if (ConfigTomb.KEYNAMED.get()) {
      TranslationTextComponent text = new TranslationTextComponent(player.getName().getString());
      text.append(new TranslationTextComponent(" "));
      text.append(key.getDisplayName());
      text.mergeStyle(TextFormatting.GOLD);
      key.setDisplayName(text);
    }
  }

  private BlockState getRandomGrave(ServerWorld world, Direction facing) {
    //TODO: CONFIG or other selection of what the player wants
    BlockTomb[] graves = new BlockTomb[] {
        TombRegistry.GRAVE_SIMPLE,
        TombRegistry.GRAVE_NORMAL,
        TombRegistry.GRAVE_CROSS,
        TombRegistry.TOMBSTONE,
    };
    BlockState state = graves[world.rand.nextInt(graves.length)].getDefaultState();
    state = state.with(BlockTomb.FACING, facing);
    state = state.with(BlockTomb.MODEL_TEXTURE, world.rand.nextInt(2));
    return state;
  }

  private List<ItemEntity> pickupFromGround(PlayerEntity player, ArrayList<ItemStack> keys) {
    double range = ConfigTomb.TOMBEXTRAITEMS.get();
    if (range == 0) {
      return new ArrayList<>();
    }
    int posX = player.getPosition().getX();
    int posY = player.getPosition().getY();
    int posZ = player.getPosition().getZ();
    return player.world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(
        posX - range,
        posY - range,
        posZ - range,
        posX + range,
        posY + range,
        posZ + range));
  }
}
