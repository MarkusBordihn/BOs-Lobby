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

package de.markusbordihn.lobby.dimension;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;
import de.markusbordihn.lobby.datapack.DataPackHandler;

@EventBusSubscriber
public class DimensionManager {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;

  private static String defaultDimension = COMMON.defaultDimension.get();
  private static boolean defaultUseCustomSpawnPoint = COMMON.defaultUseCustomSpawnPoint.get();
  private static int defaultSpawnPointX = COMMON.defaultSpawnPointX.get();
  private static int defaultSpawnPointY = COMMON.defaultSpawnPointY.get();
  private static int defaultSpawnPointZ = COMMON.defaultSpawnPointZ.get();

  private static String lobbyDimension = COMMON.lobbyDimension.get();
  private static boolean lobbyUseCustomSpawnPoint = COMMON.lobbyUseCustomSpawnPoint.get();
  private static int lobbySpawnPointX = COMMON.lobbySpawnPointX.get();
  private static int lobbySpawnPointY = COMMON.lobbySpawnPointY.get();
  private static int lobbySpawnPointZ = COMMON.lobbySpawnPointZ.get();

  private static String miningDimension = COMMON.miningDimension.get();
  private static boolean miningDisableBatSpawning = COMMON.miningDisableBatSpawning.get();
  private static boolean miningDisableMobSpawning = COMMON.miningDisableMobSpawning.get();
  private static boolean miningDisableMinecartChestSpawning =
      COMMON.miningDisableMinecartChestSpawning.get();
  private static boolean miningUseCustomSpawnPoint = COMMON.miningUseCustomSpawnPoint.get();
  private static int miningSpawnPointX = COMMON.miningSpawnPointX.get();
  private static int miningSpawnPointY = COMMON.miningSpawnPointY.get();
  private static int miningSpawnPointZ = COMMON.miningSpawnPointZ.get();

  private static Set<ServerPlayer> gameTypeReset = ConcurrentHashMap.newKeySet();

  private static ServerLevel defaultLevel = null;
  private static ServerLevel lobbyLevel = null;
  private static ServerLevel miningLevel = null;

  protected DimensionManager() {}

  @SubscribeEvent
  public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {
    // Reset mapping to avoid issues.
    defaultLevel = null;
    lobbyLevel = null;
    miningLevel = null;

    // Make sure we have the current config settings.
    defaultDimension = COMMON.defaultDimension.get();
    defaultUseCustomSpawnPoint = COMMON.defaultUseCustomSpawnPoint.get();
    defaultSpawnPointX = COMMON.defaultSpawnPointX.get();
    defaultSpawnPointY = COMMON.defaultSpawnPointY.get();
    defaultSpawnPointZ = COMMON.defaultSpawnPointZ.get();

    lobbyDimension = COMMON.lobbyDimension.get();
    lobbyUseCustomSpawnPoint = COMMON.lobbyUseCustomSpawnPoint.get();
    lobbySpawnPointX = COMMON.lobbySpawnPointX.get();
    lobbySpawnPointY = COMMON.lobbySpawnPointY.get();
    lobbySpawnPointZ = COMMON.lobbySpawnPointZ.get();

    miningDimension = COMMON.miningDimension.get();
    miningDisableBatSpawning = COMMON.miningDisableBatSpawning.get();
    miningDisableMobSpawning = COMMON.miningDisableMobSpawning.get();
    miningDisableMinecartChestSpawning = COMMON.miningDisableMinecartChestSpawning.get();
    miningUseCustomSpawnPoint = COMMON.miningUseCustomSpawnPoint.get();
    miningSpawnPointX = COMMON.miningSpawnPointX.get();
    miningSpawnPointY = COMMON.miningSpawnPointY.get();
    miningSpawnPointZ = COMMON.miningSpawnPointZ.get();
  }

  @SubscribeEvent
  public static void handleServerStartedEvent(ServerStartedEvent event) {
    // Map dimension and init dimension structure if needed.
    mapServerLevel(event.getServer());
  }

