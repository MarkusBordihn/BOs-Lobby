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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;
import de.markusbordihn.lobby.dimension.DimensionManager;

@EventBusSubscriber
public class TeleporterManager {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;

  // Default spawn points for the default structures
  private static BlockPos defaultFishingSpawnPoint = new BlockPos(42, 51, 12);
  private static BlockPos defaultGamingSpawnPoint = new BlockPos(0, 4, 0);
  private static BlockPos defaultLobbySpawnPoint = new BlockPos(9, 11, 9);
  private static BlockPos defaultMiningSpawnPoint = new BlockPos(194, 22, 563);
  private static BlockPos defaultVoidSpawnPoint = new BlockPos(0, 4, 0);

  // Clickable commands
  private static Component fishingCommand;
  private static Component gamingCommand;
  private static Component lobbyCommand;
  private static Component miningCommand;
  private static Component spawnCommand;
  private static Component voidCommand;

  protected TeleporterManager() {}

  @SubscribeEvent
  public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {

    // Construct Clickable commands
    fishingCommand = Component.literal("/" + COMMON.fishingCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.fishingCommandName.get())));
    gamingCommand = Component.literal("/" + COMMON.gamingCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.gamingCommandName.get())));
    lobbyCommand = Component.literal("/" + COMMON.lobbyCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.lobbyCommandName.get())));
    miningCommand = Component.literal("/" + COMMON.miningCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.miningCommandName.get())));
    spawnCommand = Component.literal("/" + COMMON.defaultCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.defaultCommandName.get())));
    voidCommand = Component.literal("/" + COMMON.voidCommandName.get())
        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(
            new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + COMMON.voidCommandName.get())));

    if (Boolean.TRUE.equals(COMMON.defaultUseCustomSpawnPoint.get())) {
      log.info("{} Using custom spawn point {} {} {} for default dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, COMMON.defaultSpawnPointX.get(),
          COMMON.defaultSpawnPointY.get(), COMMON.defaultSpawnPointZ.get());
    }
    if (Boolean.TRUE.equals(COMMON.fishingUseCustomSpawnPoint.get())) {
      log.info("{} Using custom spawn point {} {} {} for fishing dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, COMMON.fishingSpawnPointX.get(),
          COMMON.fishingSpawnPointY.get(), COMMON.fishingSpawnPointZ.get());
    }
    if (Boolean.TRUE.equals(COMMON.gamingUseCustomSpawnPoint.get())) {
      log.info("{} Using custom spawn point {} {} {} for gaming dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, COMMON.gamingSpawnPointX.get(),
          COMMON.gamingSpawnPointY.get(), COMMON.gamingSpawnPointZ.get());
    }
    if (Boolean.TRUE.equals(COMMON.lobbyUseCustomSpawnPoint.get())) {
      log.info("{} Using custom spawn point {} {} {} for lobby dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, COMMON.lobbySpawnPointX.get(),
          COMMON.lobbySpawnPointY.get(), COMMON.lobbySpawnPointZ.get());
    }
    if (Boolean.TRUE.equals(COMMON.miningUseCustomSpawnPoint.get())) {
      log.info("{} Using custom spawn point {} {} {} for mining dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, COMMON.miningSpawnPointX.get(),
          COMMON.miningSpawnPointY.get(), COMMON.miningSpawnPointZ.get());
    }
    if (Boolean.TRUE.equals(COMMON.voidUseCustomSpawnPoint.get())) {
      log.info("{} Using custom spawn point {} {} {} for void dimension",
          Constants.LOG_TELEPORT_MANAGER_PREFIX, COMMON.voidSpawnPointX.get(),
          COMMON.voidSpawnPointY.get(), COMMON.voidSpawnPointZ.get());
    }
  }

  public static boolean teleportToDefaultDimension(ServerPlayer player) {
    ServerLevel defaultDimension = DimensionManager.getDefaultDimension();
    boolean isSameDimension = player.level == defaultDimension;
    boolean successfullyTeleported = false;
    if (Boolean.TRUE.equals(COMMON.defaultUseCustomSpawnPoint.get())) {
      successfullyTeleported =
          teleportPlayer(player, defaultDimension, COMMON.defaultSpawnPointX.get(),
              COMMON.defaultSpawnPointY.get(), COMMON.defaultSpawnPointZ.get());
    } else {
      successfullyTeleported = teleportPlayer(player, defaultDimension);
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendSystemMessage(Component.translatable(Constants.TEXT_PREFIX + "welcome_to_default",
          fishingCommand, gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand));
    }
    return successfullyTeleported;
  }

  public static boolean teleportToFishingDimension(ServerPlayer player) {
    ServerLevel fishingDimension = DimensionManager.getFishingDimension();
    boolean isSameDimension = player.level == fishingDimension;
    boolean successfullyTeleported = false;
    if (Boolean.TRUE.equals(COMMON.fishingUseCustomSpawnPoint.get())) {
      successfullyTeleported =
          teleportPlayer(player, fishingDimension, COMMON.fishingSpawnPointX.get(),
              COMMON.fishingSpawnPointY.get(), COMMON.fishingSpawnPointZ.get());
    } else {
      successfullyTeleported =
          teleportPlayer(player, fishingDimension, defaultFishingSpawnPoint.getX(),
              defaultFishingSpawnPoint.getY(), defaultFishingSpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendSystemMessage(Component.translatable(Constants.TEXT_PREFIX + "welcome_to_fishing",
          fishingCommand, gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand));
    }
    return successfullyTeleported;
  }

  public static boolean teleportToGamingDimension(ServerPlayer player) {
    ServerLevel gamingDimension = DimensionManager.getGamingDimension();
    boolean isSameDimension = player.level == gamingDimension;
    boolean successfullyTeleported = false;
    if (Boolean.TRUE.equals(COMMON.gamingUseCustomSpawnPoint.get())) {
      successfullyTeleported =
          teleportPlayer(player, gamingDimension, COMMON.gamingSpawnPointX.get(),
              COMMON.gamingSpawnPointY.get(), COMMON.gamingSpawnPointZ.get());
    } else {
      successfullyTeleported =
          teleportPlayer(player, gamingDimension, defaultGamingSpawnPoint.getX(),
              defaultGamingSpawnPoint.getY(), defaultGamingSpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendSystemMessage(Component.translatable(Constants.TEXT_PREFIX + "welcome_to_gaming",
          fishingCommand, gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand));
    }
    return successfullyTeleported;
  }

  public static boolean teleportToLobbyDimension(ServerPlayer player) {
    ServerLevel lobbyDimension = DimensionManager.getLobbyDimension();
    boolean isSameDimension = player.level == lobbyDimension;
    boolean successfullyTeleported = false;
    if (Boolean.TRUE.equals(COMMON.lobbyUseCustomSpawnPoint.get())) {
      successfullyTeleported = teleportPlayer(player, lobbyDimension, COMMON.lobbySpawnPointX.get(),
          COMMON.lobbySpawnPointY.get(), COMMON.lobbySpawnPointZ.get());
    } else {
      successfullyTeleported = teleportPlayer(player, lobbyDimension, defaultLobbySpawnPoint.getX(),
          defaultLobbySpawnPoint.getY(), defaultLobbySpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendSystemMessage(Component.translatable(Constants.TEXT_PREFIX + "welcome_to_lobby",
          fishingCommand, gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand));
    }
    return successfullyTeleported;
  }

  public static boolean teleportToMiningDimension(ServerPlayer player) {
    ServerLevel miningDimension = DimensionManager.getMiningDimension();
    boolean isSameDimension = player.level == miningDimension;
    boolean successfullyTeleported = false;
    if (Boolean.TRUE.equals(COMMON.miningUseCustomSpawnPoint.get())) {
      successfullyTeleported =
          teleportPlayer(player, miningDimension, COMMON.miningSpawnPointX.get(),
              COMMON.miningSpawnPointY.get(), COMMON.miningSpawnPointZ.get());
    } else {
      successfullyTeleported =
          teleportPlayer(player, miningDimension, defaultMiningSpawnPoint.getX(),
              defaultMiningSpawnPoint.getY(), defaultMiningSpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendSystemMessage(Component.translatable(Constants.TEXT_PREFIX + "welcome_to_mining",
          fishingCommand, gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand));
    }
    return successfullyTeleported;
  }

  public static boolean teleportToVoidDimension(ServerPlayer player) {
    ServerLevel voidDimension = DimensionManager.getVoidDimension();
    boolean isSameDimension = player.level == voidDimension;
    boolean successfullyTeleported = false;
    if (Boolean.TRUE.equals(COMMON.voidUseCustomSpawnPoint.get())) {
      successfullyTeleported = teleportPlayer(player, voidDimension, COMMON.voidSpawnPointX.get(),
          COMMON.voidSpawnPointY.get(), COMMON.voidSpawnPointZ.get());
    } else {
      successfullyTeleported = teleportPlayer(player, voidDimension, defaultVoidSpawnPoint.getX(),
          defaultVoidSpawnPoint.getY(), defaultVoidSpawnPoint.getZ());
    }
    if (successfullyTeleported && !isSameDimension) {
      player.sendSystemMessage(Component.translatable(Constants.TEXT_PREFIX + "welcome_to_void",
          fishingCommand, gamingCommand, lobbyCommand, miningCommand, spawnCommand, voidCommand));
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
      addTeleportHistory(player);
      player.teleportTo(x, y, z);
      return true;
    }

    // Use dimensional teleporter for the player.
    addTeleportHistory(player);
    player.teleportTo(dimension, x, y, z, player.getYRot(), player.getXRot());
    return true;
  }

  private static void addTeleportHistory(ServerPlayer player) {
    ServerLevel level = player.getLevel();
    ResourceKey<Level> dimension = level.dimension();
    BlockPos blockPos = player.blockPosition();

    log.debug("Add teleport history for player {} in {} with {}", player, dimension, blockPos);
  }
}
