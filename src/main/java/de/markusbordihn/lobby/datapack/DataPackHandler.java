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

package de.markusbordihn.lobby.datapack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.level.ServerLevel;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.commands.CommandManager;
import de.markusbordihn.lobby.data.FishingData;
import de.markusbordihn.lobby.data.GamingData;
import de.markusbordihn.lobby.data.LobbyData;
import de.markusbordihn.lobby.data.MiningData;
import de.markusbordihn.lobby.data.VoidData;
import de.markusbordihn.lobby.dimension.DimensionManager;

public class DataPackHandler {

  public static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  protected DataPackHandler() {}

  public static void prepareDataPackOnce(ServerLevel level) {
    if (level == DimensionManager.getLobbyDimension()) {
      if (LobbyData.get().getDimensionLoaded()) {
        log.info("Skip Data Pack for lobby dimension {} because it was already loaded!", level);
      } else {
        prepareDataPack(level);
        LobbyData.get().setDimensionLoaded(true);
      }
    } else if (level == DimensionManager.getMiningDimension()) {
      if (MiningData.get().getDimensionLoaded()) {
        log.info("Skip Data Pack for mining dimension {} because it was already loaded!", level);
      } else {
        prepareDataPack(level);
        MiningData.get().setDimensionLoaded(true);
      }
    } else if (level == DimensionManager.getFishingDimension()) {
      if (FishingData.get().getDimensionLoaded()) {
        log.info("Skip Data Pack for fishing dimension {} because it was already loaded!", level);
      } else {
        prepareDataPack(level);
        FishingData.get().setDimensionLoaded(true);
      }
    } else if (level == DimensionManager.getGamingDimension()) {
      if (GamingData.get().getDimensionLoaded()) {
        log.info("Skip Data Pack for gaming dimension {} because it was already loaded!", level);
      } else {
        prepareDataPack(level);
        GamingData.get().setDimensionLoaded(true);
      }
    } else if (level == DimensionManager.getVoidDimension()) {
      if (VoidData.get().getDimensionLoaded()) {
        log.info("Skip Data Pack for void dimension {} because it was already loaded!", level);
      } else {
        prepareDataPack(level);
        VoidData.get().setDimensionLoaded(true);
      }
    } else {
      log.warn("Unable to get status for level {} to confirm data pack load status!", level);
    }
  }

  public static void prepareDataPack(ServerLevel level) {
    String dimensionLocation = level.dimension().location().toString();
    String dataPackFunction =
        dimensionLocation.split(":")[0] + ":" + dimensionLocation.split(":")[1] + "_load";
    log.info("Loading data pack with {} for level {} ...", dataPackFunction, level);
    CommandManager.executeServerFunction(dataPackFunction, level);
  }
}
