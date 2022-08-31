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
public class VoidData extends SavedData {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final String FILE_ID = Constants.MOD_ID;
  private static MinecraftServer server = null;
  private static VoidData data = null;
  private static ServerLevel level = null;

  private boolean dimensionLoaded = false;
  private long lastUpdate;

  public VoidData() {
    this.setDirty();
  }

  @SubscribeEvent
  public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {
    // Reset data and server for the integrated server.
    data = null;
    level = null;
    server = null;
  }

  public static VoidData get() {
    if (VoidData.data == null || VoidData.level == null) {
      prepare(ServerLifecycleHooks.getCurrentServer());
    }
    return VoidData.data;
  }

  public static void prepare(MinecraftServer server) {
    // Make sure we preparing the data only once for the same server!
    if (server == VoidData.server && VoidData.data != null && VoidData.level != null) {
      return;
    }

    VoidData.server = server;
    VoidData.level = DimensionManager.getVoidDimension();
    if (VoidData.level != null) {
      log.info("{} preparing data for {} and {}", Constants.LOG_NAME, VoidData.server,
          VoidData.level);
      VoidData.data = VoidData.level.getDataStorage().computeIfAbsent(VoidData::load, VoidData::new,
          VoidData.getFileId());
    } else {
      log.error("Unable to preparing data for {} and {}", VoidData.server, VoidData.level);
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

  public static VoidData load(CompoundTag compoundTag) {
    VoidData voidData = new VoidData();
    log.info("{} loading void dimension data ... {}", Constants.LOG_NAME, compoundTag);
    voidData.dimensionLoaded = compoundTag.getBoolean("DimensionLoaded");
    voidData.lastUpdate = compoundTag.getLong("LastUpdate");
    return voidData;
  }

  @Override
  public CompoundTag save(CompoundTag compoundTag) {
    log.info("{} saving void dimension data ... {}", Constants.LOG_NAME, this);
    compoundTag.putBoolean("DimensionLoaded", this.dimensionLoaded);
    compoundTag.putLong("LastUpdate", new Date().getTime());
    return compoundTag;
  }

}
