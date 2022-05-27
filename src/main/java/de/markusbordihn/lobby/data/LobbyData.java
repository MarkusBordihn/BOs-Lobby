/**
 * Copyright 2022 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.lobby.data;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.dimension.DimensionManager;

@EventBusSubscriber
public class LobbyData extends SavedData {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final String FILE_ID = Constants.MOD_ID;
  private static LobbyData data = null;
  private static MinecraftServer server = null;
  private static ServerLevel level = null;
  private static Set<UUID> playerTeleportList = ConcurrentHashMap.newKeySet();

  public static final String PLAYER_TELEPORT_LIST_TAG = "PlayerTeleportList";
  public static final String PLAYER_UUID_TAG = "UUID";

  private boolean dimensionLoaded = false;
  private long lastUpdate;

  public LobbyData() {
    this.setDirty();
  }

  @SubscribeEvent
  public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {
    // Reset data and server for the integrated server.
    data = null;
    level = null;
    server = null;
  }

  public static LobbyData get() {
    if (LobbyData.data == null || LobbyData.level == null) {
      prepare(ServerLifecycleHooks.getCurrentServer());
    }
    return LobbyData.data;
  }

  public static void prepare(MinecraftServer server) {
    // Make sure we preparing the data only once for the same server!
    if (server == LobbyData.server && LobbyData.data != null && LobbyData.level != null) {
      return;
    }

    LobbyData.server = server;
    LobbyData.level = DimensionManager.getLobbyDimension();
    if (LobbyData.level != null) {
      log.info("{} preparing data for {} and {}", Constants.LOG_NAME, LobbyData.server,
          LobbyData.level);

      // Using a global approach and storing relevant data in the overworld only!
      LobbyData.data = LobbyData.level.getDataStorage().computeIfAbsent(LobbyData::load,
          LobbyData::new, LobbyData.getFileId());
    } else {
      log.error("Unable to preparing data for {} and {}", LobbyData.server, LobbyData.level);
    }
  }

  public static String getFileId() {
    return FILE_ID;
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  public boolean getDimensionLoaded() {
    return this.dimensionLoaded;
  }

  public void setDimensionLoaded(boolean loaded) {
    this.dimensionLoaded = loaded;
  }

  public Set<UUID> getPlayerTeleportList() {
    return playerTeleportList;
  }

  public static void setPlayerTeleportList(Set<UUID> newPlayerTeleportList) {
    playerTeleportList = newPlayerTeleportList;
  }

  public static LobbyData load(CompoundTag compoundTag) {
    LobbyData lobbyData = new LobbyData();
    log.info("{} loading lobby dimension data ... {}", Constants.LOG_NAME, compoundTag);
    lobbyData.dimensionLoaded = compoundTag.getBoolean("DimensionLoaded");
    lobbyData.lastUpdate = compoundTag.getLong("LastUpdate");

    // Restoring Player Teleport List
    if (compoundTag.contains(PLAYER_TELEPORT_LIST_TAG)) {
      ListTag playerTeleportListTag = compoundTag.getList(PLAYER_TELEPORT_LIST_TAG, 10);
      for (int i = 0; i < playerTeleportListTag.size(); ++i) {
        UUID playerTeleportListUUID = playerTeleportListTag.getCompound(i).getUUID(PLAYER_UUID_TAG);
        if (playerTeleportListUUID != null) {
          playerTeleportList.add(playerTeleportListUUID);
        }
      }
    }

    return lobbyData;
  }

  @Override
  public CompoundTag save(CompoundTag compoundTag) {
    log.info("{} saving lobby dimension data ... {}", Constants.LOG_NAME, this);
    compoundTag.putBoolean("DimensionLoaded", this.dimensionLoaded);
    compoundTag.putLong("LastUpdate", new Date().getTime());

    // Store Player Teleport List
    ListTag playerTeleportListTag = new ListTag();
    Iterator<UUID> playerTeleportListIterator = playerTeleportList.iterator();
    while (playerTeleportListIterator.hasNext()) {
      UUID playerTeleportListUUID = playerTeleportListIterator.next();
      if (playerTeleportListUUID != null) {
        CompoundTag playerTeleportListCompoundTag = new CompoundTag();
        playerTeleportListCompoundTag.putUUID(PLAYER_UUID_TAG, playerTeleportListUUID);
        playerTeleportListTag.add(playerTeleportListCompoundTag);
      }
    }
    compoundTag.put(PLAYER_TELEPORT_LIST_TAG, playerTeleportListTag);

    return compoundTag;
  }

}
