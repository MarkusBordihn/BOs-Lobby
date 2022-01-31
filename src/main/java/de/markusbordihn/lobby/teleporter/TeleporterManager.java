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

package de.markusbordihn.lobby.teleporter;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;
import de.markusbordihn.lobby.dimension.DimensionManager;

public class TeleporterManager {

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;

  private static boolean defaultUseCustomSpawnPoint = COMMON.defaultUseCustomSpawnPoint.get();
  private static int defaultSpawnPointX = COMMON.defaultSpawnPointX.get();
  private static int defaultSpawnPointY = COMMON.defaultSpawnPointY.get();
  private static int defaultSpawnPointZ = COMMON.defaultSpawnPointZ.get();

  private static boolean fishingUseCustomSpawnPoint = COMMON.fishingUseCustomSpawnPoint.get();
  private static int fishingSpawnPointX = COMMON.fishingSpawnPointX.get();
  private static int fishingSpawnPointY = COMMON.fishingSpawnPointY.get();
  private static int fishingSpawnPointZ = COMMON.fishingSpawnPointZ.get();

  private static boolean lobbyUseCustomSpawnPoint = COMMON.lobbyUseCustomSpawnPoint.get();
  private static int lobbySpawnPointX = COMMON.lobbySpawnPointX.get();
  private static int lobbySpawnPointY = COMMON.lobbySpawnPointY.get();
  private static int lobbySpawnPointZ = COMMON.lobbySpawnPointZ.get();

  private static boolean miningUseCustomSpawnPoint = COMMON.miningUseCustomSpawnPoint.get();
  private static int miningSpawnPointX = COMMON.miningSpawnPointX.get();
  private static int miningSpawnPointY = COMMON.miningSpawnPointY.get();
  private static int miningSpawnPointZ = COMMON.miningSpawnPointZ.get();

  // Default spawn points for the default structures
  private static BlockPos defaultFishingSpawnPoint = new BlockPos(8, 53, 8);
  private static BlockPos defaultMiningSpawnPoint = new BlockPos(200, 11, 558);
  private static BlockPos defaultLobbySpawnPoint = new BlockPos(9, 9, 9);

  protected TeleporterManager() {}

  @SubscribeEvent
  public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {

    // Make sure we have the current config settings.
    defaultUseCustomSpawnPoint = COMMON.defaultUseCustomSpawnPoint.get();
    defaultSpawnPointX = COMMON.defaultSpawnPointX.get();
    defaultSpawnPointY = COMMON.defaultSpawnPointY.get();
    defaultSpawnPointZ = COMMON.defaultSpawnPointZ.get();

    fishingUseCustomSpawnPoint = COMMON.fishingUseCustomSpawnPoint.get();
    fishingSpawnPointX = COMMON.fishingSpawnPointX.get();
    fishingSpawnPointY = COMMON.fishingSpawnPointY.get();
    fishingSpawnPointZ = COMMON.fishingSpawnPointZ.get();

    lobbyUseCustomSpawnPoint = COMMON.lobbyUseCustomSpawnPoint.get();
    lobbySpawnPointX = COMMON.lobbySpawnPointX.get();
    lobbySpawnPointY = COMMON.lobbySpawnPointY.get();
    lobbySpawnPointZ = COMMON.lobbySpawnPointZ.get();

    miningUseCustomSpawnPoint = COMMON.miningUseCustomSpawnPoint.get();
    miningSpawnPointX = COMMON.miningSpawnPointX.get();
    miningSpawnPointY = COMMON.miningSpawnPointY.get();
    miningSpawnPointZ = COMMON.miningSpawnPointZ.get();
  }

  public static boolean teleportToDefaultDimension(ServerPlayer player) {
    ServerLevel defaultDimension = DimensionManager.getDefaultDimension();
    boolean isSameDimension = player.level == defaultDimension;
    boolean successfullyTeleported = false;
    if (defaultUseCustomSpawnPoint) {
      successfullyTeleported = teleportPlayer(player, defaultDimension, defaultSpawnPointX,
          defaultSpawnPointY, defaultSpawnPointZ);
    } else {
      successfullyTeleported = teleportPlayer(player, defaultDimension);
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendMessage(new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_default"),
          Util.NIL_UUID);
    }
    return successfullyTeleported;
  }

  public static boolean teleportToFishingDimension(ServerPlayer player) {
    ServerLevel fishingDimension = DimensionManager.getFishingDimension();
    boolean isSameDimension = player.level == fishingDimension;
    boolean successfullyTeleported = false;
    if (fishingUseCustomSpawnPoint) {
      successfullyTeleported = teleportPlayer(player, fishingDimension, fishingSpawnPointX,
          fishingSpawnPointY, fishingSpawnPointZ);
    } else {
      successfullyTeleported =
          teleportPlayer(player, fishingDimension, defaultFishingSpawnPoint.getX(),
              defaultFishingSpawnPoint.getY(), defaultFishingSpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendMessage(new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_fishing"),
          Util.NIL_UUID);
    }
    return successfullyTeleported;
  }

  public static boolean teleportToLobbyDimension(ServerPlayer player) {
    ServerLevel lobbyDimension = DimensionManager.getLobbyDimension();
    boolean isSameDimension = player.level == lobbyDimension;
    boolean successfullyTeleported = false;
    if (lobbyUseCustomSpawnPoint) {
      successfullyTeleported = teleportPlayer(player, lobbyDimension, lobbySpawnPointX,
          lobbySpawnPointY, lobbySpawnPointZ);
    } else {
      successfullyTeleported = teleportPlayer(player, lobbyDimension, defaultLobbySpawnPoint.getX(),
          defaultLobbySpawnPoint.getY(), defaultLobbySpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendMessage(new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_lobby"),
          Util.NIL_UUID);
    }
    return successfullyTeleported;
  }

  public static boolean teleportToMiningDimension(ServerPlayer player) {
    ServerLevel miningDimension = DimensionManager.getMiningDimension();
    boolean isSameDimension = player.level == miningDimension;
    boolean successfullyTeleported = false;
    if (miningUseCustomSpawnPoint) {
      successfullyTeleported = teleportPlayer(player, miningDimension, miningSpawnPointX,
          miningSpawnPointY, miningSpawnPointZ);
    } else {
      successfullyTeleported =
          teleportPlayer(player, miningDimension, defaultMiningSpawnPoint.getX(),
              defaultMiningSpawnPoint.getY(), defaultMiningSpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendMessage(new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_mining"),
          Util.NIL_UUID);
    }
    return successfullyTeleported;
  }

  private static boolean teleportPlayer(ServerPlayer player, ServerLevel dimension) {
    // Ignore client side levels and if dimension was not found.
    if (player.getLevel().isClientSide() || dimension == null) {
      return false;
    }
    BlockPos sharedSpawnPos = dimension.getSharedSpawnPos();
    return teleportPlayer(player, dimension, sharedSpawnPos.getX(), sharedSpawnPos.getY(),
        sharedSpawnPos.getZ());
  }

  private static boolean teleportPlayer(ServerPlayer player, ServerLevel dimension, int x, int y,
      int z) {
    // Ignore client side levels and if dimension was not found.
    if (player.getLevel().isClientSide() || dimension == null) {
      return false;
    }

    // If we are already in the same dimension use a simple teleport instead.
    if (player.level == dimension) {
      player.teleportTo(x, y, z);
      return true;
    }

    // Use dimensional teleporter for the player.
    player.teleportTo(dimension, x, y, z, player.getYRot(), player.getXRot());
    return true;
  }
}
