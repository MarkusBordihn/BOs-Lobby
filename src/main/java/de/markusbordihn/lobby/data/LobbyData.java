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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;

import de.markusbordihn.lobby.Constants;

@EventBusSubscriber
public class LobbyData extends SavedData {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final String LOBBY_FILE_ID = Constants.MOD_ID;
  private static MinecraftServer server = null;
  private static LobbyData data = null;

  private boolean lobbyDimensionLoaded = false;
  private boolean miningDimensionLoaded = false;
  private long lastUpdate;

  public LobbyData() {
    this.setDirty();
  }

  @SubscribeEvent
  public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {
    // Reset data and server for the integrated server.
    data = null;
    server = null;
  }

  public static LobbyData get() {
    if (LobbyData.data == null) {
      prepare(ServerLifecycleHooks.getCurrentServer());
    }
    return LobbyData.data;
  }

  public static void prepare(MinecraftServer server) {
    // Make sure we preparing the data only once for the same server!
    if (server == LobbyData.server && LobbyData.data != null) {
      return;
    }

    LobbyData.server = server;
    ServerLevel serverLevel = server.getLevel(Level.OVERWORLD);
    log.info("{} preparing data for {} and {}", Constants.LOG_NAME, server, serverLevel);

    // Using a global approach and storing relevant data in the overworld only!
    LobbyData.data = serverLevel.getDataStorage().computeIfAbsent(
        LobbyData::load, LobbyData::new, LobbyData.getFileId());
  }

  public static String getFileId() {
    return LOBBY_FILE_ID;
  }

  public boolean isLobbyDimensionLoaded() {
    return lobbyDimensionLoaded;
  }

  public boolean isMiningDimensionLoaded() {
    return miningDimensionLoaded;
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  public void setMiningDimensionLoaded(boolean loaded) {
    this.miningDimensionLoaded = loaded;
  }

  public void setLobbyDimensionLoaded(boolean loaded) {
    this.lobbyDimensionLoaded = loaded;
  }

  public static LobbyData load(CompoundTag compoundTag) {
    LobbyData lobbyData = new LobbyData();
    log.info("{} loading mod data ... {}", Constants.LOG_NAME, compoundTag);
    lobbyData.lobbyDimensionLoaded = compoundTag.getBoolean("LobbyDimensionLoaded");
    lobbyData.miningDimensionLoaded = compoundTag.getBoolean("miningDimensionLoaded");
    lobbyData.lastUpdate = compoundTag.getLong("LastUpdate");
    return lobbyData;
  }

  @Override
  public CompoundTag save(CompoundTag compoundTag) {
    log.info("{} saving mod data ... {}", Constants.LOG_NAME, this);
    compoundTag.putBoolean("LobbyDimensionLoaded", this.lobbyDimensionLoaded);
    compoundTag.putBoolean("MiningDimensionLoaded", this.miningDimensionLoaded);
    compoundTag.putLong("LastUpdate", new Date().getTime());
    return compoundTag;
  }

}
