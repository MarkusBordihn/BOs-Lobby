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

package de.markusbordihn.lobby.player;

import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;
import de.markusbordihn.lobby.dimension.DimensionManager;

@EventBusSubscriber
public class PlayerManager {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;
  private static boolean generalDefaultToLobby = COMMON.generalDefaultToLobby.get();

  private static Set<ServerPlayer> playerTeleportList = ConcurrentHashMap.newKeySet();
  private static Set<PlayerValidation> playerValidationList = ConcurrentHashMap.newKeySet();

  private static int playerLoginTrackingTimeout = 45;
  private static long playerLoginValidationTimeoutMilli =
      TimeUnit.SECONDS.toMillis(playerLoginTrackingTimeout);
  private static short ticker = 0;

  protected PlayerManager() {}

  @SubscribeEvent
  public static void onServerAboutToStartEvent(ServerAboutToStartEvent event) {
    playerTeleportList = ConcurrentHashMap.newKeySet();
    playerValidationList = ConcurrentHashMap.newKeySet();

    generalDefaultToLobby = COMMON.generalDefaultToLobby.get();
  }

  @SubscribeEvent
  public static void handleServerStartingEvent(ServerStartingEvent event) {
    if (generalDefaultToLobby) {
      if (DimensionManager.getLobbyDimension() == null) {
        log.error("{} Unable to find lobby dimension {}, transfer to lobby will be disabled!",
            Constants.LOG_PLAYER_MANAGER_PREFIX, DimensionManager.getLobbyDimensionName());
        generalDefaultToLobby = false;
      } else {
        log.info(
            "{} Teleporting players to lobby for their first login and after a server restart.",
            Constants.LOG_TELEPORT_MANAGER_PREFIX);
      }
    }
  }

  @SubscribeEvent
  public static void handlePlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
    if (!generalDefaultToLobby) {
      return;
    }
    String username = event.getPlayer().getName().getString();
    if (!username.isEmpty()) {
      ServerPlayer player =
          ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(username);

      log.info("{} Player {} {} logged in and will be tracked for {} secs.",
          Constants.LOG_PLAYER_MANAGER_PREFIX, username, event.getEntity(),
          playerLoginTrackingTimeout);

      // Heal player by 1 point, just in case.
      player.heal(1);

      // Send message to player that he will be transferred.
      if (!playerTeleportList.contains(player)
          && player.level != DimensionManager.getLobbyDimension()) {
        player.sendMessage(new TranslatableComponent(Constants.TEXT_PREFIX + "transfer_to_lobby"),
            Util.NIL_UUID);
      }
      playerValidationList.add(new PlayerValidation(player));
    }
  }

  @SubscribeEvent
  public static void handlePlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
    if (!generalDefaultToLobby) {
      return;
    }
    String username = event.getPlayer().getName().getString();
    if (!username.isEmpty()) {
      log.debug("{} Player {} logged out.", Constants.LOG_PLAYER_MANAGER_PREFIX, event.getEntity());
      removePlayer(username);
    }
  }

  @SubscribeEvent
  public static void handleServerTickEvent(TickEvent.ServerTickEvent event) {
    if (event.phase == TickEvent.Phase.END || ticker++ < 40 || !generalDefaultToLobby) {
      return;
    }

    if (!playerValidationList.isEmpty()) {
      try {
        // Check for any un-validated players and try to detect if they logged-in.
        for (PlayerValidation playerValidation : playerValidationList) {
          String username = playerValidation.getUsername();
          if (playerValidation.hasPlayerMoved()) {
            long validationTimeInSecs =
                TimeUnit.MILLISECONDS.toSeconds(playerValidation.getValidationTimeElapsed());
            log.info("{} Player was successful validated after {} secs.", username,
                validationTimeInSecs);
            ServerPlayer player = playerValidation.getPlayer();
            if (player.level != DimensionManager.getLobbyDimension()) {
              transferringPlayer(player);
            }
            addPlayer(username);
          } else if (playerValidation
              .getValidationTimeElapsed() >= playerLoginValidationTimeoutMilli) {
            log.warn(
                "User tracking for player {} timed out after {} secs. User will not be teleported to lobby!",
                username, playerLoginTrackingTimeout);
            addPlayer(username);
          }
        }
      } catch (ConcurrentModificationException error) {
        log.error(
            "{} Unexpected error during user validation. Please report the following error under {}.\n{}",
            Constants.LOG_PLAYER_MANAGER_PREFIX, error);
      }
    }
    ticker = 0;
  }

  private static void addPlayer(String username) {
    ServerPlayer player =
        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(username);
    try {
      for (PlayerValidation playerValidation : playerValidationList) {
        if (username.equals(playerValidation.getUsername())) {
          log.debug("{} Found player {} with player tracking {}",
              Constants.LOG_PLAYER_MANAGER_PREFIX, player, playerValidation);
          playerValidationList.remove(playerValidation);
          break;
        }
      }
    } catch (ConcurrentModificationException error) {
      log.error(
          "{} Unexpected error during adding player. Please report the following error under {} .\n{}",
          Constants.LOG_PLAYER_MANAGER_PREFIX, Constants.ISSUE_REPORT, error);
    }
    log.debug("Added player {}", username);
  }

  private static void removePlayer(String username) {
    try {
      for (PlayerValidation playerValidation : playerValidationList) {
        if (username.equals(playerValidation.getUsername())) {
          playerValidationList.remove(playerValidation);
          break;
        }
      }
    } catch (ConcurrentModificationException error) {
      log.error(
          "{} Unexpected error during removing player. Please report the following error under {} .\n{}",
          Constants.LOG_PLAYER_MANAGER_PREFIX, Constants.ISSUE_REPORT, error);
    }
    log.debug("Remove player {}", username);
  }

  private static void transferringPlayer(ServerPlayer player) {
    if (!playerTeleportList.contains(player)) {
      log.info("{} Transferring {} ({}) to lobby ...", Constants.LOG_TELEPORT_MANAGER_PREFIX,
          player, player.level);
      DimensionManager.teleportToLobby(player);
    }
    playerTeleportList.add(player);
  }

}