  @SubscribeEvent
  public static void onChangeDimension(PlayerChangedDimensionEvent event) {
    String dimensionLocation = event.getTo().location().toString();
    Player player = event.getPlayer();

    // Ignore known dimension which automatically changing the game type.
    if (defaultDimension.equals(dimensionLocation) || lobbyDimension.equals(dimensionLocation)
        || miningDimension.equals(dimensionLocation)) {
      return;
    }

    // Reset game type to survival if user is ony the gameTypeReset list for all other levels.
    if (player instanceof ServerPlayer serverPlayer && gameTypeReset.contains(serverPlayer)) {
      changeGameType(serverPlayer, GameType.SURVIVAL);
      gameTypeReset.remove(serverPlayer);
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public static void handleEntityJoinWorldEvent(EntityJoinWorldEvent event) {

    // Ignore client side and everything which is not the mining dimension.
    Level level = event.getWorld();
    String dimensionLocation = level.dimension().location().toString();
    if (level.isClientSide() || !dimensionLocation.equals(miningDimension)) {
      return;
    }

    // Ignore specific entities and deny spawn of all others.
    Entity entity = event.getEntity();
    if (entity instanceof ItemEntity || entity instanceof ExperienceOrb
        || entity instanceof LightningBolt || entity instanceof FallingBlockEntity
        || entity instanceof Projectile || entity instanceof Player) {
      return;
    }

    // Allow/deny Minecart Chest spawning
    if (miningDisableMinecartChestSpawning && entity instanceof MinecartChest) {
      event.setResult(Event.Result.DENY);
    }

    if (miningDisableMobSpawning) {
      event.setResult(Event.Result.DENY);
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public static void handleLivingCheckSpawnEvent(LivingSpawnEvent.CheckSpawn event) {
    handleSpawnEvent(event);
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public static void handleLivingSpecialSpawnEvent(LivingSpawnEvent.SpecialSpawn event) {
    handleSpawnEvent(event);
  }

  private static void mapServerLevel(MinecraftServer server) {
    // Skip search if we already found all dimensions.
    if (defaultLevel != null && lobbyLevel != null && miningLevel != null) {
      return;
    }

    // Mapping names to server level for easier access.
    for (ServerLevel serverLevel : server.getAllLevels()) {
      String dimensionLocation = serverLevel.dimension().location().toString();
      if (defaultLevel == null && dimensionLocation.equals(defaultDimension)) {
        log.info("Found default dimension with name {}: {}", defaultDimension, serverLevel);
        defaultLevel = serverLevel;
      } else if (lobbyLevel == null && dimensionLocation.equals(lobbyDimension)) {
        log.info("Found lobby dimension with name {}: {}", lobbyDimension, serverLevel);
        lobbyLevel = serverLevel;
        DataPackHandler.prepareDataPackOnce(serverLevel);
      } else if (miningLevel == null && dimensionLocation.equals(miningDimension)) {
        log.info("Found mining dimension with name {}: {}", miningDimension, serverLevel);
        miningLevel = serverLevel;
        DataPackHandler.prepareDataPackOnce(serverLevel);
      }
    }

    // Give error messages, if we are unable to match any dimension.
    if (defaultLevel == null) {
      log.error("Unable to found default dimension named {}!", defaultDimension);
    }
    if (lobbyLevel == null) {
      log.error("Unable to found lobby dimension named {}!", lobbyDimension);
    }
    if (miningLevel == null) {
      log.error("Unable to found mining dimension named {}!", miningDimension);
    }
  }

  public static ServerLevel getLobbyDimension() {
    if (lobbyLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return lobbyLevel;
  }

  public static ServerLevel getMiningDimension() {
    if (miningLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return miningLevel;
  }

  public static ServerLevel getDefaultDimension() {
    if (defaultLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return defaultLevel;
  }

  public static void teleportToDefault(Player player) {
    if (player.getLevel().isClientSide() || getLobbyDimension() == null) {
      return;
    }

    // Only teleport players from different dimensions.
    if (player.level != getDefaultDimension()) {
      if (defaultUseCustomSpawnPoint) {
        player.changeDimension(getDefaultDimension(),
            new DimensionTeleporter(defaultSpawnPointX, defaultSpawnPointY, defaultSpawnPointZ));
      } else {
        player.changeDimension(getDefaultDimension(), new DimensionTeleporter());
      }
      changeGameType(player, GameType.SURVIVAL);
    }
  }

  public static void teleportToLobby(Player player) {
    // Ignore client side levels and if lobby dimension was not found.
    if (player.getLevel().isClientSide() || getLobbyDimension() == null) {
      return;
    }
    boolean isSameDimension = player.level == getLobbyDimension();
    if (isSameDimension) {
      if (lobbyUseCustomSpawnPoint) {
        player.teleportTo(lobbySpawnPointX, lobbySpawnPointY, lobbySpawnPointZ);
      } else {
        BlockPos blockPos = lobbyLevel.getSharedSpawnPos();
        player.teleportTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
      }
    } else {
      if (lobbyUseCustomSpawnPoint) {
        player.changeDimension(getLobbyDimension(),
            new DimensionTeleporter(lobbySpawnPointX, lobbySpawnPointY, lobbySpawnPointZ));
      } else {
        player.changeDimension(getLobbyDimension(), new DimensionTeleporter());
      }
    }
    changeGameType(player, GameType.ADVENTURE);
    if (!isSameDimension) {
      player.sendMessage(new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_lobby"),
          Util.NIL_UUID);
    }
  }

  public static void teleportToMining(Player player) {
    // Ignore client side levels and if mining dimension was not found.
    if (player.getLevel().isClientSide() || getMiningDimension() == null) {
      return;
    }
    boolean isSameDimension = player.level == getMiningDimension();
    if (isSameDimension) {
      if (miningUseCustomSpawnPoint) {
        player.teleportTo(miningSpawnPointX, miningSpawnPointY, miningSpawnPointZ);
      } else {
        BlockPos blockPos = miningLevel.getSharedSpawnPos();
        player.teleportTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
      }
    } else {
      if (miningUseCustomSpawnPoint) {
        player.changeDimension(getMiningDimension(),
            new DimensionTeleporter(miningSpawnPointX, miningSpawnPointY, miningSpawnPointZ));
      } else {
        player.changeDimension(getMiningDimension(), new DimensionTeleporter());
      }
    }
    changeGameType(player, GameType.SURVIVAL);
    if (!isSameDimension) {
      player.sendMessage(new TranslatableComponent(Constants.TEXT_PREFIX + "welcome_to_mining"),
          Util.NIL_UUID);
    }
  }

  public static void changeGameType(Player player, GameType gameType) {
    if (player instanceof ServerPlayer serverPlayer) {
      changeGameType(serverPlayer, gameType);
    }
  }

  public static void changeGameType(ServerPlayer serverPlayer, GameType gameType) {
    if (serverPlayer.gameMode.getGameModeForPlayer() != gameType) {
      // Add player to reset list of the game mode if game type is not survival to avoid cheating.
      if (gameType != GameType.SURVIVAL) {
        gameTypeReset.add(serverPlayer);
      }
      serverPlayer.setGameMode(gameType);
    }
  }

  private static void handleSpawnEvent(LivingSpawnEvent event) {

    // Ignore client side and if mob spawning is allowed.
    LevelAccessor level = event.getWorld();
    if (level.isClientSide() || !miningDisableMobSpawning) {
      return;
    }

    // Ignore null entities and specific entities.
    Entity entity = event.getEntity();
    if (entity == null || entity instanceof Projectile) {
      return;
    }

    // Restrict spawn control to mining dimension
    String dimensionLocation = entity.getLevel().dimension().location().toString();
    if (!miningDimension.equals(dimensionLocation)) {
      return;
    }

    // Allow/deny bat spawning for better cave experience
    if (!miningDisableBatSpawning && entity instanceof Bat) {
      return;
    }

    event.setResult(Event.Result.DENY);
  }

}
