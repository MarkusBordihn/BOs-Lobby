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
import net.minecraft.world.level.saveddata.SavedData;

import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.dimension.DimensionManager;

@EventBusSubscriber
public class FishingData extends SavedData {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final String FILE_ID = Constants.MOD_ID;
  private static MinecraftServer server = null;
  private static FishingData data = null;
  private static ServerLevel level = null;

  private boolean dimensionLoaded = false;
  private long lastUpdate;

  public FishingData() {
    this.setDirty();
  }

  @SubscribeEvent
  public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {
    // Reset data and server for the integrated server.
    data = null;
    level = null;
    server = null;
  }

  public static FishingData get() {
    if (FishingData.data == null || FishingData.level == null) {
      prepare(ServerLifecycleHooks.getCurrentServer());
    }
    return FishingData.data;
  }

  public static void prepare(MinecraftServer server) {
    // Make sure we preparing the data only once for the same server!
    if (server == FishingData.server && FishingData.data != null && FishingData.level != null) {
      return;
    }

    FishingData.server = server;
    FishingData.level = DimensionManager.getFishingDimension();
    if (FishingData.level != null) {
      log.info("{} preparing data for {} and {}", Constants.LOG_NAME, FishingData.server,
          FishingData.level);
      FishingData.data = FishingData.level.getDataStorage().computeIfAbsent(FishingData::load,
          FishingData::new, FishingData.getFileId());
    } else {
      log.error("Unable to preparing data for {} and {}", FishingData.server, FishingData.level);
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

  public static FishingData load(CompoundTag compoundTag) {
    FishingData fishingData = new FishingData();
    log.info("{} loading fishing dimension data ... {}", Constants.LOG_NAME, compoundTag);
    fishingData.dimensionLoaded = compoundTag.getBoolean("DimensionLoaded");
    fishingData.lastUpdate = compoundTag.getLong("LastUpdate");
    return fishingData;
  }

  @Override
  public CompoundTag save(CompoundTag compoundTag) {
    log.info("{} saving fishing dimension data ... {}", Constants.LOG_NAME, this);
    compoundTag.putBoolean("DimensionLoaded", this.dimensionLoaded);
    compoundTag.putLong("LastUpdate", new Date().getTime());
    return compoundTag;
  }

}
