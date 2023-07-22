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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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
import de.markusbordihn.lobby.data.LobbyData;
import de.markusbordihn.lobby.dimension.DimensionManager;

@EventBusSubscriber
public class PlayerManager {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;

  private static Set<UUID> playerTeleportList = ConcurrentHashMap.newKeySet();
  private static Set<PlayerValidation> playerValidationList = ConcurrentHashMap.newKeySet();

  private static final int PLAYER_LOGIN_TRACKING_TIMEOUT = 45;
  private static final long PLAYER_LOGIN_VALIDATION_TIMEOUT_MILLI =
      TimeUnit.SECONDS.toMillis(PLAYER_LOGIN_TRACKING_TIMEOUT);
  private static int ticker = 0;

  private static Component lobbyCommand =
      Component.literal("/lobby").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)
          .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lobby")));

  protected PlayerManager() {}

  @SubscribeEvent
  public static void onServerAboutToStartEvent(ServerAboutToStartEvent event) {
    playerTeleportList = ConcurrentHashMap.newKeySet();
    playerValidationList = ConcurrentHashMap.newKeySet();
  }

  @SubscribeEvent
  public static void handleServerStartingEvent(ServerStartingEvent event) {
    if (automaticTransferIsEnabled()) {
      if (DimensionManager.getLobbyDimension() == null) {
        log.error("{} Unable to find lobby dimension {}, transfer to lobby will be disabled!",
            Constants.LOG_PLAYER_MANAGER_PREFIX, DimensionManager.getLobbyDimensionName());
      } else if (Boolean.FALSE.equals(COMMON.lobbyEnabled.get())) {
        log.error("{} lobby dimension is disabled {}, transfer to lobby will be disabled!",
            Constants.LOG_PLAYER_MANAGER_PREFIX, DimensionManager.getLobbyDimensionName());
      } else {
        if (Boolean.TRUE.equals(COMMON.generalDefaultToLobbyOnce.get())) {
          log.info("{} Only teleports the player once to the lobby with their first connect!",
              Constants.LOG_TELEPORT_MANAGER_PREFIX);
          Set<UUID> storedPlayerTeleportList = LobbyData.get().getPlayerTeleportList();
          if (!storedPlayerTeleportList.isEmpty()) {
            log.info(
                "{} Using stored Player Teleport List to limiting automatic transfers to lobby: {}",
                Constants.LOG_TELEPORT_MANAGER_PREFIX, storedPlayerTeleportList);
            playerTeleportList.addAll(storedPlayerTeleportList);
          }
        } else if (Boolean.TRUE.equals(COMMON.generalDefaultToLobbyAlways.get())) {
          log.info("{} Always teleport players to lobby on their server join!",
              Constants.LOG_TELEPORT_MANAGER_PREFIX);
        } else {
          log.info(
              "{} Teleports the player to the lobby for their first connect or after a server restart!.",
              Constants.LOG_TELEPORT_MANAGER_PREFIX);
        }
      }
    }
  }

  @SubscribeEvent
  public static void handlePlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
    if (!automaticTransferIsEnabled()) {
      return;
    }
    String username = event.getEntity().getName().getString();
    if (!username.isEmpty()) {
      ServerPlayer player =
          ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(username);

      if (player == null) {
        log.error("{} Unable to get player for {} {} from the server!",
            Constants.LOG_PLAYER_MANAGER_PREFIX, username, event.getEntity());
        return;
      }

      log.info("{} Player {} {} logged in and will be tracked for {} secs.",
          Constants.LOG_PLAYER_MANAGER_PREFIX, username, event.getEntity(),
          PLAYER_LOGIN_VALIDATION_TIMEOUT_MILLI);

      // Heal player by 1 point, just in case.
      player.heal(1);


      // Send message to player that he will be transferred.
      if ((COMMON.generalDefaultToLobbyAlways.get()
          || !playerTeleportList.contains(player.getUUID()))
          && player.level() != DimensionManager.getLobbyDimension()) {
        player.sendSystemMessage(
            Component.translatable(Constants.TEXT_PREFIX + "transfer_to_lobby", lobbyCommand));
      }
      playerValidationList.add(new PlayerValidation(player));
    }
  }

  @SubscribeEvent
  public static void handlePlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
    if (!automaticTransferIsEnabled()) {
      return;
    }
    String username = event.getEntity().getName().getString();
    if (!username.isEmpty()) {
      log.debug("{} Player {} logged out.", Constants.LOG_PLAYER_MANAGER_PREFIX, event.getEntity());
      removePlayer(username);
    }
  }

  @SubscribeEvent
  public static void handleServerTickEvent(TickEvent.ServerTickEvent event) {
    if (event.phase == TickEvent.Phase.END || !automaticTransferIsEnabled() || ticker++ < 40) {
      return;
    }

    if (!playerValidationList.isEmpty()) {
      try {
        // Check for any un-validated players and try to detect if they logged-in.
        for (PlayerValidation playerValidation : playerValidationList) {
          String username = playerValidation.getUsername();
          if (playerValidation.hasPlayerMoved()) {
            long validationTimeInSecs = playerValidation.getValidationTimeSecondsElapsed();
            log.info("{} Player was successful validated after {} secs.", username,
                validationTimeInSecs);
            transferringPlayerToLobby(playerValidation.getPlayer());
            addPlayer(username);
          } else if (playerValidation
              .getValidationTimeElapsed() >= PLAYER_LOGIN_VALIDATION_TIMEOUT_MILLI) {
            log.warn(
                "User tracking for player {} timed out after {} secs. User will not be teleported to lobby!",
                username, PLAYER_LOGIN_TRACKING_TIMEOUT);
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

  private static boolean automaticTransferIsEnabled() {
    return (COMMON.generalDefaultToLobby.get() || COMMON.generalDefaultToLobbyOnce.get()
        || COMMON.generalDefaultToLobbyAlways.get()) && DimensionManager.getLobbyDimension() != null
        && COMMON.lobbyEnabled.get();
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

  private static void transferringPlayerToLobby(ServerPlayer player) {
    if (player == null || player.level() == DimensionManager.getLobbyDimension()) {
      return;
    }
    if (Boolean.TRUE.equals(COMMON.generalDefaultToLobbyAlways.get())
        || ((COMMON.generalDefaultToLobbyOnce.get() || COMMON.generalDefaultToLobby.get())
            && !playerTeleportList.contains(player.getUUID()))) {
      if (Boolean.TRUE.equals(!COMMON.generalDefaultToLobbyAlways.get())
          && playerTeleportList.contains(player.getUUID())) {
        log.info("{} Skip transferring {} ({}) to lobby ...", Constants.LOG_TELEPORT_MANAGER_PREFIX,
            player, player.level());
      } else {
        if (Boolean.TRUE.equals(COMMON.generalDefaultToLobbyOnce.get())) {
          log.info("{} Transferring {} ({}) for the first time and only once to lobby ...",
              Constants.LOG_TELEPORT_MANAGER_PREFIX, player, player.level());
        } else {
          log.info("{} Transferring {} ({}) to lobby ...", Constants.LOG_TELEPORT_MANAGER_PREFIX,
              player, player.level());
        }
        DimensionManager.teleportToLobby(player);
      }
    }
    playerTeleportList.add(player.getUUID());

    // Store Player Teleport List, if user should be only transferred once!
    if (Boolean.TRUE.equals(COMMON.generalDefaultToLobbyOnce.get())) {
      LobbyData.get().setPlayerTeleportList(playerTeleportList);
    }
  }

}
