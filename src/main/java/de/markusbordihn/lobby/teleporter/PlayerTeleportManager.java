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

import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;
import de.markusbordihn.lobby.dimension.DimensionManager;
import de.markusbordihn.lobby.player.PlayerValidation;

@EventBusSubscriber
public class PlayerTeleportManager {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;
  private static boolean teleportDelayCounterVisible = COMMON.teleportDelayCounterVisible.get();
  private static boolean teleportDelayEnabled = COMMON.teleportDelayEnabled.get();
  private static int teleportDelayCounter = COMMON.teleportDelayCounter.get();

  private static Set<PlayerValidation> teleportPlayerToDefaultList = ConcurrentHashMap.newKeySet();
  private static Set<PlayerValidation> teleportPlayerToFishingList = ConcurrentHashMap.newKeySet();
  private static Set<PlayerValidation> teleportPlayerToGamingList = ConcurrentHashMap.newKeySet();
  private static Set<PlayerValidation> teleportPlayerToLobbyList = ConcurrentHashMap.newKeySet();
  private static Set<PlayerValidation> teleportPlayerToMiningList = ConcurrentHashMap.newKeySet();
  private static Set<PlayerValidation> teleportPlayerToVoidList = ConcurrentHashMap.newKeySet();

  private static final int PLAYER_TELEPORT_CHECK = 20;
  private static int ticker = 0;

  protected PlayerTeleportManager() {

  }

  @SubscribeEvent
  public static void onServerAboutToStartEvent(ServerAboutToStartEvent event) {
    teleportDelayCounter = COMMON.teleportDelayCounter.get();
    teleportDelayCounterVisible = COMMON.teleportDelayCounterVisible.get();
    teleportDelayEnabled = COMMON.teleportDelayEnabled.get();

    if (teleportDelayEnabled && teleportDelayCounter > 0) {
      log.info("Teleporting of Players will be delayed by {} seconds.", teleportDelayCounter);
    }
  }

  @SubscribeEvent
  public static void handleServerTickEvent(TickEvent.ServerTickEvent event) {

    if (event.phase == TickEvent.Phase.END || !teleportDelayEnabled || teleportDelayCounter <= 0
        || ticker++ < PLAYER_TELEPORT_CHECK) {
      return;
    }

    // Check each dimension teleport list separated.
    checkTeleportToDimension(teleportPlayerToDefaultList, "Default");
    checkTeleportToDimension(teleportPlayerToFishingList, "Fishing");
    checkTeleportToDimension(teleportPlayerToGamingList, "Gaming");
    checkTeleportToDimension(teleportPlayerToLobbyList, "Lobby");
    checkTeleportToDimension(teleportPlayerToMiningList, "Mining");
    checkTeleportToDimension(teleportPlayerToVoidList, "Void");

    ticker = 0;
  }

  public static void teleportPlayerToDefault(ServerPlayer player) {
    teleportPlayerToDefaultList.add(new PlayerValidation(player));
  }

  public static void teleportPlayerToFishing(ServerPlayer player) {
    teleportPlayerToFishingList.add(new PlayerValidation(player));
  }

  public static void teleportPlayerToGaming(ServerPlayer player) {
    teleportPlayerToGamingList.add(new PlayerValidation(player));
  }

  public static void teleportPlayerToLobby(ServerPlayer player) {
    teleportPlayerToLobbyList.add(new PlayerValidation(player));
  }

  public static void teleportPlayerToMining(ServerPlayer player) {
    teleportPlayerToMiningList.add(new PlayerValidation(player));
  }

  public static void teleportPlayerToVoid(ServerPlayer player) {
    teleportPlayerToVoidList.add(new PlayerValidation(player));
  }

  private static void checkTeleportToDimension(Set<PlayerValidation> playerValidationSet,
      String dimensionName) {
    if (playerValidationSet.isEmpty() || dimensionName.isEmpty()) {
      return;
    }
    try {
      for (PlayerValidation playerValidation : playerValidationSet) {
        String username = playerValidation.getUsername();
        ServerPlayer player = playerValidation.getPlayer();
        if (playerValidation.hasPlayerMovedPosition()) {
          log.debug("Player {} has moved, abort teleport to {} ...", username);
          player.sendSystemMessage(
              Component.translatable(Constants.TEXT_PREFIX + "teleport_abort", dimensionName)
                  .withStyle(ChatFormatting.RED));
          playerValidationSet.remove(playerValidation);
          return;
        } else if (playerValidation.getValidationTimeSecondsElapsed() >= teleportDelayCounter) {
          switch (dimensionName) {
            case "Default":
              DimensionManager.teleportToDefault(player);
              break;
            case "Fishing":
              DimensionManager.teleportToFishing(player);
              break;
            case "Gaming":
              DimensionManager.teleportToGaming(player);
              break;
            case "Lobby":
              DimensionManager.teleportToLobby(player);
              break;
            case "Mining":
              DimensionManager.teleportToMining(player);
              break;
            case "Void":
              DimensionManager.teleportToVoid(player);
              break;
            default:
              log.error("Unsupported dimension {}!", dimensionName);
          }
          playerValidationSet.remove(playerValidation);
          return;
        } else {
          long teleportCounterRemaining =
              teleportDelayCounter - playerValidation.getValidationTimeSecondsElapsed();
          log.debug("Player {} has not moved, teleport in {} secs ...", username,
              teleportCounterRemaining);
          if (teleportDelayCounterVisible) {
            player.sendSystemMessage(
                Component.translatable(Constants.TEXT_PREFIX + "teleport_remaining", dimensionName,
                    teleportCounterRemaining).withStyle(ChatFormatting.GREEN));
          }
        }
      }
    } catch (ConcurrentModificationException error) {
      log.error(
          "{} Unexpected error during user validation. Please report the following error under {}.\n{}",
          Constants.LOG_PLAYER_MANAGER_PREFIX, error);
    }
  }

}
